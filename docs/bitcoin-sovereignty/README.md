# Protecting Your Bitcoin, Preserving Your Freedom

## Purpose

This is a living framework for protecting a Bitcoin holding without losing sight of why it is held. The purpose is not merely to make private keys difficult to steal. It is to preserve the holder's ability to benefit from bitcoin—privately, voluntarily, and on their own terms—through changing markets, technologies, jurisdictions, relationships, health, and stages of life.

Bitcoin is not the final goal. It is a tool for protecting time, choice, purchasing power, and personal agency. A custody arrangement that is technically formidable but unusable, unmaintainable, impossible to inherit, or dangerous to operate has failed.

> The safest bitcoin is not merely the hardest to steal. It is bitcoin that remains yours, remains usable, survives you when necessary, protects your privacy, and expands rather than constrains your choices throughout life.

## No one-size-fits-all solution

There is no universally correct wallet, signing policy, backup scheme, or privacy threshold. An appropriate system depends on:

- the value at risk and the consequences of loss;
- technical ability and willingness to maintain skills;
- spending frequency and required recovery time;
- family, inheritance, and trusted-person circumstances;
- home, travel, and geographic exposure;
- legal and tax jurisdictions;
- credible attackers and failure events;
- the holder's tolerance for complexity and disclosure.

Every defence creates costs or secondary risks. More copies reduce accidental loss but increase exposure. A passphrase protects a discovered seed but can become an unrecoverable secret. Multisignature removes one catastrophic key but creates policy, metadata, coordination, and succession risks. Strong privacy improves personal safety but can complicate accounting and regulated liquidity.

The objective is not maximum security in one dimension. It is a balanced, tested system whose trade-offs are deliberately chosen.

## The continuing journey

Bitcoin security is a process, not a product. Most people move from an exchange to a software wallet, then learn about hardware signers, seed backups, passphrases, UTXOs, nodes, multisignature, geographic separation, and inheritance. Each step changes the threat model.

The system should evolve as value, competence, family, residence, health, and technology change. Complexity must be earned by a specific risk. Improvements should be incremental, tested with small amounts, documented, and reversible until the new arrangement has been proved.

This top-level file is the map and common protocol. The linked files are operational modules. They contain risk registers, controls, response playbooks, tests, and residual risks.

## The six objectives

1. **Ownership** — nobody spends the bitcoin without the holder's consent.
2. **Availability** — it can be accessed within the required time when genuinely needed.
3. **Purchasing power** — market and liquidity decisions preserve future economic options.
4. **Privacy** — outsiders cannot easily map identity, balances, relationships, and activity.
5. **Continuity** — accident, incapacity, death, or memory failure does not destroy it.
6. **Freedom of action** — no single exchange, custodian, person, service, device, or jurisdiction can arbitrarily prevent lawful use.

## Risk map

No table can cover literally every future event. This map covers the principal families of loss and provides a protocol for unknown events: pause, preserve evidence, classify the failure, contain exposure, restore from a known-good state, and improve the design.

| Domain | Representative event | Prevent | Detect | First response | Module |
| --- | --- | --- | --- | --- | --- |
| Design | Architecture does not match the real threat | Written threat model; proportional complexity | Owner cannot explain or test it | Freeze changes; simplify; retest | [Threat model](01-threat-model-and-design-principles.md) |
| Market | Drawdown, leverage, or emergency forces a sale | No leverage; adequate fiat runway; sizing | Collateral warnings; shrinking runway | Stop new risk; restore liquidity; do not improvise | [Market and liquidity](02-market-liquidity-and-purchasing-power.md) |
| Counterparty | Exchange freezes, fails, or is hacked | Minimise balances; withdrawal routes | Delays, changed terms, distress reports | Preserve records; stop deposits; test lawful withdrawal | [Counterparties](03-counterparty-and-institutional-risk.md) |
| Key theft | Seed, passphrase, or signer may be exposed | Offline signing; compartmentation; discretion | Unexplained access or device behaviour | Treat as compromised; move to verified fresh policy | [Keys and signing](04-keys-custody-and-signing.md) |
| Policy tampering | Wrong multisig keys, address, or change | Registered policy; device verification | Devices show different policy/address | Do not fund or sign; rebuild and cross-check | [Keys and signing](04-keys-custody-and-signing.md) |
| Physical loss | Fire, flood, theft, corrosion, device death | Durable and separated recovery components | Missing seal/component; unreadable backup | Inventory remaining quorum; secure; recover safely | [Backups and recovery](05-operations-backups-and-recovery.md) |
| Transaction error | Wrong address, amount, fee, network, or change | Deliberate signing checklist; test payment | Device differs from intent | Reject before signing; if broadcast, preserve evidence | [Backups and recovery](05-operations-backups-and-recovery.md) |
| Privacy | KYC linkage, address reuse, UTXO merge, xpub leak | Fresh instructions; coin control; own node | Unexpected wallet visibility or clustering | Stop further linkage; quarantine metadata/UTXOs | [Privacy and UTXOs](06-privacy-kyc-and-utxo-risk.md) |
| Coercion | Robbery, extortion, targeted home attack | Discretion; limited hot balance; distributed authority | Threats, stalking, data leak | Prioritise life; follow personal emergency plan | [Physical and social](07-physical-personal-and-social-risk.md) |
| Personal | Incapacity, memory decline, impulsive action | Governance, trusted checks, written recovery | Missed steps, confusion, behaviour change | Pause unilateral large transfers; activate helpers | [Physical and social](07-physical-personal-and-social-risk.md) |
| Legal | Seizure, reporting issue, hostile rule change | Records; advice; geographic and service diversity | Official notice or rule change | Preserve records; obtain counsel; avoid concealment | [Law and jurisdiction](08-legal-political-and-jurisdictional-risk.md) |
| Succession | Owner dies and heirs cannot recover | Integrated technical and estate plan | Outdated people/instructions | Executor follows staged recovery; avoid rushed disclosure | [Inheritance](09-inheritance-and-continuity.md) |
| Technology | Firmware, wallet, format, or vendor becomes unsafe | Open standards; diversity; planned upgrades | Advisory, incompatibility, failed verification | Stop signing; verify issue; migrate from clean devices | [Review and migration](10-review-testing-and-continuing-journey.md) |

## Security protocol: the universal lifecycle

### Phase 1 — Define

1. Inventory holdings by purpose without putting secret material in the inventory.
2. Identify credible theft, loss, privacy, availability, legal, and succession events.
3. Rate each by impact, likelihood, detectability, and recoverability.
4. Define acceptable recovery time and acceptable single-event loss.
5. Choose the simplest architecture that brings catastrophic risks within tolerance.

### Phase 2 — Build

1. Obtain devices through trustworthy channels and inspect packaging and firmware verification.
2. Generate keys in a controlled environment from verifiable entropy; never invent a brainwallet.
3. Record the recovery material accurately and durably.
4. Record wallet type, network, script type, derivation information, fingerprints, and—where relevant—the complete output descriptor or multisig policy.
5. Keep secrets, public wallet metadata, operational instructions, and legal documents in deliberately chosen locations.
6. Verify policy and first receive address independently on each capable signer.

### Phase 3 — Prove before funding

1. Restore or reconstruct the wallet from the intended recovery package.
2. Confirm multiple receive addresses match between the original and restored wallet.
3. Receive a small amount.
4. Build, verify, sign, broadcast, and confirm a complete test spend.
5. Confirm change returns to the intended wallet.
6. Destroy any temporary recovery copies and record the successful test date.

### Phase 4 — Receive

1. Generate a fresh payment instruction for each ordinary on-chain receipt.
2. Verify it on a trusted device when the amount matters.
3. Communicate the address through an authenticated channel; for large transfers, verify through a second channel or signed message where supported.
4. Preserve source, cost basis, purpose, and UTXO labels in an appropriately protected record.
5. Wait for confirmation appropriate to the value and counterparty risk.

### Phase 5 — Spend

1. Start from a trusted wallet view connected to a trusted node where practical.
2. Select UTXOs deliberately; review privacy consequences and change.
3. Confirm recipient, network, amount, fee, inputs, outputs, and change out of band.
4. Verify the final transaction on every signing device; the computer coordinating the transaction is not the authority.
5. For multisignature, pass the same PSBT through the required signers and verify the final transaction again before broadcast.
6. Update labels and records after confirmation.

### Phase 6 — Maintain

1. Monitor authoritative security advisories without reacting to rumours.
2. Inspect backup presence and condition without unnecessarily exposing secrets.
3. Rehearse a small transaction periodically.
4. Test recovery on a planned schedule and after material changes.
5. Review liquidity, trusted people, inheritance, jurisdiction, and architecture annually.

### Phase 7 — Change or migrate

1. Define why the change is needed and what new risks it introduces.
2. Build and fully test the destination wallet first.
3. Verify fresh receive addresses on destination signing devices.
4. Move a small amount and complete a return test.
5. Move the reserve deliberately, preserving privacy and fee considerations.
6. Retain the old recovery capability until the migration and records are conclusively reconciled; then retire it safely.

### Phase 8 — Incident

Use **S-A-F-E-R**:

1. **Stop** — do not sign, disclose more, reinstall blindly, or follow unsolicited recovery help.
2. **Assess** — classify the event: theft risk, availability failure, privacy leak, legal issue, or market/liquidity pressure.
3. **Fence** — isolate affected devices/accounts, revoke sessions where relevant, and protect remaining quorum components.
4. **Execute** — follow the relevant module's prewritten response using known-good devices, software, addresses, and people.
5. **Record and review** — preserve transaction IDs, communications, logs, timings, and decisions; then remove the root cause.

If physical safety is threatened, preservation of life takes priority over preservation of bitcoin.

## Security states

| State | Meaning | Permitted action |
| --- | --- | --- |
| Green | Components accounted for; tests current; no credible compromise | Normal receipt and deliberate spending |
| Amber | Anomaly, stale test, missing non-quorum component, or unverified advisory | Receive only if safe; pause large spends; investigate |
| Red | Seed/key exposure, quorum risk, confirmed malware, coercion, or policy mismatch | Stop ordinary operation; execute incident migration/recovery |
| Black | Active threat to life or legal emergency | Follow personal safety/legal plan; do not improvise publicly |

## Layer funds according to purpose

| Layer | Purpose | Design target |
| --- | --- | --- |
| Spending | Daily payments and experimentation | Convenient; limited-loss balance; easy device replacement |
| Working reserve | Planned medium-term expenditure | Hardware-assisted; accessible; tested recovery |
| Deep reserve | Long-term wealth preservation | Slow; strongly protected; geographically resilient |
| Emergency liquidity | Avoid forced bitcoin sales | Sufficient non-Bitcoin liquidity for foreseeable shocks |
| Continuity | Incapacity and inheritance | Staged discovery and recovery for intended people |

The amount kept convenient should be an amount whose loss is painful but not life-changing. The deep reserve should not be exposed whenever a small payment is made.

## Universal rules

- Never type or photograph deep-reserve seed words on an ordinary connected device.
- Never disclose a seed to “support”; legitimate support does not need it.
- Never sign what cannot be verified on the signing device.
- Never rely on memory as the sole recovery path.
- Never assume a seed alone recovers a non-trivial wallet.
- Never make a large migration before a complete small-value test.
- Never let urgency created by a stranger override the incident protocol.
- Treat xpubs, descriptors, labels, and transaction records as privacy-sensitive even when they cannot spend.
- Keep enough ordinary liquidity that bitcoin security is not defeated by forced selling.
- Treat law, health, family, and ageing as part of the system—not external inconveniences.

## Standards baseline

The technical modules use established Bitcoin formats where supported: [BIP32 hierarchical deterministic wallets](https://bips.dev/32/), [BIP39 mnemonic recovery](https://bips.dev/39/), [BIP174 PSBT](https://bips.dev/174/), [BIP380 output descriptors](https://bips.dev/380/), [BIP129 secure multisig setup](https://bips.dev/129/), [BIP388 wallet policies](https://bips.dev/388/), and [BIP329 wallet labels](https://bips.dev/329/). Support varies by wallet and device; a standard's existence does not prove a particular implementation supports it safely.

## Scope

This is an educational and operational risk framework, not personal investment, legal, tax, or physical-security advice. Jurisdiction-dependent actions require appropriate professional advice. No protocol removes all risk; the objective is to make catastrophic loss less likely, recovery more reliable, and trade-offs visible.

## Version

Version 0.2 — expanded risk map and lifecycle protocol. Review date: 2026-09-13.
