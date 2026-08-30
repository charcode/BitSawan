# BitSawan contributor guidance

## Scope and architecture

- BitSawan is an independent Android application forked from MetroVault. Keep the inherited Kotlin namespace (`com.gorunjinian.metrovault`) and the independent application ID (`com.charcode.bitsawan`) unless a release plan explicitly changes them.
- The app uses Kotlin, Jetpack Compose, and MVVM. UI and ViewModels live under `feature/`; wallet orchestration is in `domain/Wallet.kt`; Bitcoin and mnemonic operations are under `domain/service/bitcoin/`; encrypted persistence is under `data/`.
- Make focused changes. Preserve BitSawan-specific release signing, package identity, and reproducible-build configuration when incorporating upstream work.

## Mandatory security protocol

- Before changing or reviewing code, build configuration, dependencies, storage, or release automation, read `docs/SECURITY.md` in full. It mirrors the upstream [MetroVault security policy](https://github.com/gorunjinian/MetroVault/security/policy) and is a design contract, not optional background reading.
- Treat BitSawan-specific decisions in this repository as authoritative where they intentionally differ from MetroVault. Do not silently weaken an invariant or overwrite a fork-specific control while syncing upstream; document and escalate any genuine conflict before continuing.
- Before editing, identify whether the change touches authentication, password records or migration, cryptography, entropy, wallet secrets, persistence, session/lifecycle state, biometrics, rate limiting, destructive wipe, decoy isolation, sensitive UI, QR/clipboard flows, backups, network access, dependencies, or signing. Trace the complete data and call path for every relevant category.
- Preserve the policy's defense-in-depth model: no wallet secret may reach a network interface, log, analytics/crash payload, Android backup, screenshot/recent-app preview, autofill/keyboard learning, or an uncleared clipboard. Do not add network permissions, clients, remotely loaded assets, or runtime-fetched icons to the offline app without explicit security review.
- Preserve cryptographic separation and formats: password verification and wallet encryption use distinct HKDF contexts; comparisons of secret-derived verifiers remain constant-time; authenticated encryption keeps unique cryptographically random IVs; serialized or encrypted formats and legacy migrations remain compatible unless an explicit, tested migration is supplied.
- Keep mnemonics, passphrases, private keys, seeds, master/session keys, raw entropy, and sensitive intermediates in memory only as long as required. Prefer mutable buffers, wipe before replacement/removal, and keep concurrent access synchronized so a lifecycle or emergency wipe cannot expose partially cleared state.
- Biometric flows must remain hardware-backed where available, bound to the exact cryptographic operation with `BiometricPrompt.CryptoObject`, restricted to strong per-use authentication, invalidated safely when enrollment changes, and fail closed by removing stale ciphertext whenever a credential update is incomplete.
- Preserve brute-force protections, persisted dual-clock lockouts, opt-in destructive-wipe confirmation and scope, and the distinction between user-entered failures and stale biometric-originated failures. Preserve main/decoy storage and key isolation, neutral error messages, and behavior that does not reveal whether another vault exists.
- Use `SecureRandom` for security-sensitive randomness. Do not invent cryptographic constructions or change algorithms, parameters, domain strings, entropy accounting, key derivation, signing, or encryption behavior without explicit security review, deterministic vectors where applicable, compatibility analysis, and focused tests.
- For every security-relevant change, state the threat model and affected invariants in the review description; test success, failure, cancellation, migration, tampering, lifecycle, and cleanup paths as applicable. A passing happy-path test is not sufficient evidence that a security property is preserved.
- If a potential vulnerability is discovered, do not open a public issue or include exploit details in public logs. Report it privately to the BitSawan maintainer and allow coordinated remediation before disclosure.

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
