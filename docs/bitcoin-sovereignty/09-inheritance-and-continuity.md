# Inheritance and Continuity

[← Back to the framework](README.md)

## Objective

Ensure intended beneficiaries can discover, lawfully claim, and safely recover the bitcoin after death or incapacity—without giving them or an intermediary unnecessary premature control.

Inheritance is successful only when four chains work together:

1. **Discovery:** the right people know that an arrangement exists.
2. **Authority:** legal documents identify who may act and benefit.
3. **Technical recovery:** sufficient keys, policy data, and instructions can reconstruct and spend.
4. **Safe execution:** heirs obtain competent help without exposing funds to a scammer.

## Risk register

| Scenario | Prevent | Trigger | Response |
| --- | --- | --- | --- |
| Asset never discovered | Estate inventory and authenticated instruction location | Death/incapacity | Executor locates inventory without public disclosure |
| Seed found but wallet unknown | Script, derivation, fingerprint, descriptor/policy record | Recovery attempt shows no balance | Stop blind imports; reconstruct using documented metadata |
| Passphrase dies with owner | Independent protected recovery route | Owner unavailable | Activate route under defined authority |
| Heir gains access too early | Separate discovery, legal authority, and signing components | Boundary breach or relationship change | Migrate policy; update legal documents |
| Executor/beneficiary lacks competence | Named technical helper and verification procedure | Activation | Use watch-only reconstruction first; small test before transfer |
| Helper steals or misdirects | No unilateral helper authority; device verification | Address/policy mismatch | Stop; replace helper; preserve evidence |
| Cosigner unavailable/refuses | Threshold and replacement plan | Missed response/death/dispute | Use remaining lawful quorum or migrate before threshold loss |
| Law conflicts with technical outcome | Professional estate review | Move, marriage, divorce, law change | Align will/trust/entity and custody plan |
| Instructions become stale | Annual and event-driven review | New wallet/person/location | Replace obsolete copies and record version |

## Role separation

| Role | Needs to know | Should not automatically possess |
| --- | --- | --- |
| Beneficiary | That they benefit and how activation is authenticated | Immediate complete spending authority |
| Executor/personal representative | Asset inventory, legal documents, process | Seed/passphrase unless design requires it |
| Technical helper | Wallet type, recovery procedure, compatible tools | Ability to redirect funds unobserved |
| Cosigner/key holder | Their role and authenticated policy | All other keys and private family data |
| Storage host | How/when to release held material | Complete wallet map or other locations |
| Legal adviser | Ownership, value, beneficiaries, jurisdiction | Private keys by default |

One person may hold several roles, but the concentration should be intentional and documented.

## Continuity package

The non-secret or privacy-sensitive inventory should identify:

- existence and approximate role of each wallet layer;
- wallet name/identifier, network, script type, and creation date;
- where authenticated instructions are stored;
- executor, beneficiaries, technical helper, and cosigner contacts;
- what event activates access and what evidence is required;
- where accounting and ownership records are kept;
- latest test/version date.

The technical package may require mnemonic/key backups, passphrases, fingerprints, derivation paths, output descriptors, multisig policy, cosigner xpubs, birth height/date, and software compatibility notes. Distribute these so the intended activation reaches sufficient authority without creating an easy pre-activation theft path.

## Design protocol

### 1. Define outcomes

Specify beneficiaries, shares or purposes, executor, incapacity authority, timing, and whether heirs should receive bitcoin or fiat. Clarify treatment of minors, dependants, business holdings, charitable gifts, and taxes.

### 2. Map jurisdictions

Identify the holder's residence/domicile, asset/entity location, beneficiaries, executors, key holders, and storage locations. Obtain advice where these cross borders.

### 3. Select technical mechanism

Possible components include ordinary seed recovery, passphrase separation, multisignature with family/professional cosigners, timelocked recovery branches, institutional recovery, or combinations. Evaluate:

- premature-access risk;
- permanent-loss risk;
- legal enforceability;
- maintenance and fee requirements;
- privacy exposed to each participant;
- what happens when one participant dies first;
- compatibility across wallet implementations.

Timelocks can reduce dependence on a long-unavailable key, but they require careful script design, compatible software, monitoring, and periodic action depending on the construction. They are not a substitute for legal planning or testing.

### 4. Write two documents

1. **Estate/continuity overview:** explains existence, roles, legal authority, locations, and safe activation without containing all spending secrets.
2. **Technical recovery runbook:** precise wallet reconstruction and verification steps, stored so only authorised people can assemble it.

### 5. Authenticate instructions

Heirs must know how to distinguish the genuine plan from a forged email or opportunistic “recovery expert.” Use known advisers, sealed/versioned documents, pre-agreed contacts, and independent confirmation.

### 6. Test and educate

Walk each participant through only what their role requires. Conduct a tabletop exercise and a technical recovery test with limited funds. Record deficiencies and update both legal and technical documents.

## Activation protocol: death

1. Executor verifies death and legal authority; do not circulate seed material immediately.
2. Secure homes, devices, documents, phones, password managers, and known recovery locations against opportunistic loss.
3. Inventory wallet layers and records using watch-only information where possible.
4. Confirm the latest instruction version and all required roles/components.
5. Obtain jurisdiction-specific estate/tax advice before disposal or distribution.
6. Reconstruct wallet/policy without exposing all secrets to one uncontrolled computer or helper.
7. Verify several known addresses and expected balances.
8. Create beneficiary destination arrangements and test each with a small transfer.
9. Execute distribution with independent on-device verification and documented approvals.
10. Preserve records and securely retire obsolete keys/instructions after completion.

## Activation protocol: incapacity

1. Verify the defined medical/legal trigger; distinguish temporary unavailability from incapacity.
2. Use ordinary liquid funds first where appropriate.
3. Activate only the minimum authority required for care and obligations.
4. Record every material transaction and retain independent oversight.
5. If the holder recovers, review all disclosures and migrate any wallet whose secrecy or policy was weakened.

## Incident playbooks

### Owner dies with incomplete instructions

Secure all devices and physical records; do not guess passphrases repeatedly or type seeds into internet tools. Identify wallet apps, hardware devices, transaction records, xpubs/descriptors, and exchanges. Use a reputable professional under a written, verified engagement that does not require surrendering unilateral control where avoidable.

### Beneficiary/cosigner relationship changes

Review legal entitlement separately from signing power. If an unwanted person holds a key or sufficient recovery knowledge, build and test a new policy, migrate, then update all documents and destroy obsolete instructions where lawfully appropriate.

### One key holder dies or becomes unreachable

If the remaining policy still has a healthy quorum, use the opportunity to migrate deliberately. Do not leave the wallet indefinitely at reduced fault tolerance.

### Instructions released prematurely

Identify exactly which secrets and metadata were disclosed. If unilateral or threshold spending may be possible, migrate to a fresh tested wallet. Update release controls; retrieving the paper does not prove it was not copied.

## Validation tests

- The executor can discover the plan without already controlling the funds.
- Technical recovery succeeds without the owner's memory.
- Heirs can authenticate instructions and helpers.
- The legal plan and wallet policy produce the same intended outcome.
- Death/unavailability of any one designated person does not create unintended permanent loss.
- A small end-to-end beneficiary transfer has been rehearsed.
- Every copy shows version and review date.

## Residual risks

Every inheritance route creates some premature-access exposure. Legal authority may not compel a technically unavailable key, while technical possession may not create legal ownership. Timelocks, multisignature, and professional services add maintenance and implementation dependencies. The plan must balance these explicitly.

## Technical references

- [BIP380: Output Script Descriptors](https://bips.dev/380/)
- [BIP129: Bitcoin Secure Multisig Setup](https://bips.dev/129/)
- [BIP388: Wallet Policies](https://bips.dev/388/)
