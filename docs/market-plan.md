# TWYN — 17-Month Market Plan

**Start:** August 2026 · **Horizon:** December 2027 (Month 17)
**One line:** An exocortex digital twin. Not an assistant that serves you, a clone that IS you: your memory, your voice, your judgment, plus everything you don't know.

---

## The Thesis

Everyone wanted a twin. An exact copy with all your strengths, none of your weaknesses (or your weaknesses too, if you want honesty). Jarvis was a butler. TWIN is you, in and out. It knows you completely and it knows the world beyond you, so it has more perspective than you on your own life.

The wedge: build ONE twin first, the founder's, and make it undeniably real. Dogfooding is the product demo, the marketing engine, and the moat. Every phase makes the founder's twin more capable in public, and the audience watching becomes the waitlist.

**End state at Month 17:** the founder's twin runs meaningful parts of his life and business, has a public face, and 10+ external people have live twins of their own. Physical embodiment is scoped, not built.

---

## Phase 0 — Companion v0 (Months 1-2)

The twin exists. Phone-native, widget-fast, remembers everything.

**Goals**
- Android app on founder's phone: one tap from home screen to talking twin
- Persistent memory: persona doc (TWIN.md), facts, projects, daily summaries
- Persona seeded via structured interview

**Deliverables**
- Kotlin/Compose app (chat, voice, Glance widget, quick tile), sideloaded APK
- FastAPI brain on Hostinger VPS, Claude-powered, markdown memory + FTS5 retrieval
- Interview flow producing TWIN.md

**Metrics**
- Founder talks to twin daily (7-day streak by end of M1)
- Widget tap to visible response: under 3 seconds
- Twin correctly recalls facts told 1+ week earlier: 90%+

**Risks:** latency kills habit (mitigate: SSE streaming, lean context); memory bloat (nightly distillation)
**Cost:** ~$30-80/mo (Claude API + existing VPS). No hires.

---

## Phase 1 — Exocortex (Months 3-6)

The twin stops waiting to be asked. It ingests the founder's digital life and becomes proactive.

**Goals**
- Data ingestion: Gmail, Google Calendar, notes, WhatsApp exports, browsing highlights
- Proactive: morning brief, deadline nudges, "you forgot X" via push notifications
- Voice out (TTS in founder's cloned voice via ElevenLabs or similar)
- Twin executes personal workflows: draft replies, summarize threads, schedule

**Deliverables**
- Ingestion pipelines (Gmail/Calendar APIs already connected via MCP-style integrations)
- Embeddings retrieval (sqlite-vec + Voyage) once corpus outgrows FTS5
- FCM proactive engine with quiet hours
- Voice clone + TTS pipeline

**Metrics**
- Twin answers "what should I do today" correctly (founder-judged) 5 days out of 7
- 50%+ of proactive pings rated useful (thumbs in app)
- 1,000+ memory facts, retrieval precision holding

**Risks:** privacy blast radius grows (mitigate: VPS hardening, encrypted backups, ingestion allowlist); proactive spam kills trust (strict quiet hours, usefulness feedback loop)
**Cost:** ~$100-200/mo (API + TTS + embeddings). No hires.

---

## Phase 2 — Public Twin (Months 7-10)

The twin gets a face and an audience. This is the brand engine.

**Goals**
- Twin posts on social media (X/Twitter, Instagram, YouTube Shorts/TikTok) in founder's voice
- AI video: founder's likeness + cloned voice, short-form content
- Build-in-public narrative: "I cloned myself, here's what happened" is the content

**Deliverables**
- Content pipeline: twin drafts → founder approves → scheduled post (approval gate is non-negotiable in this phase)
- Video generation stack (likeness LoRA / HeyGen-class avatar + ElevenLabs voice)
- Weekly show format: twin and founder discuss the week
- twin landing page + waitlist

**Metrics**
- 3+ posts/week sustained for 12 weeks
- 10k followers aggregate across platforms by M10
- 500+ waitlist signups
- 1 viral moment (100k+ views) demonstrating the twin is real, not a chatbot skin

**Risks:** deepfake/authenticity backlash (mitigate: radical transparency, every post labeled as twin, founder on camera explaining); platform bans on AI content (label compliance, human approval gate)
**Cost:** ~$300-500/mo (video gen + tools). **Hiring trigger:** if content cadence slips 2 weeks running, hire part-time editor (first hire).

---

## Phase 3 — Business Operator (Months 11-14)

The twin earns its keep. From knowing the business to running parts of it.

**Goals**
- Twin drafts client communications, tracks projects, chases invoices, triages inbox to zero
- Agent workflows: twin spawns task agents (research, QA, reporting) and reviews their output
- Founder delegates a defined business function end-to-end (e.g. client onboarding comms)

**Deliverables**
- Business context ingestion: client CRM data, project trackers, financials summary
- Workflow engine: recurring jobs, approval queues, escalation to founder
- Audit log: every action the twin took and why

**Metrics**
- 10+ hours/week of founder time measurably saved (time-tracked before/after)
- 90%+ of twin-drafted client comms sent with zero or minor edits
- Zero client-facing incidents caused by the twin

**Risks:** an autonomous mistake in front of a client (mitigate: approval gates on all external sends until 3 months of clean drafts; audit log); over-delegation atrophies founder judgment (weekly review ritual)
**Cost:** ~$300-600/mo. **Hiring trigger:** revenue attributable to twin-freed hours funds hire #2 (ops/engineer) to harden the platform for Phase 4.

---

## Phase 4 — Productize (Months 15-17)

From "his twin" to "your twin." The audience becomes users.

**Goals**
- Multi-tenant architecture: isolated memory, per-user auth, billing
- Onboarding = the interview flow, productized: a stranger gets a working twin in under 1 hour
- Closed beta from waitlist

**Deliverables**
- Multi-user backend (Postgres, per-tenant encryption, proper auth)
- Play Store release (or direct APK + iOS TestFlight scoping)
- Pricing live: see Monetization below
- Beta cohort onboarded, feedback loop running

**Metrics**
- 10+ external users with live, daily-used twins by M17
- Week-4 retention of beta cohort: 50%+
- First revenue (any amount; proves willingness to pay)
- NPS from beta: 40+

**Risks:** what works for a technical founder fails for normies (mitigate: white-glove onboarding for first 10, watch every session); privacy/regulatory exposure with user data (per-tenant encryption, data export/delete from day one, no training on user data)
**Cost:** ~$1-2k/mo infra + team. **Hiring trigger:** beta demand over 100 waitlist activations justifies raising or revenue-funding hires #3 (mobile) and beyond.

---

## Beyond Month 17 — Physical Embodiment (R&D Scoping Only)

Not built in this plan. Scoped so the vision has a costed path.

- **Reality check:** credible humanoid platforms (Unitree G1 ~$16k, Figure, 1X Neo) are becoming buyable; a TWIN embodiment is a software layer on someone else's hardware, not a robotics company. Building hardware solo is a capital incinerator; do not.
- **Nearer step:** a physical presence device (desk robot / smart display / wearable with camera+mic) as "twin's body v0.1" is achievable in 2028 at consumer-electronics cost.
- **Decision point M17:** if productized twin has traction, embodiment becomes a partnership/integration conversation (and a fundraise story), not a build.

---

## Market Landscape

| Player | What it is | Why TWYN is different |
|---|---|---|
| Replika, Character.ai | Fantasy companions, fictional personas | TWYN is a real person's clone with real data and real utility, not roleplay |
| Rewind / Limitless | Passive memory capture, search your life | Capture without agency; TWIN acts, speaks, and represents you |
| MindBank, Personal.ai | "Digital twin" memory vaults | Q&A archives, not operators; no phone-native speed, no business execution |
| Delphi | Clones of experts for their audiences | Closest neighbor; audience-facing only, doesn't run YOUR life or business |
| ChatGPT/Claude + memory | General assistants with memory features | Serve everyone identically; not YOU, no persona, no public face, no delegation |

**The gap TWYN occupies:** one system that is simultaneously (1) private exocortex, (2) public representative, (3) business operator, all as a single evolving persona. Nobody holds all three. The compounding asset is the persona + memory corpus, which deepens daily and cannot be copied by a competitor.

## Positioning + Moat

- **Positioning:** "Not an assistant. A twin." Assistants serve; twins ARE. Every marketing beat reinforces identity, not utility.
- **Moat 1, data depth:** months of accumulated persona + memory makes the twin unclonable by any new entrant for that user.
- **Moat 2, proof:** the founder's twin operating in public is a demo no slide deck matches.
- **Moat 3, brand:** first credible "real twin" story compounds; legitimacy and distinct voice are the 5-10 year assets. Every phase decision gets tested against: does this compound legitimacy, credibility, brand power, distinct voice?

## Monetization (activated Phase 4)

1. **Twin subscription:** $29-49/mo prosumer (exocortex + companion), $99-199/mo operator tier (business workflows). Primary.
2. **White-glove twin setup:** $1-5k one-time concierge cloning for founders/creators. High-margin early revenue, feeds case studies.
3. **Creator twins (later):** Delphi-style audience-facing twins with revenue share. Only after core product proves retention.
4. **Not doing:** ads, data sales, anything that erodes the trust the product depends on.

## Brand Strategy (5-10 Year Lens)

- **Name:** TWYN. Spelled with a Y: ownable, trademark-able, domain-gettable. Short, ownable, says the whole thesis.
- **Voice:** direct, confident, slightly provocative. The twin speaks like the founder: no corporate softness.
- **Transparency as brand:** always disclose what's twin vs human. In a deepfake-fatigued world, the honest clone is the differentiated one.
- **Content flywheel:** the build IS the content (Phase 2). The product journey is the marketing budget.
- **Legitimacy compounding:** ship real utility before spectacle. A twin that demonstrably runs a business beats a twin that just talks.

## Budget Summary (Bootstrap Path)

| Phase | Months | Run rate | Cumulative cash need |
|---|---|---|---|
| 0 Companion | 1-2 | ~$50/mo | ~$100 |
| 1 Exocortex | 3-6 | ~$150/mo | ~$700 |
| 2 Public | 7-10 | ~$400/mo + editor | ~$4k |
| 3 Operator | 11-14 | ~$500/mo + hire #2 | ~$15-25k (revenue-offset) |
| 4 Productize | 15-17 | ~$1.5k/mo + team | funded by revenue or raise |

Bootstrap through Phase 2 is fully feasible solo. Phases 3-4 hires are triggered by traction, not calendar.

## Kill / Pivot Criteria (Honesty Section)

- Phase 0-1: if founder himself stops using the twin daily for 2+ weeks, the product thesis is broken. Fix the product before any public phase.
- Phase 2: if 12 weeks of consistent content yields under 2k followers, the public-twin angle is weak; pivot marketing to pure utility/B2B operator story.
- Phase 4: if beta week-4 retention under 25%, do not scale; return to concierge model (option 2) and iterate.
