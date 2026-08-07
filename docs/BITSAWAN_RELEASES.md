# BitSawan release signing

BitSawan is distributed under its own Android and cryptographic identity. It does not impersonate
or replace the upstream MetroVault application.

- Package ID: `com.charcode.bitsawan`
- Release certificate SHA-256:
  `578152e3c2f77b01d7b3b93b37df7870b38da2a699ac3f0382a86d90a0098bc9`
- Release tags: `bitsawan-v*`
- Signed downloads: <https://github.com/charcode/BitSawan/releases>

Verify a downloaded APK with Android SDK Build Tools:

```shell
apksigner verify --verbose --print-certs BitSawan-*-release.apk
```

The reported certificate SHA-256 digest must exactly match the value above.

## Signing-key handling

The private release key and its recovery credentials are never committed. The maintainer keeps
restricted local copies and separate encrypted offline backups. GitHub Actions receives an
encrypted copy through the protected `release` environment using these environment secrets:

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

The environment requires maintainer approval and permits deployments only from `main` and tags
matching `bitsawan-v*`. Pull-request workflows do not receive signing secrets. Third-party actions
in the release workflow are pinned to immutable commit SHAs.

The signing identity must remain stable for the lifetime of the BitSawan package. Losing the key
prevents future updates; disclosure would allow an attacker to sign malicious updates. Rotate or
replace it only through a documented Android signing-key migration.
