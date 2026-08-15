# ADR 0001 — v0 Stack

**Status:** Accepted · **Date:** 2026-08-15

## Decisions

1. **Android: native Kotlin + Jetpack Compose + Glance.** Widget/tile/instant-launch are native-only surface in every framework; RN/Flutter would need the same Kotlin widget code plus a bridge. One platform, one user: cross-platform buys nothing. Sideload APK via `gradlew assembleRelease`, no Play Store. JDK 17 pinned.

2. **Interaction: deep-link combo, not in-widget typing.** Android widgets (RemoteViews/Glance) cannot host a text input. Speed comes from: Glance widget (last message + tap→`twin://chat`, mic→`twin://chat?voice=1`), translucent no-splash ChatActivity with pre-warmed OkHttp and SSE streaming, quick-settings tile, app shortcuts. Default-assistant role deferred to v0.5.

3. **Server: FastAPI on Hostinger VPS, not Vercel serverless.** The brain needs persistent disk (SQLite + markdown memory), a persistent process (SSE agent loops, cron, proactive push), and fixed cost. Serverless kills all three. Python 3.12, anthropic SDK, SQLite WAL, APScheduler, Docker Compose behind Caddy auto-HTTPS. Single static bearer token (one user).

4. **Models:** `claude-sonnet-5` interactive chat (latency/cost), `claude-fable-5` nightly summarization + persona distillation (quality).

5. **Retrieval v0: FTS5, no embeddings.** Single-user corpus is small; keyword search + always-loaded persona + recency ships in hours. Add sqlite-vec + Voyage embeddings in v0.5 when corpus outgrows it.

6. **Memory: hybrid.** Markdown files are source of truth (inspectable, editable, git-backable): TWIN.md (persona, always in system prompt), facts.md (append-only, distilled nightly), projects/, daily/, inbox.md. SQLite holds transcript + FTS5 index re-synced on write. Writes happen through Claude tool-use: `remember_fact`, `update_persona_section`, `write_project_note`, `search_memory`.

## Revisit when
- Multi-user (Phase 4): Postgres, real auth, per-tenant encryption
- Corpus > ~2k facts or retrieval misses felt: embeddings
- iOS demand: cross-platform reassessment
