# Counterparty and Institutional Risk

[← Back to the framework](README.md)

## Objective

Preserve control and practical liquidity when an exchange, custodian, lender, stablecoin issuer, bank, wallet service, sidechain, or coordinator fails, freezes access, changes terms, is hacked, or acts under legal compulsion.

A displayed balance is not necessarily native bitcoin under the holder's keys. Identify whether each position is bitcoin, a contractual claim, a token, collateral, or a promise to deliver.

## Counterparty map

| Exposure | Principal failure | Limit |
| --- | --- | --- |
| Exchange balance | Insolvency, hack, freeze, withdrawal delay | Keep only operational balance; withdraw by threshold |
| Custodian | Censorship, legal seizure, recovery dependence | Understand control policy and exit route; avoid single-custodian totality |
| Lender/yield platform | Credit loss, rehypothecation, maturity mismatch | Do not expose deep reserve; assume principal can be lost |
| Stablecoin | Issuer/bank freeze or depeg | Treat as counterparty cash, not sovereign bitcoin |
| Wrapped/sidechain asset | Bridge, federation, contract, or peg failure | Use only for a defined purpose and capped amount |
| Bank | Transfer blocking or account closure | Maintain lawful records and more than one banking route where practical |
| Wallet/coordinator service | Surveillance, outage, policy substitution | Keep keys and complete recovery data independent of service |
| Cloud/email/phone account | Account takeover reveals identity or resets access | Strong authentication; do not make it a custody root |

## Due-diligence protocol

Before material exposure, determine:

1. What exact legal or technical claim is owned?
2. Who controls the private keys and how many parties can authorize movement?
3. Are client assets segregated, pledged, lent, or bankruptcy-remote?
4. What identity, source-of-funds, jurisdiction, and withdrawal restrictions apply?
5. Is there a tested path to native bitcoin or fiat outside the service?
6. What happens on death, account lock, lost phone, sanctions screening, or service closure?
7. Which records remain available if the website disappears?

Marketing statements and proof-of-reserves do not by themselves prove liabilities, governance, or redemption ability.

## Operating protocol for exchanges

1. Use a unique email alias where feasible, a password manager, and phishing-resistant multi-factor authentication.
2. Protect account-recovery email, phone, and identity documents to the same standard as the account.
3. Enable withdrawal allowlists and anti-phishing codes where offered, while retaining a tested method to add a fresh self-custody address.
4. Record account identity, jurisdiction/entity, deposit and withdrawal methods, and support route.
5. Define a maximum operational balance and/or maximum holding period.
6. Verify every withdrawal address on the destination signing device.
7. Preserve statements, trade confirmations, deposits, withdrawals, fees, and correspondence independently.
8. Test the full withdrawal path after long inactivity or a material platform change.

## Failure indicators

- withdrawal delays that are not explained by chain conditions;
- new limits, forced conversions, or abrupt changes of legal entity;
- banking partners leaving or persistent proof-of-reserve ambiguity;
- unusually high yield or incentives to lock funds;
- support asking for remote access, a seed phrase, or off-platform communication;
- login notifications, API keys, or reset requests not initiated by the holder;
- divergence between the platform's claimed transaction and public-chain data.

## Incident playbooks

### Withdrawal delayed or frozen

1. Do not send additional funds to “unlock” the withdrawal.
2. Capture balances, transaction references, timestamps, terms, and communications.
3. Confirm whether the delay is account-specific, network-wide, or legally imposed.
4. Use only authenticated support channels.
5. Attempt a small lawful withdrawal through an existing method if permitted.
6. Stop trading and new deposits until the cause is understood.
7. Escalate through the entity's formal complaint and relevant professional/legal route.

### Account takeover suspected

1. From a clean device, secure the primary email and password-manager account first.
2. Revoke sessions, API keys, and compromised recovery methods.
3. Contact the platform through a verified route and request protective restriction if needed.
4. Preserve notification emails and login data.
5. Review linked bank accounts, identity exposure, and other accounts using similar credentials.

### Custodian/service insolvency suspected

1. Stop deposits and preserve all evidence of title and transactions.
2. Do not accept an improvised token conversion without understanding legal effect.
3. Verify official notices through independent sources.
4. Obtain advice before actions that may waive claims or create tax consequences.

### Wallet coordinator unavailable

1. Do not reconstruct from seeds on an online computer.
2. Use the backed-up descriptor/policy and compatible wallet software to recreate a watch-only view.
3. Verify derived addresses against signer displays.
4. Build and sign via standard PSBT where supported.

## Validation tests

- Long-term holdings remain spendable without any one exchange or coordinator.
- A complete independent transaction history exists.
- Account recovery cannot be defeated by loss of one phone number.
- At least one self-custody withdrawal and one lawful liquidation route have been tested.
- The holder can explain which assets are native bitcoin and which are claims.

## Residual risks

Self-custody removes some institutional risks while adding personal operational risk. Multiple venues reduce concentration but expand identity exposure and attack surface. The aim is bounded, purposeful counterparty exposure—not the fiction of zero reliance on institutions.
