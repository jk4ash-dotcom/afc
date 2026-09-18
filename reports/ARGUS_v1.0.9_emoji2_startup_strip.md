# Argus v1.0.9-poc — EmojiCompat / Startup / ProfileInstall strip proof

**versionName:** `1.0.9-poc` · **versionCode:** `10`

Same offline security anti-patterns checklist as Tanakh v0.4.3 / v0.4.4.

## Controls

1. Manifest `tools:node="remove"` on empty `androidx.startup.InitializationProvider` (strips `EmojiCompatInitializer` + `ProfileInstallerInitializer` meta-data with it).
2. Manifest `tools:node="remove"` on `androidx.profileinstaller.ProfileInstallReceiver`.
3. Gradle `configurations.configureEach { exclude ... }` for `androidx.emoji2:emoji2|emoji2-views|emoji2-views-helper` and `androidx.profileinstaller:profileinstaller`.
4. Dropped `debugImplementation` `ui-tooling` + `ui-test-manifest` (no exported PreviewActivity).
5. `android:allowBackup="false"`.
6. **No** `INTERNET` permission.
7. Ship primary = **release** APK (`debuggable=false`), signed with local debug keystore for POC sideload.

Primary Argus HIGH control: EmojiCompatInitializer cannot auto-run / trigger GMS downloadable emoji-font fetch.

## Proof greps (must be 0)

| Artifact | EmojiCompatInitializer | InitializationProvider | ProfileInstall* | PreviewActivity | INTERNET |
|----------|------------------------|------------------------|-----------------|-----------------|----------|
| merged_manifests/debug | 0 | 0 | 0 | 0 | 0 |
| merged_manifests/release | 0 | 0 | 0 | 0 | 0 |
| packaged_manifests/debug | 0 | 0 | 0 | 0 | 0 |
| packaged_manifests/release | 0 | 0 | 0 | 0 | 0 |
| release APK `aapt dump xmltree` | 0 | 0 | 0 | 0 | 0 |
| debug APK `aapt dump xmltree` | 0 | 0 | 0 | 0 | 0 |

Dex note (same as Tanakh): Compose UI still has optional soft string refs to `Landroidx/emoji2/text/EmojiCompat;` / `EmojiCompatStatus` for reflective "is loaded?" checks. **`EmojiCompatInitializer` ABSENT** from dex. No `emoji2` / `profileinstaller` artifacts on `releaseRuntimeClasspath`.

`allowBackup="false"` confirmed in merged debug+release manifests.

## Manifest permissions (release)

Only signature-level:

- `com.afcpoc.prayer.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION`

No `android.permission.INTERNET`.

## APKs

| Variant | debuggable | versionCode / versionName | SHA-256 |
|---------|------------|---------------------------|---------|
| **release (primary)** | false | 10 / 1.0.9-poc | `bcdfe4faf13a95b0466ac1251eadc157bc6cfb408dbfcd47a35b96d95436944f` |
| debug (optional) | true | 10 / 1.0.9-poc | `63f6d9453d42542656c7e02ba698ee0d7a3ebac47261268f6662b99f091e1bf8` |

About screen uses `BuildConfig.VERSION_NAME` → shows `1.0.9-poc`.

## Signing

Release signed with local Android debug keystore (`signingConfig = debug`) — not a Play App Signing upload key. Play Protect may still warn on sideload until a real keystore.
