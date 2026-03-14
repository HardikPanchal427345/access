# AdMint Finance Toolkit (Play Store Ready Starter)

AdMint Finance Toolkit is a high-retention, ad-monetized utility app with a modern premium UI:

- Dark mode first design
- Gradient background and subtle glow
- Minimal glassmorphism cards
- Built-in AdMob banner + interstitial flow
- Practical daily tools:
  - Tip + bill split calculator
  - Discount calculator
  - EMI loan calculator
  - GST calculator

This niche generally gives better ad CPC/eCPM than generic apps because users are in a finance intent flow.

---

## Tech Stack

- Kotlin
- Jetpack Compose (Material 3)
- Google Mobile Ads SDK
- Android minSdk 24 / targetSdk 35

---

## Quick Start

```bash
./gradlew assembleDebug
```

Open in Android Studio and run on a device/emulator.

---

## Ad Monetization Setup (Important)

The app currently uses official Google **test ad IDs** so it is safe to run and review.

Before release:

1. Create your AdMob app and ad units.
2. Replace values in:
   - `app/build.gradle.kts` -> `manifestPlaceholders["admobAppId"]`
   - `app/src/main/res/values/strings.xml`
     - `admob_banner_unit_id`
     - `admob_interstitial_unit_id`
3. Keep test IDs in debug builds while testing.

---

## Play Store Production Checklist (1-hour focused path)

1. **Build release AAB**
   - Build > Generate Signed Bundle / APK > Android App Bundle
2. **Create Play Console listing**
   - App name, short/full description, category = Finance
3. **Upload screenshots**
   - Home + each calculator tab + premium dark UI screen
4. **Data safety + ads disclosure**
   - Mark that app serves ads
5. **Content rating + app access form**
6. **Upload AAB to production or closed testing**
7. **Submit for review**

---

## Icon Assets

Adaptive icon resources are included:

- `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- legacy vector fallback:
  - `app/src/main/res/mipmap-anydpi/ic_launcher.xml`
  - `app/src/main/res/mipmap-anydpi/ic_launcher_round.xml`

If you want a branded icon pack (Play Store 512x512 + feature graphic), generate from this base in Android Studio Image Asset Studio or Figma.
