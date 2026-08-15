import logging
from contextlib import asynccontextmanager

from apscheduler.schedulers.background import BackgroundScheduler
from apscheduler.triggers.cron import CronTrigger
from fastapi import Depends, FastAPI, HTTPException
from fastapi.responses import PlainTextResponse, StreamingResponse
from pydantic import BaseModel

from . import chat, config, db
from .auth import require_token
from .memory import store, summarizer

logging.basicConfig(level=logging.INFO)


@asynccontextmanager
async def lifespan(app: FastAPI):
    db.get_conn()
    store.ensure_persona()
    store.reindex_all()
    scheduler = BackgroundScheduler()
    scheduler.add_job(summarizer.run_nightly, CronTrigger(hour=config.SUMMARY_HOUR, minute=0))
    scheduler.start()
    yield
    scheduler.shutdown(wait=False)


app = FastAPI(title="TWIN", lifespan=lifespan)


class ChatRequest(BaseModel):
    message: str
    mode: str = "chat"  # chat | interview


@app.get("/health")
def health():
    return {"ok": True}


@app.post("/chat", dependencies=[Depends(require_token)])
def post_chat(req: ChatRequest):
    if req.mode not in ("chat", "interview"):
        raise HTTPException(400, "mode must be 'chat' or 'interview'")
    return StreamingResponse(
        chat.stream_chat(req.message, req.mode),
        media_type="text/event-stream",
        headers={"Cache-Control": "no-cache", "X-Accel-Buffering": "no"},
    )


@app.get("/messages", dependencies=[Depends(require_token)])
def get_messages(limit: int = 50):
    return db.recent_messages(min(limit, 200))


@app.get("/memory/{path:path}", response_class=PlainTextResponse, dependencies=[Depends(require_token)])
def get_memory(path: str):
    try:
        content = store.read(path)
    except ValueError:
        raise HTTPException(400, "bad path")
    if not content:
        raise HTTPException(404, "not found")
    return content


class MemoryPut(BaseModel):
    content: str


@app.put("/memory/{path:path}", dependencies=[Depends(require_token)])
def put_memory(path: str, body: MemoryPut):
    if not path.endswith(".md"):
        raise HTTPException(400, "only .md files")
    try:
        store.write(path, body.content)
    except ValueError:
        raise HTTPException(400, "bad path")
    return {"ok": True}
