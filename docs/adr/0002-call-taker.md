# ADR 0002 — Call Taker (TWYN's first "chore" product)

**Status:** Proposed · **Date:** 2026-08-24

## Context

TWYN's thesis is a twin that IS you — your memory, voice, and judgment — applied to
**normal life**, not your work or coding. The first product should remove a real,
universal chore. Inbound phone calls are that chore: people get calls they don't want
to take, spam they don't want to hear, and context they lose when they decline.

Target experience: instead of "answer / decline," add **"let Twin take it."** Twin
handles the caller, keeps the context, decides who's worth a callback, and for spam runs
a sarcastic **mock mode**. Functionality is added one call-handling capability at a time.

## The platform constraint (the crux)

A third-party Android app **cannot** answer a live cellular (PSTN) call and converse as
the user. Verified against current platform behavior (Android 15/16, 2026):

- Capturing the remote party's audio requires the `VOICE_CALL` / `VOICE_UPLINK` /
  `VOICE_DOWNLINK` sources, which need `CAPTURE_AUDIO_OUTPUT` — reserved for system
  components since Android 10. Non-privileged apps only get the local mic.
- There is **no public API to inject synthesized audio into a live PSTN call.**
- `CallScreeningService.setShouldScreenCallViaAudioProcessing()` (the in-call audio hook)
  is `@SystemApi`, needs `CAPTURE_AUDIO_OUTPUT`, and only works when the app shares the
  system dialer's UID.
- Google's Pixel "Call Screen" does this only because it is privileged and region-gated.
  Root / Shizuku impersonation exists but is fragile and not distributable.

What a third-party app **can** do:

- `CallScreeningService` (`ROLE_CALL_SCREENING`): before ringing, allow / reject / silence
  a call, skip the call log, and read the caller number + carrier verification status
  (STIR/SHAKEN) for spam detection.
- Respond-via-message: send an SMS when declining a call.
- Be the default dialer (`InCallService` + `ROLE_DIALER`) to answer/end calls
  programmatically — but still with **no** access to call media.
- Carrier **conditional call forwarding** (forward-when-busy / -unanswered) to another
  number, configured with MMI codes / carrier settings.
- Fully own the media of **VoIP calls the app itself places or receives**, or that a
  telephony provider bridges server-side.

Conclusion: "Twin literally picks up your ringing phone and talks" is impossible for a
shippable app. Forwarding to a Twin-owned VoIP number delivers the identical user value
inside the rules.

## Decision — two-plane architecture

### Plane A — On-device Screen & Decline (ships first, no phone number)
- `CallScreeningService` labels/blocks spam using number + verification status and a
  server/reputation check.
- Incoming-call action **"Let Twin handle it"** → decline, then Twin (server, using
  persona + memory) generates a context-aware SMS reply. Spam → **mock mode** sarcastic
  reply templates.
- Every screened/declined call is written to Twin memory as a **caller + callback**
  record; the morning brief surfaces "who called, what they wanted, who to call back."
- Reuses existing pieces: `app/voice/Speech.kt`, `TwinApi.kt`, and the server memory
  tools (a new `log_caller` / `queue_callback` alongside `remember_fact`).

### Plane B — Twin Number (the real "Twin takes the call")
- Provision a dedicated number via a telephony provider (Twilio / Plivo / Telnyx).
- User sets conditional call-forwarding to it, or hands it out directly.
- Provider voice webhook drives the server media loop: **STT → Claude (persona+memory) →
  TTS**. Twin converses, captures intent, and writes a structured call summary + callback
  task into memory. SMS path uses the provider's messaging API.
- Inbound VoIP surfaced in-app later via FCM high-priority **data** messages + Jetpack
  Core-Telecom `CallsManager` (2026 recipe); requires OEM battery-optimization
  whitelisting UX.

## Consequences

- **New dependencies:** telephony provider account + number (~$1/mo + usage); STT
  (Deepgram / Whisper); TTS (ElevenLabs — already planned for the Phase 1 voice clone, so
  Twin can answer *in the user's cloned voice*).
- **Privacy / legal:** call-recording and AI-agent disclosure laws vary (two-party-consent
  regions). Twin must disclose ("You're speaking with X's TWYN") and behavior must be
  region-configurable. This is on-brand: transparency is a stated TWYN principle.
- **Reliability:** OEM battery optimization (Samsung "Sleeping Apps", Xiaomi, etc.) can
  kill screening/FCM; onboarding must guide whitelisting.
- **Positioning:** distinct from Google Call Screen (privileged, generic), Truecaller
  (spam ID only), and AI receptionists like Rosie/Numa (business-only). TWYN answers as
  **you**, for **personal** life, with your memory and judgment.

## Revisit when
- Android opens call audio to screening apps, or we obtain privileged/OEM status.
- An RCS Business Messaging path beats SMS for the decline-reply flow.
- Multi-user (Phase 4): per-tenant numbers, provider billing, and consent config.
