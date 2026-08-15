"""Nightly job: summarize the day, distill inbox into facts. Runs on claude-fable-5."""
import logging
from datetime import date, timedelta

from .. import chat, config, db
from . import store

log = logging.getLogger("twin.summarizer")


def run_nightly() -> None:
    day = (date.today() - timedelta(days=1)).isoformat()
    try:
        _summarize_day(day)
    except Exception:
        log.exception("daily summary failed for %s", day)
    try:
        _distill_inbox()
    except Exception:
        log.exception("inbox distillation failed")


def _summarize_day(day: str) -> None:
    rows = db.messages_for_day(day)
    if not rows:
        return
    transcript = "\n".join(f"{r['role']}: {r['content']}" for r in rows)
    resp = chat.client().messages.create(
        model=config.DEEP_MODEL,
        max_tokens=1024,
        system="You distill a day of conversation between a user and their digital twin into a compact daily memory note: key events, decisions, moods, open loops. Markdown bullets, nothing else.",
        messages=[{"role": "user", "content": transcript[:100_000]}],
    )
    summary = "".join(b.text for b in resp.content if b.type == "text")
    if summary.strip():
        store.write_daily(day, summary)
        log.info("daily summary written for %s", day)


def _distill_inbox() -> None:
    inbox = store.read(store.INBOX_FILE)
    if not inbox.strip():
        return
    resp = chat.client().messages.create(
        model=config.DEEP_MODEL,
        max_tokens=1024,
        system="Extract durable atomic facts from these raw notes. Output ONLY a list, one fact per line, no bullets, no commentary. Skip anything ephemeral.",
        messages=[{"role": "user", "content": inbox[:50_000]}],
    )
    text = "".join(b.text for b in resp.content if b.type == "text")
    facts = [ln.strip("-• ").strip() for ln in text.splitlines() if ln.strip()]
    for fact in facts:
        store.append_fact(fact)
    store.write(store.INBOX_FILE, "")
    log.info("distilled %d facts from inbox", len(facts))
