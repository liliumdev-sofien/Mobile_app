# Play Store readiness audit

Findings from a code-level pass on 2026-08-19/20, cross-referencing this
repo against the Django backend and current Play Store policy. Items are
either fixed directly (noted as such, see git log) or flagged for your
judgment — nothing here required touching signing, Play Console, or a
physical device.

## 1. Permission audit — all clean, nothing unjustified

Every permission in `AndroidManifest.xml` is actually exercised by real
app code; none are declared-but-unused (which Play's automated review
does flag):

| Permission | Justified by |
|---|---|
| `INTERNET` | The WebView loading `app.liliumpharma.com` at all |
| `CAMERA` | `@capacitor-mlkit/document-scanner`, used in `add_rapport.html`, `addorder.html`, `update_rapport.html` |
| `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` | `@capacitor/geolocation`, used in `medecins/edit_location.html` for medecin GPS capture |
| `RECORD_AUDIO` | `@capacitor-community/speech-recognition`, used in `add_visite.html` / `add_visite_commercial.html` |
| `POST_NOTIFICATIONS` | `@capacitor/push-notifications`, wired in `templates/push_notifications.html` |

No storage permissions (`WRITE_EXTERNAL_STORAGE` etc.) are declared and
none are needed — document scanning and file handling go through scoped,
app-private storage via the Filesystem plugin, not shared storage.

## 2. targetSdkVersion — ⚠️ action needed before submission, and the deadline is close

**Current state**: `android/variables.gradle` has `compileSdkVersion 35` /
`targetSdkVersion 35`.

**Current Play Store policy** (checked live, not from training data — see
sources): starting **August 31, 2026**, *new app submissions* must target
**Android 16 (API level 36)**, not 35. An extension to November 1, 2026 is
available for apps already in Play Console's pipeline, but the request
form wasn't available yet as of this check, and it's unclear whether a
first-time, not-yet-submitted app can use it at all — you should not
assume it applies to you.

Since a public Play Store search turned up **no existing live listing**
for `com.liliumpharma.app` earlier in our session, this is very likely a
first-time submission — meaning **35 will very likely be rejected** if you
submit after August 31, 2026, which is only days away from when this was
written.

**I did not bump this myself.** Raising the target API level isn't just a
number — Android enforces new default runtime behaviors at each target
level (this jump from 35→36 potentially affects predictive-back-gesture
handling, additional permission enforcement, and other platform defaults
I can't fully enumerate or verify without a real build). It also may
require newer Android Gradle Plugin / Gradle / androidx versions to even
compile, none of which I can verify without a real build+device test —
same reasoning as why `minifyEnabled` was left off. **Recommend**: bump
`compileSdkVersion`/`targetSdkVersion` to 36 on your machine, run a full
build, and test on a real device (or emulator) before your first
submission attempt — don't leave this until you're at the Play Console
upload step and discover the rejection then.

Sources: [Target API level requirements — Play Console Help](https://support.google.com/googleplay/android-developer/answer/11926878), [Meet Google Play's target API level requirement — Android Developers](https://developer.android.com/google/play/requirements/target-sdk)

## 3. Production config sanity check — clean

- `capacitor.config.json`: `server.url` is `https://app.liliumpharma.com`
  (production), `cleartext: false`, `allowNavigation` locked to that same
  domain. No debug/staging URL, no cleartext bypass.
- `google-services.json`: Firebase project `lilium-mobile`
  (project number `240116999569`), package name `com.liliumpharma.app`
  matches the app's `applicationId` exactly. Nothing about the project
  name suggests a test/dev project, but I can't inspect the Firebase
  Console itself (billing, environment labels) from here — worth a quick
  glance there yourself for full certainty.
- No `network_security_config.xml` present, and no
  `android:usesCleartextTraffic="true"` override in the manifest — the
  platform default (cleartext blocked) applies, consistent with
  `cleartext: false` above.

## 4. General cleanup — checked, nothing found needing a fix

- Adaptive icon: full set present (`ic_launcher_foreground`/`_background`
  across all densities, correct `mipmap-anydpi-v26` XML structure with
  16.7% inset) — compliant, no action needed.
- `strings.xml`: app name is "LiliumPharma" throughout, not a leftover
  Capacitor scaffolding placeholder.
- No stray `TODO`/`FIXME` comments or hardcoded `localhost`/private-IP
  URLs anywhere in the repo (grepped `.json`/`.gradle`/`.xml`/`.java`/`.ts`).
- `MainActivity` correctly has `android:exported="true"` (required since
  API 31 for a launcher activity); the `FileProvider` correctly has
  `android:exported="false"`.
- Unused plugins/permissions: already handled in the previous commit
  (`5b0194b`) — `capacitor-native-biometric`, `capacitor-secure-storage-plugin`,
  `@capacitor-mlkit/barcode-scanning` removed along with their now-dead
  `USE_BIOMETRIC` permission and ML Kit `barcode_ui` manifest tag.

## What's genuinely left for you (judgment calls, not guesses)

1. **targetSdkVersion 36 bump + real-device verification** (above) — the
   single most time-sensitive item.
2. Fill in the `[TODO: ...]` placeholders in `docs/privacy-policy.md`
   (legal entity name, contact email, retention/deletion practice) and
   host it at a public URL before submission — say the word and I can add
   it as a page on `app.liliumpharma.com` once you've reviewed the content.
3. Review `docs/play-data-safety.md` against your actual role permissions
   — specifically whether HR/financial `UserProfile` fields are reachable
   by the Commercial/Medico_commercial roles who'll install this app,
   since the WebView isn't scoped to a subset of pages.
4. Everything else in the original Phase 3/4 checklist (Play Developer
   account, content rating questionnaire, store listing assets/screenshots)
   is unchanged from before this pass — none of it needed code changes.
