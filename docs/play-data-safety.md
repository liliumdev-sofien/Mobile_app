# Play Console Data Safety — draft answers

Drafted from the actual app code (this repo + the Django backend at
`/var/www/server`), not boilerplate. For you to review and transcribe into
Play Console's Data Safety form yourself — nothing here has been submitted
anywhere. Anything I couldn't verify from code is marked **TODO** rather
than guessed.

Scope note up front: this app is a Capacitor **WebView wrapper** around the
existing `app.liliumpharma.com` web platform, session-authenticated the
same way the website is. That means two categories of data collection:

1. **Native-permission-driven flows** — location capture, speech-to-text,
   document scanning, push notifications. These are fully mapped below
   with high confidence, since they're this repo's own explicit code.
2. **Whatever the authenticated web platform itself exposes** — since the
   WebView isn't scoped to a subset of pages, any page a logged-in user's
   role can reach in a browser is reachable in the app too. The backend's
   `UserProfile` model holds a full employee record (bank account, salary,
   contract file, date of birth, etc. — see accounts/models.py) beyond
   what a field rep would typically view about themselves. I have **not**
   audited which pages/roles can reach which fields — that's a
   page-by-page effort beyond this pass. **TODO**: confirm HR/financial
   fields aren't reachable by the roles who'll actually install this app
   (Commercial/Medico_commercial reps), and adjust the "Personal info"
   section below if they are.

---

## Location

| | |
|---|---|
| **Collected** | Yes — precise (fine) and approximate (coarse) location |
| **How** | `@capacitor/geolocation`, one-shot `getCurrentPosition()` call, triggered only by the user tapping "Utiliser ma position actuelle" on the medecin edit page (`medecins/templates/medecins/edit_location.html`). No background or continuous tracking (`watchPosition` is not used anywhere in this codebase). |
| **Why** | To record the GPS coordinates of a doctor's/pharmacy's office the field rep is visiting, for the company's own CRM (`Medecin.lat`/`Medecin.lon` in the backend). This is the location of the *business contact being visited*, not passive tracking of the app user. |
| **Required or optional** | Optional — a user can use every other feature without ever tapping the capture button. |
| **Shared with third parties** | No — stored only in Lilium Pharma's own PostgreSQL database. |
| **Processed ephemerally** | No — persisted server-side. |

## Audio

| | |
|---|---|
| **Collected** | Effectively no raw audio is collected/stored/transmitted **by this app's own code**. |
| **How** | `@capacitor-community/speech-recognition` wraps Android's native `SpeechRecognizer`. The app only ever receives the **transcribed text** via the `partialResults` listener (`rapports/templates/rapports/visites/add_visite.html`, `add_visite_commercial.html`) — there is no code path that captures, stores, or uploads raw audio bytes. |
| **Nuance** | Android's on-device speech recognizer may, depending on the device/OS version, route audio through Google's own platform-level speech service outside this app's control — that's OS behavior, not something this app initiates or can inspect. Play's Data Safety guidance generally treats this as "audio not collected by the app" when the app itself never receives/stores audio, but you may want to note the OS-level caveat in the form's audio section rather than answer a flat "no" — **TODO**: your call on exact phrasing here. |
| **Why (the resulting text)** | The transcribed text fills the same "Observation"/"Note" text fields as manual typing would — treated as regular form data from that point on. |
| **Shared with third parties** | No, from the app's side. |

## Photos / Documents

| | |
|---|---|
| **Collected** | Yes — scanned document images |
| **How** | `@capacitor-mlkit/document-scanner`, triggered manually per-field when a user chooses to scan a document (attaching evidence to a `Rapport` or `Order`) in `add_rapport.html`, `addorder.html`, `update_rapport.html`. Processing happens on-device via Google ML Kit — the image is not sent to Google as part of scanning. |
| **Why** | Attached as supporting documentation on the rapport/order the user is submitting. |
| **Required or optional** | Optional — a feature the user chooses to use per document, not required to use the app. |
| **Storage** | Uploaded to and stored on Lilium Pharma's own server (Django `MEDIA_ROOT`, local disk on this VPS — `liliumpharm/settings.py`). Not a third-party cloud storage bucket. |
| **Shared with third parties** | No. |

## Device / other identifiers (push notification token)

| | |
|---|---|
| **Collected** | Yes — an FCM (Firebase Cloud Messaging) registration token |
| **How** | `@capacitor/push-notifications` registers the device for push, the token is POSTed to the backend (`accounts/api/mobile/register-push-token/`) and stored in `UserProfile.notification_token`. |
| **Why** | Solely to deliver push notifications relevant to the user's role (new order status, plan-validation requests, leave requests, etc. — see `notifications/utils.py`). |
| **Shared with third parties** | Yes — the token round-trips through **Firebase Cloud Messaging (Google)**, `notifications/utils.py` calls `firebase_admin.messaging.send()` against Firebase project `lilium-mobile`. This is standard FCM usage for delivering the notification, not used for advertising or analytics. Play's Data Safety form has specific guidance for "app functionality" purpose disclosures for push tokens — **TODO**: confirm exact form wording against current Play Console category options when you fill it in, since this can shift between Play policy versions. |
| **Required or optional** | Optional — the runtime notification permission prompt can be dismissed; the rest of the app works without it. |

## Personal info (account/profile)

| | |
|---|---|
| **Collected** | Name, email, phone number, address, profile photo, company/role/region assignment — standard fields on the existing `UserProfile` model that the same web login already uses. |
| **Why** | Account authentication and role-based access to the company's field-force management system (this is an internal employee tool, not a consumer-facing app with a public signup flow). |
| **Shared with third parties** | No third-party auth provider — Django's own session/email-based login (`accounts/backends.py`). |
| **Required or optional** | Required — the entire app is behind login. |

## What this app does **not** do
- No advertising ID collection, no ad SDKs.
- No analytics SDK bundled intentionally — the only third-party native dependency pulled in for data purposes is Firebase Cloud Messaging (push only). **TODO**: if you want certainty on the full dependency tree (in case a transitive Firebase Analytics dependency snuck in), run `./gradlew :app:dependencies` on a machine with the Android SDK and check for `firebase-analytics`.
- No background location tracking.
- No data sale.

## Data deletion / retention
Play's form also asks whether users can request data deletion. This app has no self-service "delete my account" flow visible in the code I checked — deletion would go through whatever internal process Lilium Pharma already has for employee offboarding. **TODO**: confirm with whoever owns that process before answering this section of the form.
