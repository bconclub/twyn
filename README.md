# TWYN

Exocortex digital twin. Not an assistant. A clone: your memory, your voice, your judgment.

- `app/` — Android (Kotlin/Compose/Glance): chat, voice, widget, quick tile
- `server/` — FastAPI brain: Claude agent loop, markdown memory + FTS5
- `memory/` — memory templates (runtime memory lives on the VPS)
- `deploy/` — Docker + Caddy for Hostinger VPS
- `docs/` — [market plan](docs/market-plan.md) · [roadmap](docs/roadmap.md) · [ADRs](docs/adr/)

## Quick start (server, local)

```bash
cd server
python -m venv .venv && .venv/Scripts/activate
pip install -e .
set TWIN_TOKEN=dev-token & set ANTHROPIC_API_KEY=sk-...
uvicorn twin_server.main:app --reload
```

## App

Open `app/` with Gradle (JDK 17): `gradlew assembleDebug`, sideload the APK, set server URL + token in Settings.
