# Argus v1.0.10-poc — emoji2 classpath fix (launch crash)

**versionName:** `1.0.10-poc` · **versionCode:** 11

## Diff: known-good v1.0.8 → broken v1.0.9

v1.0.9 added Gradle excludes for `androidx.emoji2:*` + `profileinstaller` and
`tools:node="remove"` on the entire `InitializationProvider`. Result:

- emoji2 classes **gone** from APK (Compose still subclasses `InitCallback`)
- startup-runtime **gone** (was transitive via emoji2/profileinstaller)
- release installs but will not launch

v1.0.8 debug (James: still launches) retained emoji2 + Startup + provider with
`EmojiCompatInitializer` meta-data.

## Fix

Same pattern as Tanakh v0.4.5:

1. Do **not** exclude emoji2 (Compose needs classes).
2. Keep `InitializationProvider`; strip only `EmojiCompatInitializer` +
   `ProfileInstallerInitializer` meta-data.
3. Keep `ProfileInstallReceiver` remove + profileinstaller Gradle exclude.
4. Keep `allowBackup=false`, no `INTERNET`.

Argus HIGH (GMS emoji font auto-fetch) remains closed: initializer never runs.
