# Operations, Backups, and Recovery

[← Back to the framework](README.md)

## Objective

Make ordinary use repeatable and deliberate, and ensure that fire, water, theft, device failure, forgotten procedures, or software disappearance does not make the bitcoin unspendable.

Recovery means restoring the ability to identify and safely spend every intended UTXO. Producing valid seed words is only one component of recovery.

## Recovery package

The package required depends on the wallet, but may include:

| Component | Function | Sensitivity |
| --- | --- | --- |
| Seed/mnemonic or key backup | Recreates private keys | Critical spending secret |
| Passphrase | Selects/protects wallet derived from seed | Critical spending secret |
| Network and script type | Identifies address family | Non-secret but essential |
| Fingerprint and derivation path | Locates correct key branch | Privacy-sensitive metadata |
| Output descriptor with checksum | Defines scripts, keys, and derivations | Usually watch-only but reveals wallet |
| Multisig policy and cosigner xpubs | Reconstructs threshold wallet | Privacy-sensitive; operationally essential |
| Birth height/date and gap assumptions | Makes rescan reliable and efficient | Metadata |
| Labels and UTXO provenance | Preserves privacy and accounting decisions | Highly sensitive metadata |
| Instructions and contacts | Explain activation and safe procedure | Sensitive; avoid embedding secrets unnecessarily |

[BIP380 descriptors](https://bips.dev/380/) address the recovery ambiguity created when private keys alone do not specify script types and derivation paths. [BIP329](https://bips.dev/329/) defines a portable labels format, though it remains a draft and wallet support varies.

## Backup design protocol

### 1. Identify failure domains

List fire, water, corrosion, theft, discovery, disposal, building access, legal seizure, conflict, and geographic disaster. Two backups in one building are one fire domain. A seed and passphrase in one envelope are one secrecy domain.

### 2. Choose redundancy deliberately

Specify which one event may destroy and which one event may expose without causing permanent loss or theft. More copies are beneficial only when their exposure risk is controlled.

### 3. Choose media

- paper is legible and inexpensive but vulnerable to water, fire, pests, fading, and accidental disposal;
- metal can resist environmental damage but must be tested for legibility, corrosion, deformation, and completeness;
- digital encrypted backup may improve duplication and portability but creates password, malware, format, and cloud-account dependencies;
- device backups are convenient but should not be the only recovery route.

### 4. Record accurately

Number every word or component. Preserve exact spelling, order, case, spaces, and format. For BIP39 English words, the first four letters uniquely identify each word, but recording full words improves human checking.

### 5. Separate information by role

Do not automatically keep all secrets together. Conversely, do not split information so aggressively that recovery becomes a scavenger hunt. Record a non-secret inventory that identifies what exists, what wallet it belongs to, and what event activates it.

### 6. Add tamper evidence

Seals and access logs do not prevent copying, but they can change a silent compromise into a detected event. Record identifiers without photographing secret contents.

### 7. Test

Reconstruct in a controlled environment using only the intended recovery package. Compare addresses, conduct a small spend, confirm change, and record the result.

## Receive protocol

1. Open the intended wallet and check network and wallet name/fingerprint.
2. Generate a fresh address for ordinary on-chain receipt.
3. Verify the address on the relevant signing device; for multisig, verify on more than one independently configured device when value warrants.
4. Share the address through an authenticated channel. For a large transfer, compare characters or a cryptographic digest/second channel rather than trusting a pasted QR alone.
5. Confirm the sending service shows the intended network, address, and amount.
6. Verify the transaction in the wallet and, where appropriate, independently on a block explorer or node without needlessly leaking wallet data.
7. Label the UTXO with source, purpose, date, and relevant records.

## Spend protocol

### Before constructing

- confirm recipient identity and payment instruction;
- confirm whether payment is time-sensitive;
- identify the appropriate fund layer and UTXOs;
- consider privacy created by combining inputs or producing change;
- obtain a current fee estimate from a trusted source/node.

### Before signing

- verify network, recipient address, amount, all outputs, change, absolute fee, and fee rate;
- question unexpected extra outputs, unknown scripts, unusually high fees, or “blind signing” warnings;
- use a second person/check for life-changing amounts;
- take a cooling-off interval unless delay itself increases risk.

### Before broadcasting

- verify the fully signed transaction still matches the approved intent;
- save the final PSBT/raw transaction only in accordance with the privacy plan;
- understand whether RBF is enabled and how confirmation will be monitored.

### After confirmation

- verify recipient and change outcomes;
- update labels, accounting, and remaining balances;
- remove temporary PSBT files from transport media when no longer required.

## Recovery drill protocol

1. Define a drill scope that cannot endanger the deep reserve.
2. Use a spare or factory-reset compatible device, or an offline verified environment.
3. Start with the documented inventory and recovery components—not the original live coordinator.
4. Reconstruct the wallet and verify several known receive/change addresses.
5. Rescan from the recorded birth date/height with an adequate gap limit.
6. Confirm expected UTXOs and wallet policy.
7. Complete a small signed transaction where safe.
8. Reset temporary devices and destroy temporary copies.
9. Record date, participants, result, problems, and remediation without recording secrets.

## Incident playbooks

### Backup missing or tamper seal broken

1. Treat possible copying as compromise, not merely physical loss.
2. Inventory whether spend authority or only metadata was exposed.
3. Secure unaffected components without co-locating them.
4. If a threshold may be reached—or a single-signature seed exposed—migrate to fresh keys.
5. Replace the location/process, not just the missing object.

### Backup damaged or partly unreadable

1. Do not repeatedly clean, stamp, heat, or chemically treat it.
2. Photograph only if the security environment permits and the image can be controlled; otherwise use offline magnification and transcription.
3. Work from copies of the transcription, never altering the original.
4. Use checksum constraints only in an offline, controlled recovery process.
5. Once recovered, create a fresh wallet if confidentiality may have been lost.

### Wrong address or amount before broadcast

Reject and rebuild from a clean, authenticated payment instruction. Investigate clipboard malware, coordinator compromise, and recipient-channel compromise.

### Wrong transaction already broadcast

Bitcoin transactions are generally irreversible. Preserve the transaction ID and evidence; contact the unintended recipient or relevant service immediately if identifiable. Do not pay “recovery hackers” or reveal seeds. If change or other keys may also be compromised, migrate remaining funds.

### Fee too low or transaction stuck

Confirm the transaction and mempool state through a trusted node. Use wallet-supported RBF or CPFP only after verifying the replacement/child transaction in full. Never paste private keys into a fee-acceleration site.

### Device failure during signing

Do not repeatedly initialise or enter seeds into random replacement software. Use the documented recovery route, verify the recreated wallet against known addresses/policy, and complete a small test before a material spend.

## Validation tests

- Every recovery component is inventoried and assigned a failure domain.
- The actual backup—not a duplicate prepared for the test—has successfully reconstructed the wallet.
- A descriptor/policy backup exists for every non-trivial wallet.
- Receive and change addresses can be verified independently.
- The fee-bump procedure is understood and has been tested with a small amount if required.
- Temporary digital recovery material has a destruction procedure.

## Residual risks

Testing can itself expose secrets; drills must be designed carefully. Geographic separation improves disaster resilience but delays access. Tamper evidence detects some interference but does not prove a secret was not copied. Recovery procedures must balance proof with minimal exposure.
