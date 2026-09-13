# Threat Model and Design Principles

[← Back to the framework](README.md)

## Purpose

A threat model prevents Bitcoin security from becoming a collection of devices, secrets, and rituals. It asks what must survive, what could defeat it, and whether a proposed control reduces total risk rather than merely moving risk somewhere less visible.

The unit of analysis is not the hardware wallet. It is the complete socio-technical system: holder, money, keys, devices, wallet software, backups, locations, network, counterparties, trusted people, law, and time.

## Assets to protect

| Asset | Required property | Failure example |
| --- | --- | --- |
| Spend authority | Only intended policy can authorize payment | Seed theft or malicious cosigner |
| Recovery authority | Intended recovery remains possible | Lost passphrase or incomplete descriptor |
| Wallet truth | Balance, addresses, policy, and transaction intent are authentic | Compromised coordinator displays false address |
| Availability | Funds can be reached within required time | Quorum components inaccessible across borders |
| Privacy | Wealth graph and identity are not needlessly exposed | Xpub leak or UTXO consolidation |
| Purchasing power | Holder is not forced into a destructive sale | Leverage or inadequate emergency cash |
| Continuity | Intended successors can act after incapacity/death | Heirs find seeds but not policy or instructions |
| Evidence | Ownership, cost basis, and incident facts can be demonstrated | Exchange closes and records disappear |

## Threat actors and failure sources

Do not model only a technically sophisticated thief. Include:

- opportunistic thief finding a backup or unlocked device;
- remote malware or phishing operator;
- targeted attacker who knows the holder has bitcoin;
- dishonest or pressured family member, employee, adviser, or cosigner;
- compromised manufacturer, wallet release, update channel, or coordinator;
- exchange, custodian, lender, bank, or service failure;
- fire, water, corrosion, disposal, travel loss, and geographic disaster;
- government or court acting through lawful or unlawful coercion;
- future cryptographic or implementation failure;
- the holder while tired, rushed, impaired, ill, ageing, or deceased.

## Scoring method

Score each scenario from 1 to 5 on four dimensions:

| Dimension | 1 | 5 |
| --- | --- | --- |
| Impact | Negligible | Life-changing or total loss |
| Likelihood | Remote | Expected or recurring |
| Detectability | Immediate and obvious | Silent until irreversible |
| Recoverability | Easy and complete | Irreversible |

Use the scores to compare priorities, not to manufacture false precision. A lower-likelihood event may still require treatment if its impact is catastrophic and the control is cheap.

Record for each risk: scenario, preconditions, affected layer, existing controls, remaining exposure, trigger for action, owner of the response, and next review date.

## Architecture decision protocol

### 1. Define the layers

Separate spending funds, working reserve, deep reserve, emergency liquidity, and continuity arrangements. State the maximum acceptable loss and required access time for each.

### 2. Define failure tolerance

For each layer, answer:

- Which single component may be lost without losing funds?
- Which single component may be stolen without enabling theft?
- Which people may become unavailable?
- How long can access be delayed?
- What information may be disclosed without enabling spending?

### 3. Select the minimum sufficient policy

- **Mobile/software single-signature:** suitable only for deliberately limited loss exposure.
- **Hardware-backed single-signature:** often the best simplicity/security balance for a competent individual.
- **Seed plus passphrase:** useful when discovered seed material is credible, provided the passphrase has an independent recovery path.
- **Multisignature:** justified when eliminating a single signing/recovery point matters enough to support policy backup and coordination.
- **Collaborative or institutional custody:** may improve availability and inheritance while adding counterparty, privacy, and censorship exposure.

### 4. Model correlated failures

Three devices are not three independent defences if they share the same seed, vendor, location, host computer, update process, cloud account, or trusted person. Geographic distribution is not independent if all locations fall under one legal or family failure.

### 5. Model usability

Ask whether the holder can execute the protocol when travelling, under stress, after years of inactivity, and with one device unavailable. A design whose normal use invites shortcuts will eventually be bypassed.

### 6. Prove recovery and spending

Do not fund materially until reconstruction, receipt, signing, change verification, broadcast, and record updates have all been tested.

## Control hierarchy

Prefer controls in this order:

1. **Eliminate:** do not expose deep reserves to a hot wallet or yield platform.
2. **Limit:** cap the balance or authority exposed to one device/person/service.
3. **Separate:** divide roles, locations, secrets, and fund layers.
4. **Verify:** authenticate policy, address, transaction, software, and people independently.
5. **Detect:** monitoring, seals, balance checks, advisories, and review triggers.
6. **Recover:** tested backups, replacement devices, migration destinations, and succession.

## Common design traps

| Trap | Hidden failure | Correction |
| --- | --- | --- |
| “More devices means safer” | Same seed or vendor creates correlation | Define truly independent components and roles |
| “The seed is the backup” | Script/policy/derivation data may be missing | Back up complete wallet reconstruction information |
| “I will remember the passphrase” | Memory is a single point of permanent loss | Create an intentional independent recovery route |
| “Air-gapped means safe” | Malicious transaction or QR data can still be signed | Verify policy, inputs, outputs, amount, fee, and change on-device |
| “Multisig solves theft” | Bad setup can insert keys or destroy recovery | Cross-verify policy and first address on every signer |
| “Privacy is restored by moving coins” | On-chain history remains analysable | Model linkage, UTXO use, network data, and counterparties |
| “No one knows I own bitcoin” | Exchange, email, vendors, or public speech may reveal it | Assume partial knowledge and minimise actionable detail |

## Unknown-event protocol

When an event does not fit an existing playbook:

1. Stop irreversible action.
2. Write down observed facts separately from assumptions.
3. Identify what authority or information may be exposed.
4. Protect uncompromised components and preserve evidence.
5. Seek independent, authenticated expertise without revealing seeds.
6. Choose a response that remains safe if the leading assumption is wrong.
7. Test with a small amount where time and threat permit.
8. Record the lesson and update the risk register.

## Validation tests

- The owner can explain the architecture and its failure tolerance in plain language.
- Losing any designated non-critical component does not lose funds.
- Compromising any designated sub-threshold component does not enable theft.
- A complete recovery and small test spend has succeeded.
- The system can be operated without consulting secret notes on an online device.
- A named successor can locate authentic instructions without already possessing unilateral control.
- Every material risk has a trigger, response, and review date.

## Residual risk statement

No architecture eliminates cryptographic, human, market, political, and physical risk simultaneously. The accepted residual risks should be written explicitly. Unwritten acceptance is usually unexamined exposure.

## Technical references

- [BIP32: Hierarchical Deterministic Wallets](https://bips.dev/32/)
- [BIP380: Output Script Descriptors](https://bips.dev/380/)
- [BIP388: Wallet Policies for Descriptor Wallets](https://bips.dev/388/)
