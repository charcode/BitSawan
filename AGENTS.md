# BitSawan contributor guidance

## Scope and architecture

- BitSawan is an independent Android application forked from MetroVault. Keep the inherited Kotlin namespace (`com.gorunjinian.metrovault`) and the independent application ID (`com.charcode.bitsawan`) unless a release plan explicitly changes them.
- The app uses Kotlin, Jetpack Compose, and MVVM. UI and ViewModels live under `feature/`; wallet orchestration is in `domain/Wallet.kt`; Bitcoin and mnemonic operations are under `domain/service/bitcoin/`; encrypted persistence is under `data/`.
- Make focused changes. Preserve BitSawan-specific release signing, package identity, and reproducible-build configuration when incorporating upstream work.

## Build and test

Use JDK 17 and an Android SDK that supports the compile SDK declared in `app/build.gradle.kts`. On Windows use `gradlew.bat`; on Unix-like systems use `./gradlew`.

```text
gradlew.bat testDebugUnitTest
gradlew.bat lintDebug
gradlew.bat assembleDebug
```

Run the narrowest relevant unit tests while iterating, then run the full unit-test and debug-build commands before handoff. Release builds additionally require the signing configuration documented in `keystore.properties.example` and `docs/BITSAWAN_RELEASES.md`; never commit signing secrets or `keystore.properties`.

## Bitcoin and sensitive-data rules

- Treat mnemonic generation, entropy conversion/mixing, BIP32/BIP39 derivation, address construction, PSBT signing, encryption, and key storage as security-critical. Cryptographic behavior must not be changed casually or as part of unrelated refactoring.
- Trace callers and document compatibility effects before changing serialized formats or cryptographic behavior. Require review and deterministic published test vectors for every physical-entropy conversion or serialization change.
- Device + physical entropy is the recommended default. The explicitly supported physical-only mode is deterministic, must enforce the selected BIP39 strength threshold, and must retain converter-compatible/frozen test vectors. Hashing or normalization does not create entropy.
- Never log or persist seed phrases, BIP39 passphrases, private keys, raw entropy, or sensitive intermediate values. Avoid exposing them to clipboard, screenshots, recent-app previews, crash reports, analytics, or Android backups.
- Keep sensitive values in memory only as long as necessary and wipe mutable buffers where practical. Do not weaken security dialogs, biometric binding, password defaults, or release log stripping without explicit security review.
- For coin, dice, or card entropy, use a frozen canonical serialization with explicit domain separation and deterministic vectors. Validate distribution assumptions; do not map non-power-of-two symbols directly to bytes and claim uniform entropy.

## Upstream work

Inspect `git status`, fetch `upstream`, and compare from the merge base before syncing MetroVault. Do not overwrite fork-specific changes or touch unrelated untracked files. Stop and report meaningful conflicts before resolving them unless conflict resolution was explicitly requested.
