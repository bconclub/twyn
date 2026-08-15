import os
from pathlib import Path

from dotenv import load_dotenv

load_dotenv()

DATA_DIR = Path(os.environ.get("TWIN_DATA_DIR", Path(__file__).resolve().parent.parent / "data"))
MEMORY_DIR = DATA_DIR / "memory"
DB_PATH = DATA_DIR / "twin.db"

TOKEN = os.environ.get("TWIN_TOKEN", "")
CHAT_MODEL = os.environ.get("TWIN_CHAT_MODEL", "claude-sonnet-5")
DEEP_MODEL = os.environ.get("TWIN_DEEP_MODEL", "claude-fable-5")
MAX_TOKENS = int(os.environ.get("TWIN_MAX_TOKENS", "2048"))
HISTORY_LIMIT = int(os.environ.get("TWIN_HISTORY_LIMIT", "30"))
FTS_TOP_K = int(os.environ.get("TWIN_FTS_TOP_K", "6"))
SUMMARY_HOUR = int(os.environ.get("TWIN_SUMMARY_HOUR", "2"))

DATA_DIR.mkdir(parents=True, exist_ok=True)
MEMORY_DIR.mkdir(parents=True, exist_ok=True)
