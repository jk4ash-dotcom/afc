# AFC Prayer Companion (POC)


**Package:** `com.afcpoc.prayer` · **versionName:** `1.0.10-poc` · **versionCode:** 11
Offline-first Android app (Kotlin + Jetpack Compose) for praying AFC prayers and the Holy Rosary.

> **Proof of concept for Apostolate for Family Consecration review — not an official AFC product.**

See also [NOTICE](NOTICE) and [CONTENT_SOURCES.md](CONTENT_SOURCES.md).

## Requirements

- Android Studio Ladybug (2024.2+) or newer recommended
- JDK 17
- Android SDK with compile SDK 35
- Emulator or device running API 26+

## Open & run

1. Open the project folder in Android Studio (`File → Open` → this directory).
2. Let Gradle sync (Studio will download the wrapper distribution and dependencies).
3. Select an emulator or device, then **Run** the `app` configuration.


## Install the release APK (quickest)

1. Download the **release** APK (`afc-prayer-companion-*-release.apk`, `debuggable=false`) from the latest [GitHub Release](https://github.com/jk4ash-dotcom/afc/releases).
2. On your Android phone: allow install from this source if prompted, then open the APK.
3. Or with a cable: `adb install -r afc-prayer-companion-*-release.apk`

Package id: `com.afcpoc.prayer`

## Command-line build

```bash
./gradlew assembleDebug
```

Expected debug APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

Install:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Package & SDK

| Item | Value |
|------|--------|
| Application ID | `com.afcpoc.prayer` |
| Display name | AFC Prayer Companion (POC) |
| Min SDK | 26 |
| Compile / Target SDK | 35 |
| UI | Material 3, Navigation Compose |
| JSON | kotlinx.serialization from `assets/` |

## v1 features

- Home sections: AFC Prayers, Rosary, About
- AFC prayer list → scrollable detail (offline JSON)
- Rosary: mysteries by day (incl. Luminous), guided bead-by-bead with Next/Previous & progress; **All For after each decade's Fatima** (AFC practice, always); optional AFC after-Rosary overlay (St. Joseph, Act of Contrition — HHQ stays in Closing only; All For is per-decade, not end-only)
- Calm, family-friendly UI with large readable type
- No network calls for core prayer content
- No audio

## Deferred (not in this POC)

- Audio / spoken prayers
- Accounts / sync
- Push notifications
- Official AFC branding / assets
- Play Store signing & release pipeline
- Non-English localization

## Content attribution

Bundled assets under `app/src/main/assets/`:

- `afc_prayers.json`
- `rosary.json`

Sources, URLs, and known gaps are documented in [CONTENT_SOURCES.md](CONTENT_SOURCES.md) (copied from the content pack’s `SOURCES.md`). Texts are transcribed from AFC (afc.org) and USCCB; wording was not invented for doctrine.

## Project layout (high level)

```text
afc-prayer-companion/
  app/src/main/
    assets/           # offline prayer JSON
    java/com/afcpoc/prayer/
      MainActivity.kt
      data/           # models + asset loaders
      navigation/     # NavHost routes
      ui/screens/     # Compose screens
      ui/components/  # guided flow scaffold
      ui/theme/       # Material 3 theme
    res/
  CONTENT_SOURCES.md
  NOTICE
  README.md
```
