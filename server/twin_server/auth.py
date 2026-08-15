import hmac

from fastapi import HTTPException, Request

from . import config


def require_token(request: Request) -> None:
    header = request.headers.get("authorization", "")
    token = header.removeprefix("Bearer ").strip()
    if not config.TOKEN or not hmac.compare_digest(token, config.TOKEN):
        raise HTTPException(status_code=401, detail="unauthorized")
