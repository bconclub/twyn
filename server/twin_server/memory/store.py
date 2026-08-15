"""Markdown memory store. Files are source of truth; FTS5 is a derived index."""
import re
from datetime import date, datetime, timezone
from pathlib import Path

from .. import config, db

PERSONA_FILE = "TWIN.md"
FACTS_FILE = "facts.md"
INBOX_FILE = "inbox.md"

PERSONA_TEMPLATE = """# TWIN — Persona

## Identity

## Voice & Style

## Values & Boundaries

## Business Context

## Preferences
"""


def _root() -> Path:
    config.MEMORY_DIR.mkdir(parents=True, exist_ok=True)
    (config.MEMORY_DIR / "projects").mkdir(exist_ok=True)
    (config.MEMORY_DIR / "daily").mkdir(exist_ok=True)
    return config.MEMORY_DIR


def _safe_path(rel: str) -> Path:
    root = _root()
    p = (root / rel).resolve()
    if root.resolve() not in p.parents and p != root.resolve():
        raise ValueError(f"path escapes memory root: {rel}")
    return p


def read(rel: str) -> str:
    p = _safe_path(rel)
    return p.read_text(encoding="utf-8") if p.exists() else ""


def write(rel: str, content: str) -> None:
    p = _safe_path(rel)
    p.parent.mkdir(parents=True, exist_ok=True)
    p.write_text(content, encoding="utf-8")
    _reindex(rel, content)


def _chunks(content: str) -> list[str]:
    parts = [c.strip() for c in re.split(r"\n\s*\n", content)]
    return [c for c in parts if c]


def _reindex(rel: str, content: str) -> None:
    db.reindex_file(rel, _chunks(content))


def reindex_all() -> int:
    root = _root()
    count = 0
    for p in root.rglob("*.md"):
        rel = p.relative_to(root).as_posix()
        _reindex(rel, p.read_text(encoding="utf-8"))
        count += 1
    return count


def ensure_persona() -> str:
    content = read(PERSONA_FILE)
    if not content:
        write(PERSONA_FILE, PERSONA_TEMPLATE)
        content = PERSONA_TEMPLATE
    return content


def append_fact(fact: str) -> None:
    today = date.today().isoformat()
    content = read(FACTS_FILE)
    if not content:
        content = "# Facts\n"
    content = content.rstrip() + f"\n- [{today}] {fact.strip()}\n"
    write(FACTS_FILE, content)


def append_inbox(note: str) -> None:
    stamp = datetime.now(timezone.utc).isoformat(timespec="minutes")
    content = read(INBOX_FILE)
    content = (content.rstrip() + f"\n- [{stamp}] {note.strip()}\n").lstrip()
    write(INBOX_FILE, content)


def update_persona_section(section: str, content: str) -> bool:
    """Replace the body of an existing `## section`; returns False if not found."""
    persona = ensure_persona()
    pattern = re.compile(
        rf"(^## {re.escape(section)}\s*\n)(.*?)(?=^## |\Z)", re.MULTILINE | re.DOTALL
    )
    if not pattern.search(persona):
        persona = persona.rstrip() + f"\n\n## {section}\n\n{content.strip()}\n"
        write(PERSONA_FILE, persona)
        return True
    persona = pattern.sub(lambda m: m.group(1) + "\n" + content.strip() + "\n\n", persona)
    write(PERSONA_FILE, persona)
    return True


def write_project_note(project: str, note: str) -> None:
    slug = re.sub(r"[^a-z0-9-]+", "-", project.lower()).strip("-") or "misc"
    rel = f"projects/{slug}.md"
    today = date.today().isoformat()
    content = read(rel)
    if not content:
        content = f"# {project}\n"
    content = content.rstrip() + f"\n- [{today}] {note.strip()}\n"
    write(rel, content)


def write_daily(day: str, summary: str) -> None:
    write(f"daily/{day}.md", f"# {day}\n\n{summary.strip()}\n")


def last_daily_notes(n: int = 2) -> list[tuple[str, str]]:
    daily_dir = _root() / "daily"
    files = sorted(daily_dir.glob("*.md"), reverse=True)[:n]
    return [(p.stem, p.read_text(encoding="utf-8")) for p in files]


def search(query: str, top_k: int | None = None) -> list[dict]:
    return db.search_fts(query, top_k or config.FTS_TOP_K)
