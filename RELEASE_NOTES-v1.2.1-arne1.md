# Belfius Root v1.2.1-arne1 — unofficial community build

> ⚠️ This is a **fork build** from [ArneAmeye/Belfius-Root](https://github.com/ArneAmeye/Belfius-Root), **not** an official release by Rikj000.
> It contains the work in the still-open upstream [PR #26](https://github.com/Rikj000/Belfius-Root/pull/26). Once that PR is merged, use the official release instead.

`versionName` 1.2.1-arne1 · `versionCode` 114 · source commit `e9eb30b`

## What's in this build

Based on upstream `development` at `0ef8558`, with the PR #26 changes on top:

- **Modern Xposed API** — ported from the legacy api:82 bridge to libxposed API 102 (`XposedModule` / `onModuleLoaded` / `onPackageLoaded`, `META-INF/xposed` module metadata, `chain.proceed()` plumbing) and a remote-preferences **Debug logging** toggle.
- **Belfius v26.1.0 and v26.3.0 support** — hides from v26.1's anti-hooking checks, fixes the v26.3 generic "Algemene fout" at its source, and forces the in-app root verdict to "not rooted" (that verdict is read by real login logic, not just the info screen).
- **Per-version hook profiles** — reads the Belfius app's own version code before the packer injects its runtime code and selects the `legacy` / `v26.1` / `v26.3` hook set. Unknown versions get a union of hooks plus a loud warning.
- **Isolated scanner disarm** (new setting, **off by default**) — additively disables the isolated `o.cDe` root-environment scanner on top of the shipped bypass; it does not replace the existing hooks.
- **UI refresh** — About and Settings split into tabs with bottom navigation, Material 3 System/Dark/Light theme setting, icons and short descriptions per setting, and a themed monochrome launcher icon on Android 12+.
- **Code structure** — `BelfiusRoot` split into focused classes; hook behaviour and log strings are unchanged.

## Compatibility

| Belfius app | Version code | Status |
|---|---:|---|
| v26.3.0 | 262601628 | ✅ supported |
| v26.1.0 | 260781255 | ✅ supported |
| v25.4.1 and older | see `SupportedVersions.md` | legacy support; Belfius no longer allows these versions |

Belfius currently enforces a minimum of v26.1.0 (260781255). Full details are in `SupportedVersions.md` in the source.

## Install

1. Install [Magisk](https://topjohnwu.github.io/Magisk/install.html)
2. Magisk => Settings => Magisk => Check `Zygisk`
3. Magisk => Settings => Magisk => Configure DenyList => UnCheck `Belfius Mobile` (= Default)
4. Install [LSPosed (Zygisk)](https://github.com/LSPosed/LSPosed/releases/latest) + [Shamiko](https://github.com/LSPosed/LSPosed.github.io/releases/latest)
5. **Uninstall any official Belfius-Root build first.** This APK is signed with a different key, so Android will refuse to install it over the official one.
6. Install this APK
7. LSPosed => Modules => `Belfius Root` => Check `Enable module` + Check `Belfius Mobile`
8. Reboot

## Source, licensing and verification

- License: **GPL-3.0**; upstream copyright and notices are retained. My changes are identified in the commit history and in this note.
- Source for this exact build: tag `v1.2.1-arne1` at `e9eb30b` in [ArneAmeye/Belfius-Root](https://github.com/ArneAmeye/Belfius-Root). GitHub also attaches the source archive to this release automatically.
- APK: `Belfius-Root_v1.2.1-arne1.apk` — SHA-256: `950ec0d77f4fd624ae296526f3af9dda0b6df6ac4462d80cf02b7c451932c8e2`
- Signing: APK Signature Scheme v2, certificate `CN=ArneAmeye, O=Belfius-Root community fork, C=BE`, SHA-256 fingerprint: `55aee66aa5b1ccab792a30cb8727e822d1348d461cda9e9e74c312c3d5c064a5` (different from Rikj000's release key, hence the uninstall step above)

This is an independent build shared for testing while the upstream PR awaits review; it is not endorsed by Rikj000 and is used at your own risk.
