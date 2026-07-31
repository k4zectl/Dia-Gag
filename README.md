# Dia-Gag

An [Xposed](https://github.com/LSPosed/LSPosed) module for the **My Dialog** app
(`net.omobio.dialogsc`) that neutralises its root / debug / integrity checks so
the app runs on rooted or modded devices.

Written from scratch for the current My Dialog release (18.6.1).

## Build

```
./gradlew assembleRelease
```

Output APK: `app/build/outputs/apk/release/`.

## Install

1. Install the built APK.
2. Enable **Dia-Gag** in LSPosed and set its scope to **My Dialog**.
3. Force-stop and reopen My Dialog.

## Author

Built and maintained by **K4ZE DEV** (GitHub [@k4zectl](https://github.com/k4zectl)).

## License

Licensed under the [GNU GPL v3.0](LICENSE). Copyright © 2026 K4ZE DEV.
