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
   contract file, date of birth, CNAS, etc. — see `accounts/models.py`)
   beyond what a field rep would typically view about themselves.
   **Confirmed** (this pass, not guessed): `accounts/templates/accounts/profile.html`
   and `edit_profile.html` — the only self-service profile pages a
   `Commercial`/`Medico_commercial` rep can reach — render none of those
   HR/financial fields (grepped for every one of them: zero matches in
   either template). The Django admin, which *does* expose them via
   `UserProfileInline`, requires `is_staff=True`, which field reps don't
   have. So: HR/financial fields are **not** reachable by the roles this
   app is for, in the paths that exist today. (Standard caveat: this
   covers the app as it exists now, not future template changes — worth a
   quick re-check if a new profile-related page is ever added.)

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
| **Nuance** | Android's on-device speech recognizer may, depending on the device/OS version, route audio through Google's own platform-level speech service outside this app's control — that's OS behavior, not something this app initiates or can inspect. Play's Data Safety guidance treats this as "audio not collected by the app" when the app itself never receives/stores audio, so answer **"No"** to "Does your app collect or share audio data" in the form — the app genuinely doesn't. **Suggested optional note**, if Play Console's form gives you a free-text field anywhere in that section: *"Voice input uses the device's built-in OS speech-to-text; this app receives and stores only the resulting text, never raw audio."* This isn't a required disclosure, just a defensive note in case of a future review question. |
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
| **Shared with third parties** | Yes — the token round-trips through **Firebase Cloud Messaging (Google)**, `notifications/utils.py` calls `firebase_admin.messaging.send()` against Firebase project `lilium-mobile`. This is standard FCM usage for delivering the notification, not used for advertising or analytics. |
| **Purpose category in the form** | Checked live against Play Console's current Data Safety help page (support.google.com, Aug 2026): the purpose options for "Device or other IDs" are *App functionality*, *Analytics*, *Developer communications*, *Advertising or marketing*, *Fraud prevention/security/compliance*, *Personalization*, *Account management*. For this token, select **"App functionality"** only — it's used solely to deliver in-app-relevant notifications (order status, plan validation, leave requests), not analytics/ads/personalization. |
| **Required or optional** | Optional — the runtime notification permission prompt can be dismissed; the rest of the app works without it. |

## Personal info (account/profile)

| | |
|---|---|
| **Collected** | Name, email, phone number, address, profile photo, company/role/region assignment — standard fields on the existing `UserProfile` model that the same web login already uses. |
| **Why** | Account authentication and role-based access to the company's field-force management system (this is an internal employee tool, not a consumer-facing app with a public signup flow). |
| **Shared with third parties** | No third-party auth provider — Django's own session-based login, authenticating by username or phone number + password (`accounts/backends.UsernameOrPhoneBackend`). (Correction from an earlier draft: this project's `EmailBackend` class exists in the same file but isn't actually registered in `AUTHENTICATION_BACKENDS` — login is not email-based.) |
| **Required or optional** | Required — the entire app is behind login. |

## Reviewer / demo account for App Store & Play Store review

A dedicated `appreview@liliumpharma.com` account exists for this purpose —
not a real employee, `speciality_rolee=Medico_commercial` with no
`usersunder` and no assignments beyond two synthetic records (a demo
"doctor" and a demo "pharmacy") created solely for it. Verified empirically
(not just by reading the scoping code) against the live database: this
account's visible doctor/report/order/plan lists contain only those two
synthetic records — zero real HCPs, clients, visit history, or orders.
Credentials were handed directly to the app owner in chat, not stored in
this repo — paste them into App Store Connect / Play Console's own
reviewer-notes field when submitting.

## What this app does **not** do
- No advertising ID collection, no ad SDKs.
- No analytics SDK bundled intentionally — the only third-party native dependency pulled in for data purposes is Firebase Cloud Messaging (push only). **Checked**: this server has no Java/Android SDK, so `./gradlew :app:dependencies` couldn't be run to fully resolve the transitive dependency graph. As a static check instead, I grepped every `.gradle` file in this repo (including the `@capacitor/push-notifications` plugin's own `build.gradle` inside `node_modules`) and `google-services.json` for `firebase-analytics`, `firebase-bom`, and `analytics` — zero matches. The plugin declares exactly one Firebase dependency, `com.google.firebase:firebase-messaging:24.1.0`, no BOM. `google-services.json` also has no `analytics_service` block (Firebase configs with Analytics enabled include one). This is strong evidence no Analytics dependency is present, but isn't a substitute for the real `gradlew` resolution — **TODO (optional, low-risk)**: if you want full certainty, run `./gradlew :app:dependencies` on a machine with the Android SDK before submission.
- No background location tracking.
- No data sale.

## Data deletion / retention
Play's form also asks whether users can request data deletion. This app has no self-service "delete my account" flow — confirmed no such view/endpoint exists anywhere in the backend.

**What the code actually does on offboarding** (`accounts/models.py`, `UserProfile.save()`): setting a `contract_end_date` on an employee's profile automatically flips `User.is_active = False` and force-revokes their login — their DRF token is deleted and every active Django session is killed immediately (`_force_logout()`). So there **is** a real, code-level deactivation mechanism tied to contract end date — this is presumably what "offboarding" already means operationally here, not a manual DB edit.

What this doesn't tell us: whether that's actually the process someone follows today (does HR set `contract_end_date` in the admin when someone leaves, or is deactivation done some other way?), and whether deactivation is treated as equivalent to "deletion" for Play's purposes, or whether records are ever hard-deleted/purged afterward. **TODO (needs your input, not derivable from code)**: confirm (a) is `contract_end_date` actually the real offboarding trigger in practice, (b) is deactivation-only considered sufficient, or do you also purge data after some retention period, (c) is there a process for an employee to explicitly *request* deletion rather than just being offboarded.
