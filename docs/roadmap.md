# TWYN Roadmap — One Page

**Started:** August 2026 · Tracker updated as milestones land.

## Phase 0 — Companion v0 (M1-2) `IN PROGRESS`
- [x] Server core: FastAPI + SSE chat + SQLite
- [x] Memory engine: markdown + FTS5 + agent tools
- [x] Interview mode → TWIN.md persona
- [x] Deployed on VPS, HTTPS (twin.bconclub.com)
- [x] Android app: chat + voice, APK built (sideload pending)
- [x] Widget + quick tile built (verify on device)
- [ ] Founder 7-day daily-use streak

## Phase 0.5 — Call Taker (M2-3) `NEXT` · see [ADR 0002](adr/0002-call-taker.md)
The first chore TWYN removes: inbound calls. Twin screens, declines with context, and
keeps track of who to call back — in your voice, with your judgment.
- [ ] Plane A: `CallScreeningService` spam label/block (number + STIR/SHAKEN + reputation)
- [ ] Plane A: "Let Twin handle it" decline → context-aware SMS via server (persona+memory)
- [ ] Plane A: spam "mock mode" sarcastic auto-reply
- [ ] Plane A: caller + callback logged to memory; surfaced in morning brief
- [ ] Plane B (CORE): Twin number on a SIP trunk/provider + conditional call-forwarding
- [ ] Plane B: contacts sync → Twin knows who's calling (name/relationship/notes)
- [ ] Plane B: voice loop (STT → Claude+contacts → TTS) answers + writes call summary/callback
- [ ] Metric: 90% of spam auto-handled; every real caller logged; founder phone-triage time down

## Phase 1 — Exocortex (M3-6)
- [ ] Gmail + Calendar ingestion
- [ ] Nightly summarizer + morning brief (FCM)
- [ ] Embeddings retrieval (sqlite-vec + Voyage)
- [ ] Voice clone TTS out
- [ ] "What should I do today" passes 5/7 days

## Phase 2 — Public Twin (M7-10)
- [ ] Content pipeline with approval gate
- [ ] Avatar video + cloned voice stack
- [ ] 3 posts/week × 12 weeks
- [ ] Landing page + waitlist live
- [ ] 10k aggregate followers, 500 waitlist

## Phase 3 — Business Operator (M11-14)
- [ ] Business data ingestion (CRM, projects, finance summary)
- [ ] Workflow engine + approval queues + audit log
- [ ] One business function delegated end-to-end
- [ ] 10 hrs/week founder time saved, measured

## Phase 4 — Productize (M15-17)
- [ ] Multi-tenant backend (Postgres, per-tenant encryption)
- [ ] Productized interview onboarding (stranger → twin in 1 hr)
- [ ] Pricing live, beta cohort onboarded
- [ ] 10+ external daily-used twins, first revenue

## Beyond — Embodiment (2028+)
- [ ] R&D scoping doc: platforms, partners, costs
