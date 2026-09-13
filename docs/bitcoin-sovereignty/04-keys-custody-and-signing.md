# Keys, Custody, and Signing Risk

[← Back to the framework](README.md)

## Objective

Ensure that only the intended policy can authorize spending, while preserving reliable recovery if a device, key, person, vendor, or coordinator becomes unavailable.

## What must be preserved

A private key authorizes spending. A mnemonic may reproduce the seed from which keys are derived. A passphrase changes that seed. A derivation path and script type determine which keys and addresses are used. A multisignature descriptor records the threshold, participants, key origins, derivation, and script.

Therefore “I have the words” is not proof that a wallet can be recovered.

## Risk register

| Scenario | Preventive control | Detection | Response |
| --- | --- | --- | --- |
| Weak or manipulated entropy | Device-generated entropy or independently verifiable process | Deterministic verification/test vectors where appropriate | Do not fund; regenerate cleanly |
| Seed observed/copied | No cameras; controlled room; never enter on connected host | Broken seal, suspicious access, admission | Treat affected key as compromised; migrate |
| Passphrase lost/mistyped | Independent durable recovery; wallet fingerprint/check address | Expected balance absent | Stop guessing on exposed systems; validate spelling/normalisation safely |
| Signer firmware/host compromised | Verified software; hardware display verification | Advisory, signature/address mismatch | Stop use; verify advisory; migrate with known-good stack |
| Multisig key insertion/policy substitution | Cross-device policy and first-address verification | Signers show different policy/address | Do not fund/sign; rebuild setup |
| One multisig key compromised | Geographic and vendor independence; monitoring | Missing component or evidence of access | Create new policy and migrate before second failure |
| Xpub/descriptor leak | Treat wallet metadata as confidential | Full wallet unexpectedly visible | Contain metadata; assess physical/privacy risk; new wallet if warranted |
| Malicious PSBT | Verify full transaction on signers | Unexpected output, change, amount, or fee | Reject; investigate coordinator |

## Key-generation ceremony

Use this for a material reserve, adapted to the actual architecture.

1. **Prepare:** choose policy, script type, devices, backup medium, room, and observers in advance. Remove cameras and unneeded electronics.
2. **Authenticate devices:** source from a trustworthy channel; inspect tamper evidence; install/verify supported firmware by the vendor's documented method.
3. **Generate entropy:** use the signer's cryptographically secure generator or a correctly executed independent method. BIP39 encodes 128–256 bits of entropy plus checksum; it is not a method for converting a memorable sentence into a safe wallet.
4. **Record once, accurately:** number words; verify spelling and order; avoid photography, printers, cloud notes, clipboard, and ordinary password managers for deep-reserve seeds.
5. **Passphrase decision:** if used, understand that every exact passphrase produces a different wallet. Record it through an independent plan; include case, spaces, Unicode/normalisation concerns, and avoid memory-only recovery.
6. **Capture public reconstruction data:** network, script type, account, master fingerprint, derivation path, xpub(s), descriptor(s) with checksum, threshold/order, wallet name, and creation date. Store this as sensitive metadata, not as a spending secret.
7. **Register policy:** import the policy into each capable signer. Confirm threshold, number/order of keys, own key, derivation, and first receive address.
8. **Prove:** restore or reconstruct the wallet, compare several addresses, fund minimally, spend, and verify change.
9. **Distribute:** move recovery components to their intended locations only after successful testing.
10. **Close:** destroy temporary copies; record ceremony and test results without recording secrets in the log.

## Single-signature protocol

Hardware-backed single-signature is often appropriate when simplicity and reliable recovery dominate.

- Keep the signer and durable seed backup separate enough that one event does not take both.
- A PIN protects device access; it is not a substitute for seed security.
- A seed copy enables recovery and normally enables spending; protect it accordingly.
- If a passphrase is used, the seed and passphrase should not share the same failure domain.
- Maintain a compatible replacement or a documented standard-based recovery route.
- Verify receive addresses and transaction outputs on the signer display.

## Multisignature protocol

For an `M-of-N` wallet, the objective is that no one credible event steals or destroys `M` signing paths.

1. Use independently generated seeds. Never derive several cosigners from one seed.
2. Prefer independence across device vendor, storage location, administrator, and communication channel where the added complexity remains manageable.
3. Export each key record with fingerprint and derivation information.
4. Assemble the complete descriptor/policy using a coordinator.
5. On every capable signer, verify exact membership, threshold `M`, total signers `N`, derivation restrictions, script type, and first address. [BIP129](https://bips.dev/129/) specifically addresses tampering and interoperable setup.
6. Ensure each recovery location has sufficient non-secret policy information to combine a recovered seed with the other intended components. A seed without the other xpubs/policy may not recover the wallet.
7. Confirm different coordinator software reconstructs the same receive addresses where practical.
8. Conduct a complete PSBT signing test with the intended communication path—including remote signers if applicable.
9. Define what happens if one signer is lost, compromised, dies, refuses, or becomes unreachable.
10. Replace a compromised key by migrating to a new policy; a still-unspent threshold is time to act, not proof that nothing happened.

## Transaction-signing protocol

1. Establish recipient and amount through an authenticated channel.
2. Build the transaction in a watch-only/coordinator wallet using deliberate UTXO selection.
3. Review inputs, recipient outputs, change outputs, fee rate, absolute fee, and locktime/RBF behaviour.
4. Transfer the unsigned or partially signed transaction using [PSBT](https://bips.dev/174/) where supported.
5. On each signer, independently verify the intended policy, recipient address, amount, fee, and change. Do not approve blind or “non-standard” warnings without understanding them.
6. Combine signatures in the coordinator. Verify the final transaction again before broadcast.
7. Broadcast through a trusted node or deliberately chosen service.
8. Confirm inclusion, reconcile change, and update labels.

## Incident playbooks

### Seed or passphrase exposed

1. Assume copying may have occurred; do not merely relocate the same backup.
2. From verified devices, create a new wallet with new entropy and a tested backup.
3. Verify destination addresses on the new signer(s).
4. If the attacker may spend immediately, migrate with an appropriate fee; avoid public discussion.
5. After confirmation, retire the old wallet and investigate how exposure occurred.

For multisignature, exposure of fewer than the threshold does not authorize theft by itself, but the safety margin has been reduced. Replace the affected key/policy promptly.

### Signer lost or stolen

1. Determine whether the device, PIN, seed, and passphrase may all be exposed.
2. Protect remaining quorum components; do not gather them unnecessarily in one place.
3. If compromise is credible, migrate to a fresh policy.
4. If only availability is affected and threshold remains healthy, replace deliberately after testing.

### Wrong wallet after passphrase entry

1. Do not send funds and do not assume they disappeared.
2. Check exact case, spaces, keyboard layout, and character normalisation in a safe environment.
3. Compare a recorded wallet fingerprint or known receive address.
4. Never reveal the seed/passphrase to a recovery service without a carefully assessed legal and security process.

### Firmware or RNG vulnerability announced

1. Verify the advisory from the manufacturer and independent technical sources.
2. Identify affected model, version, generation method, and wallet—not merely the physical device.
3. Stop using the affected signer for new approvals if warranted.
4. Build a new wallet from fresh entropy on a known-good implementation; updating firmware does not retroactively repair weak seed generation.
5. Test, migrate, and preserve evidence/records.

### Signers disagree on address or policy

Stop. Do not fund or sign. Compare descriptor checksums, fingerprints, derivation paths, script type, threshold, and key order. Reconstruct from authenticated exports and verify the first address on all devices.

## Validation tests

- The intended wallet is restored from the actual recovery package.
- Several receive/change addresses match across original and recovery views.
- A full small-value spend has succeeded.
- A lost-device simulation leaves funds both safe and recoverable.
- A compromised-one-key multisig response has a written migration path.
- No seed or passphrase exists in photos, email, cloud notes, clipboard history, or support chats.
- Descriptor/policy backups are readable and sufficient without the original coordinator.

## Residual risks

Single-signature concentrates authority. Passphrases create silent alternative wallets and memory risk. Multisignature distributes authority but expands metadata and operational surface. Hardware signers reduce host compromise but still require the holder to verify what the device displays.

## Technical references

- [BIP32: Hierarchical Deterministic Wallets](https://bips.dev/32/)
- [BIP39: Mnemonic Code](https://bips.dev/39/)
- [BIP174: Partially Signed Bitcoin Transactions](https://bips.dev/174/)
- [BIP380: Output Script Descriptors](https://bips.dev/380/)
- [BIP129: Bitcoin Secure Multisig Setup](https://bips.dev/129/)
- [BIP388: Wallet Policies](https://bips.dev/388/)
