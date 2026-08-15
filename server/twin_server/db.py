import sqlite3
import threading
from datetime import datetime, timezone

from . import config

_local = threading.local()

SCHEMA = """
CREATE TABLE IF NOT EXISTS messages (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    role TEXT NOT NULL,
    content TEXT NOT NULL,
    mode TEXT NOT NULL DEFAULT 'chat',
    created_at TEXT NOT NULL
);
CREATE VIRTUAL TABLE IF NOT EXISTS memory_fts USING fts5(
    path, chunk, content
);
"""


def get_conn() -> sqlite3.Connection:
    conn = getattr(_local, "conn", None)
    if conn is None:
        conn = sqlite3.connect(config.DB_PATH)
        conn.execute("PRAGMA journal_mode=WAL")
        conn.row_factory = sqlite3.Row
        conn.executescript(SCHEMA)
        _local.conn = conn
    return conn


def now_iso() -> str:
    return datetime.now(timezone.utc).isoformat(timespec="seconds")


def add_message(role: str, content: str, mode: str = "chat") -> None:
    conn = get_conn()
    conn.execute(
        "INSERT INTO messages (role, content, mode, created_at) VALUES (?, ?, ?, ?)",
        (role, content, mode, now_iso()),
    )
    conn.commit()


def recent_messages(limit: int) -> list[dict]:
    rows = get_conn().execute(
        "SELECT role, content FROM messages ORDER BY id DESC LIMIT ?", (limit,)
    ).fetchall()
    return [{"role": r["role"], "content": r["content"]} for r in reversed(rows)]


def messages_for_day(day_iso_prefix: str) -> list[sqlite3.Row]:
    return get_conn().execute(
        "SELECT role, content, created_at FROM messages WHERE created_at LIKE ? ORDER BY id",
        (f"{day_iso_prefix}%",),
    ).fetchall()


def reindex_file(path: str, chunks: list[str]) -> None:
    conn = get_conn()
    conn.execute("DELETE FROM memory_fts WHERE path = ?", (path,))
    for i, chunk in enumerate(chunks):
        conn.execute(
            "INSERT INTO memory_fts (path, chunk, content) VALUES (?, ?, ?)",
            (path, str(i), chunk),
        )
    conn.commit()


def search_fts(query: str, top_k: int) -> list[dict]:
    # FTS5 query syntax chokes on raw punctuation; quote each term.
    terms = [t.replace('"', "") for t in query.split() if t.strip()]
    if not terms:
        return []
    fts_query = " OR ".join(f'"{t}"' for t in terms)
    try:
        rows = get_conn().execute(
            "SELECT path, content, rank FROM memory_fts WHERE memory_fts MATCH ? ORDER BY rank LIMIT ?",
            (fts_query, top_k),
        ).fetchall()
    except sqlite3.OperationalError:
        return []
    return [{"path": r["path"], "content": r["content"]} for r in rows]
