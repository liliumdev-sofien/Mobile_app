# Privacy Policy — Lilium Pharma (mobile app)

*Last updated: August 29, 2026*

---

## Who this applies to

This app is an internal tool for **EURL Lilium Pharma Algérie**'s
field-force employees (sales representatives, supervisors, and office
staff) — it is not a public consumer app, and there is no public account
signup; access requires an employee account already provisioned in Lilium
Pharma's system. This policy is governed by Algerian law; no GDPR- or
CCPA-specific provisions apply.

**EURL Lilium Pharma Algérie**
Lot communal C, lot n°123, 2ème étage, Draria, Alger, Algérie

## Data we collect and why

### Account information
Your name, email address, phone number, and role/region assignment,
collected when your account is provisioned and used to authenticate you
and control what parts of the system you can access. This is the same
account data already used by the existing `app.liliumpharma.com` web
platform — the app does not collect anything new here beyond what logging
into the website already requires.

### Location
When you tap "Utiliser ma position actuelle" while editing a medical/
pharmacy contact's record, the app reads your device's current GPS
position (once, on that tap — not continuously or in the background) and
saves it as that contact's office location in our system. This records
*where the business you're visiting is located*, not a log of your own
movements. Using this feature is optional.

### Microphone / voice dictation
The app offers a voice-to-text option for filling in visit notes. Your
device's built-in speech recognition converts speech to text; the app
receives and stores only the resulting text, the same as if you had typed
it. We do not record, store, or transmit audio.

### Camera / document scanning
The app can scan physical documents (e.g. paperwork related to an order
or visit report) using your device's camera. Scanning is processed on
your device; the resulting image is uploaded and stored on our own
servers as an attachment to the relevant record. Using this feature is
optional and happens only when you choose to scan something.

### Push notification token
If you allow notifications, your device is registered for push
notifications via Firebase Cloud Messaging (a Google service) so we can
notify you about things relevant to your role — order status changes,
approval requests, leave requests, etc. We use this token only to deliver
those notifications, not for advertising or tracking.

## How we store and protect your data

All data described above is stored on servers we operate and control
(PostgreSQL database and file storage), not on third-party cloud storage.
All communication between the app and our servers is encrypted in transit
(HTTPS) — the app only ever talks to `https://app.liliumpharma.com`.

## Third parties we share data with

- **Firebase Cloud Messaging (Google)** — receives your device's push
  notification token solely to deliver notifications on our behalf.
  Google's own privacy policy governs their handling of that token:
  https://policies.google.com/privacy
- **Google ML Kit** — processes document scans and (indirectly, via your
  device's OS) speech recognition *on your device*; we do not send that
  raw audio or image data to Google as part of this processing.

We do not sell your data, and we do not share it with advertisers.

## Data retention and deletion

There is no self-service "delete my account" option in the app. Account
access ends as part of Lilium Pharma's employee offboarding process: when
an employee's record is marked as having ended, their login is deactivated
and immediately revoked (active sessions and login tokens are invalidated
right away, not merely on next use). Data associated with a deactivated
account is retained indefinitely in our systems after offboarding; it is
not automatically purged after a fixed period, and there is currently no
separate self-service request process for an employee to have their data
deleted outright.

## Children's privacy

This app is not directed at children and is not available for public
signup — all accounts belong to adult employees.

## Changes to this policy

We'll update the "Last updated" date above whenever this policy changes,
and for material changes we'll notify you via the app's existing push
notification system (the same channel already used for order/plan/leave
alerts).

## Contact us

For privacy questions, contact us at **contact@liliumpharma.com**, or at
our registered address above.
