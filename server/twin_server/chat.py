"""SSE chat endpoint: Claude agent loop with memory tools."""
import json
from pathlib import Path

import anthropic

from . import config, db
from .memory import store, tools

PROMPTS = Path(__file__).parent / "prompts"

_client: anthropic.Anthropic | None = None


def client() -> anthropic.Anthropic:
    global _client
    if _client is None:
        _client = anthropic.Anthropic()
    return _client


def _prompt(name: str) -> str:
    return (PROMPTS / f"{name}.md").read_text(encoding="utf-8")


def build_system(mode: str, user_message: str) -> str:
    base = _prompt("interview" if mode == "interview" else "system")
    parts = [base]

    persona = store.ensure_persona()
    parts.append(f"# Persona (TWIN.md)\n\n{persona}")

    facts = store.read(store.FACTS_FILE)
    if facts:
        parts.append(f"# Facts\n\n{facts}")

    for day, note in store.last_daily_notes(2):
        parts.append(f"# Daily summary {day}\n\n{note}")

    hits = store.search(user_message)
    if hits:
        seen = set()
        lines = []
        for h in hits:
            key = (h["path"], h["content"][:80])
            if key in seen:
                continue
            seen.add(key)
            lines.append(f"[{h['path']}] {h['content']}")
        parts.append("# Retrieved memory\n\n" + "\n\n".join(lines))

    return "\n\n---\n\n".join(parts)


def _sse(event: str, data: dict) -> str:
    return f"event: {event}\ndata: {json.dumps(data, ensure_ascii=False)}\n\n"


def stream_chat(user_message: str, mode: str = "chat"):
    """Generator yielding SSE lines. Runs the full agent loop incl. tool use."""
    db.add_message("user", user_message, mode)
    system = build_system(mode, user_message)

    history = db.recent_messages(config.HISTORY_LIMIT)
    # history already includes the just-saved user message
    messages: list[dict] = [{"role": m["role"], "content": m["content"]} for m in history]

    full_text: list[str] = []

    for _ in range(8):  # tool-use round cap
        with client().messages.stream(
            model=config.CHAT_MODEL,
            max_tokens=config.MAX_TOKENS,
            system=system,
            messages=messages,
            tools=tools.TOOLS,
        ) as s:
            for text in s.text_stream:
                full_text.append(text)
                yield _sse("delta", {"text": text})
            final = s.get_final_message()

        if final.stop_reason != "tool_use":
            break

        tool_results = []
        assistant_content = []
        for block in final.content:
            if block.type == "text":
                assistant_content.append({"type": "text", "text": block.text})
            elif block.type == "tool_use":
                assistant_content.append(
                    {"type": "tool_use", "id": block.id, "name": block.name, "input": block.input}
                )
                result = tools.handle(block.name, block.input)
                tool_results.append(
                    {"type": "tool_result", "tool_use_id": block.id, "content": result}
                )
                yield _sse("tool", {"name": block.name})
        messages.append({"role": "assistant", "content": assistant_content})
        messages.append({"role": "user", "content": tool_results})

    reply = "".join(full_text).strip()
    if reply:
        db.add_message("assistant", reply, mode)
    yield _sse("done", {"text": reply})
