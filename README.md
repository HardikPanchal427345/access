# AdMint Finance Toolkit

AdMint Finance Toolkit is a production-focused Android finance utility app with strong ad monetization potential and modern UX.

## Why this app can monetize well

Finance utility traffic (EMI/GST/discount/tip users) generally has stronger ad intent and better ad rates than broad entertainment utilities.

## Included features

- Tip + split calculator
- Discount calculator
- EMI calculator
- GST calculator
- Modern dark UI with gradients, subtle glow, and minimal glassmorphism
- AdMob monetization:
  - banner ad placement
  - interstitial frequency control (every 4 calculator actions)
- Consent-aware ads with Google UMP (privacy flow)
- In-app privacy policy dialog + external policy URL support

## Tech stack

- Kotlin
- Jetpack Compose (Material 3)
- Google Mobile Ads SDK
- Google User Messaging Platform (UMP)

## Build

```bash
./gradlew :app:assembleDebug
```

## Release build (AAB)

```bash
./scripts/build_release_bundle.sh
```

Output bundle:

```text
app/build/outputs/bundle/release/app-release.aab
```

## AdMob production IDs

The project defaults to official Google test IDs for safe testing.

For production, set these in `gradle.properties`:

```properties
ADMOB_APP_ID=ca-app-pub-xxxxxxxxxxxxxxxx~yyyyyyyyyy
ADMOB_BANNER_UNIT_ID=ca-app-pub-xxxxxxxxxxxxxxxx/zzzzzzzzzz
ADMOB_INTERSTITIAL_UNIT_ID=ca-app-pub-xxxxxxxxxxxxxxxx/aaaaaaaaaa
PRIVACY_POLICY_URL=https://your-domain.com/privacy-policy
```

If these are not set, test IDs remain active.

## Play Store metadata

Prepared drafts are available at:

- `docs/playstore/listing.md`
- `docs/playstore/release-notes.md`

## Privacy policy

- In-app text source: `app/src/main/res/raw/privacy_policy.txt`
- External URL: `BuildConfig.PRIVACY_POLICY_URL` via `PRIVACY_POLICY_URL` property

## Notes

This cloud environment does not include Android SDK installation, so local/emulator builds may require SDK setup (`ANDROID_HOME` or `local.properties`).
