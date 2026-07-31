<p align="center">
  <img src="docs/icon.png" width="128" alt="Dia-Gag icon">
</p>

<h1 align="center">Dia-Gag</h1>

<p align="center">
  An <a href="https://github.com/LSPosed/LSPosed">Xposed</a> / LSPosed module that lets the
  <b>My Dialog</b> app run on rooted, modded, or otherwise "non-standard" devices by
  neutralising its root / emulator / debug / tamper detection.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/license-GPL--3.0-blue.svg" alt="License: GPL-3.0">
  <img src="https://img.shields.io/badge/target-net.omobio.dialogsc-orange.svg" alt="Target package">
  <img src="https://img.shields.io/badge/tested-18.6.1-green.svg" alt="Tested version">
  <img src="https://img.shields.io/github/downloads/k4zectl/Dia-Gag/total.svg" alt="Total downloads">
</p>

---

## What it does

My Dialog ships a multi-layer anti-tamper stack that force-closes the app on rooted
devices. Dia-Gag hooks that stack so the app boots normally. It bypasses:

- **Root detection** - su/Magisk package scan, RootBeer, native root checks
- **Emulator detection** - `react-native-device-info` + the in-app emulator probes
- **Debugger / hook / taint detection** - the `tracker.*` detector set
- **Comprehensive security facade** - `SecurityManager` / `SecurityValidator`
- **Signature / integrity checks** funnelled through the same facade
- **Block handlers** - suppresses the "rooted device" / "security standards" toast + exit
- **RN startup crash guard** - fixes a `react-native-os` null-interface crash on boot

Nothing is spoofed or faked - detection results are simply forced to "clean" so the
app stops blocking itself.

## Requirements

- Android with **Magisk** (root) + **LSPosed** installed
- **My Dialog** app (`net.omobio.dialogsc`) - built and tested against **18.6.1**

## Install

1. Download the APK from [Releases](../../releases) (or build it - see below) and install it.
2. Open **LSPosed** -> **Modules** -> enable **Dia-Gag**.
3. Set its **Scope** to **My Dialog**.
4. Force-stop My Dialog and reopen it.

## Build

```bash
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/`.

## Important

> [!IMPORTANT]
> - **I am not responsible for any complications that may occur when using this module. Use at your own risk.**
> - **App and version support is not guaranteed.** This is tested against a specific My Dialog build; a new app update can move or rename the checks and break the module at any time. **Always read the release notes before updating or downloading.**
> - **Issues without logs or reproduction steps will be ignored.** Attach `adb logcat` output (filter tag `Dia-Gag`), your device / Android / LSPosed / My Dialog versions, and exact steps.
> - This module is for **educational and interoperability purposes** on devices **you own**. It does not bypass account authentication, payments, or any server-side security.

## License

Licensed under the [GNU GPL v3.0](LICENSE). Copyright (C) 2026 <a href="https://www.k4ze.dev/" target="_blank" rel="noopener noreferrer"><b>K4ZE DEV</b></a>.

## Author

Built and maintained by <a href="https://www.k4ze.dev/" target="_blank" rel="noopener noreferrer"><b>K4ZE DEV</b></a> (GitHub [@k4zectl](https://github.com/k4zectl)).
