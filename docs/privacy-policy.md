# Privacy Policy — Lilium Pharma (mobile app)

**Draft for your review — not yet published anywhere.** Once you're happy
with it, it needs to be hosted at a public URL (Play Console requires a
live link, not a file in a repo) — I can wire this up as a page on
`app.liliumpharma.com` if you'd like, once you've reviewed the content.

Placeholders you need to fill in before this is publish-ready, marked
**[TODO: ...]** throughout: legal company name/address, a contact email
for privacy questions, and your jurisdiction (affects which sections, if
any, need GDPR/CCPA-specific language).

---

## Who this applies to

This app is an internal tool for Lilium Pharma's field-force employees
(sales representatives, supervisors, and office staff) — it is not a
public consumer app, and there is no public account signup; access
requires an employee account already provisioned in Lilium Pharma's
system.

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
[TODO: add a line about encryption in transit (HTTPS — already true, the
app only ever talks to `https://app.liliumpharma.com`) and at rest if
that's something your infrastructure guarantees and you want stated
publicly.]

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

There is no self-service "delete my account" option in the app — confirmed
no such feature exists in the codebase. Account access ends as part of
Lilium Pharma's employee offboarding process: when an employee's record is
marked as having ended, their login is deactivated and immediately revoked
(active sessions and login tokens are invalidated right away, not merely
on next use).

[TODO — needs your input, this is a policy/process question I can't answer
from code alone: (a) is offboarding-triggered deactivation the actual
process an employee's data deletion goes through today, or is there a
separate step; (b) how long is data retained after an account is
deactivated — indefinitely, or purged after some period; (c) is there any
path for an employee to explicitly request their data be deleted rather
than just deactivated. State whatever is actually true operationally.]

## Children's privacy

This app is not directed at children and is not available for public
signup — all accounts belong to adult employees.

## Changes to this policy

We'll update the "Last updated" date below whenever this policy changes,
and for material changes we'll notify you via the app's existing push
notification system (the same channel already used for order/plan/leave
alerts).

[TODO: this is a suggested default using infrastructure that already
exists in the app (push notifications) — replace with your actual practice
if you'd rather notify a different way, e.g. email or a supervisor
announcement instead.]

## Contact us

[TODO: contact email/address for privacy questions.]

---
*Last updated: [TODO: date this is actually published]*
