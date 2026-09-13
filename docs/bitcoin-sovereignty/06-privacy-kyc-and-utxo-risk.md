# Privacy, KYC, and UTXO Risk

[← Back to the framework](README.md)

## Objective

Reduce unnecessary disclosure of identity, wealth, counterparties, locations, and behaviour while retaining spendability, accounting evidence, and lawful access to liquidity.

Bitcoin's ledger is public. Privacy is a property of the full transaction and information system, not a switch on a wallet.

## Information map

| Observer | What it may know | How knowledge expands |
| --- | --- | --- |
| KYC exchange | Identity, bank route, purchase and withdrawal | Address reuse, later consolidation, deposits back to venue |
| Recipient/merchant | Payment amount and input/output transaction | Common-input ownership assumptions, change heuristics |
| Wallet server | Queried addresses/scripts and network identity | Full descriptor/xpub queries, repeated IP linkage |
| Internet provider/network observer | Node/wallet connection timing and endpoints | Transaction broadcast correlation |
| Person finding xpub/descriptor | Past and future derived addresses and balances | Labels or identity data stored beside it |
| Physical attacker | Holder likely owns bitcoin | Public boasting, vendor leaks, home address linkage |
| Public chain analyst | Transaction graph | Address reuse, merges, round amounts, timing, change patterns |

## Risk register

| Event | Prevent | Detect | Response |
| --- | --- | --- | --- |
| Address reuse | Fresh receive address or reusable protocol designed not to reuse outputs | Same address appears more than once | Stop reuse; do not assume moving erases history |
| KYC linkage | Separate provenance; minimise unnecessary service data | Withdrawal address tied to identity | Maintain labels; control later merges and disclosure |
| UTXO consolidation | Coin control; planned UTXO sizes | Wallet selects unrelated inputs | Reject/rebuild transaction; quarantine merge if already confirmed |
| Xpub/descriptor leak | Limit copies; encrypt where appropriate; need-to-know access | Unknown watch-only view or data breach | Assess physical/privacy exposure; consider fresh wallet |
| Wallet-server surveillance | Trusted node; connection privacy; minimal queries | Unexpected endpoint or logs | Disconnect; reconfigure; assume queried wallet exposed |
| Public balance disclosure | Redacted demonstrations and screenshots | Address or fingerprint posted | Remove where possible; assume permanent copies; migrate privacy posture |
| CoinJoin/privacy-tool failure | Understand coordinator/model and legal/liquidity effects | Unequal outputs, toxic change, service rejection | Stop automatic merges; preserve provenance; reassess tool |

## Receiving protocol

1. Use a fresh ordinary on-chain address for each receipt. Current URI guidance continues to treat ordinary addresses as one-time payment instructions; reusable methods must be designed specifically to avoid address reuse.
2. Verify the address on a signing device for meaningful value.
3. Label the resulting UTXO by source, identity linkage, purpose, and sensitivity.
4. Avoid exposing the entire wallet/xpub merely to prove one address.
5. Use a separate receiving workflow for public donations or repeated counterparties rather than publishing a conventional static address.

[BIP352 Silent Payments](https://bips.dev/352/) defines static payment addresses that generate unique on-chain outputs, but support and scanning requirements must be verified in the actual wallet. Newer PSBT and descriptor work, including [BIP375](https://bips.dev/375/), [BIP376](https://bips.dev/376/), and [BIP392](https://bips.dev/392/), shows that interoperability is still evolving. Do not rely on a feature merely because the standard exists.

## KYC acquisition and withdrawal protocol

KYC bitcoin is linked to the account holder at acquisition. Sending it through an intermediate wallet does not erase that history.

1. Record the exchange source and withdrawal transaction privately.
2. Prefer a fresh hardware-wallet-controlled destination when exchange controls allow it.
3. If an allowlisted intermediary address is operationally unavoidable, do not reuse one address: use a wallet/service workflow that permits a fresh derived address under the approved ownership arrangement, or accept and document the privacy cost until the venue supports a better process.
4. Do not mix a high-volume acquisition transit wallet with everyday merchant spending. Small spends can reveal or help cluster much larger reserves.
5. Withdraw in economically sensible batches: too many small UTXOs create fee and consolidation pressure; very large repeated withdrawals can create obvious patterns.
6. Label every withdrawal before later spending.

## Spending and coin-control protocol

1. Identify each candidate UTXO's source, KYC status, counterparty, and sensitivity.
2. Choose inputs that reveal no more than necessary to the recipient.
3. Avoid combining UTXOs whose common ownership should remain unlinked.
4. Review change: amount, address ownership, and whether later spending will link the transaction onward.
5. Do not spend a large reserve input for a tiny payment if a spending-layer UTXO is available.
6. Preserve labels after wallet migration using a supported export format; remember that labels themselves are sensitive.

## Node and network protocol

Running a fully validating node protects verification and can reduce disclosure to third-party wallet servers. It does not automatically provide network anonymity.

- Connect the wallet only to the intended node and verify the endpoint/certificate configuration.
- Restrict remote access; do not expose unauthenticated Electrum or RPC services to the public internet.
- Keep Bitcoin Core and indexer software supported and monitored.
- Understand whether connections and transaction broadcasts use the clearnet, Tor, VPN, or another path, and what each operator can observe.
- Avoid querying the same wallet across multiple public explorers and servers.
- Test failover deliberately; a wallet silently falling back to a vendor server changes the privacy model.

## CoinJoin or collaborative-transaction decision protocol

Before use, determine:

1. the exact privacy objective and adversary;
2. coordinator and wallet trust assumptions;
3. input, output, change, and fee structure;
4. whether the holder can avoid recombining outputs afterwards;
5. legal, tax, exchange, and liquidity consequences;
6. software maintenance and anonymity-set quality;
7. recovery and labelling requirements.

A collaborative transaction can improve some on-chain ambiguity but cannot erase KYC records, network logs, prior address reuse, or later consolidation. Poor post-mix behaviour can undo much of its value.

## Incident playbooks

### Xpub or descriptor leaked

1. Determine precisely which account/branches and labels were exposed.
2. Assume historical and future derived-address visibility for the exposed scope.
3. Protect physical identity/location information that could be combined with balances.
4. Stop distributing the exposed metadata and revoke cloud/share access.
5. Decide whether to migrate to a fresh wallet; migration itself creates an on-chain link unless designed carefully.

### Address posted publicly

1. Remove it where possible but assume copies remain.
2. Stop reusing it.
3. Avoid consolidating associated UTXOs with unrelated reserves.
4. Move future receipts to fresh payment instructions; do not rush a revealing sweep merely for cosmetic address change.

### Unwanted UTXO merge broadcast

The link cannot be removed from the ledger. Label the merged history, avoid expanding the cluster further, reassess future change spending, and update the operational checklist that allowed the merge.

### Node or wallet server exposed

Disconnect the service, preserve logs, patch/authenticate before re-enabling, and assume observed wallet queries/IP data may be compromised. Private keys should not be on the node; if they were, treat this as key compromise too.

## Validation tests

- No ordinary receive address is deliberately reused.
- Every UTXO has enough provenance to support coin selection and accounting.
- Spending and deep-reserve wallets are operationally separated.
- The wallet does not silently use an unintended public server.
- Remote node/indexer services require appropriate authentication and network restriction.
- Xpubs, descriptors, labels, and screenshots are handled as sensitive data.
- The holder can explain what privacy a chosen tool does and does not provide.

## Residual risks

Blockchain data is permanent and privacy heuristics evolve. Stronger privacy can reduce regulated liquidity or create recordkeeping burdens. Running a node changes who is trusted but does not eliminate network metadata. Perfect privacy is not a realistic control objective; deliberate minimisation is.

## Technical references

- [BIP329: Wallet Labels Export Format](https://bips.dev/329/)
- [BIP352: Silent Payments](https://bips.dev/352/)
- [BIP392: Silent Payment Output Descriptors](https://bips.dev/392/)
- [BIP321: Bitcoin URI Scheme](https://bips.dev/321/)
