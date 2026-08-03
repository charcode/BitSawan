# Donation Addresses

This file is the canonical list of MetroVault donation addresses. It exists so that the addresses shown on [metrovault.app/donate](https://metrovault.app/donate/) can be verified against a second, independently hosted source. A donation page is a tampering target — if this file and the website ever disagree, **do not send anything** and [open an issue](https://github.com/gorunjinian/MetroVault/issues).

## Addresses

### Silent Payments (BIP-352) — preferred

```
sp1qqt8skdfnn4xytf9yq05hg2zhf0fctrlwk4jeprhpyp4k2rj3m83j7qk8zt5paevz4gte3nld8m9vqdspwegwsjyqa2043rw7sd4wh2dh3qpewwnr
```

One reusable address with no on-chain footprint — donations cannot be linked to this page or to each other. Requires a wallet that can send to Silent Payments addresses (Cake Wallet, Sparrow).

### Lightning

```
donate@metrovault.app
```

For small donations. Resolves via LNURL-pay at `https://metrovault.app/.well-known/lnurlp/donate`.

### On-chain (fallback)

```
bc1qmgprkfwrmapv2rth9fl7ccql8pua6342pc97np
```

A standard bech32 address for wallets without Silent Payments support. Note that this address is reused, so donations to it are publicly linkable on-chain.

## BIP-322 Signature

The address list is also signed with the key that controls the on-chain donation address, using [BIP-322](https://github.com/bitcoin/bips/blob/master/bip-0322.mediawiki). This proves the person who controls the donation funds published this exact list — independently of GitHub, the website, and the maintainer's GPG key.

**Signed message** — exactly these four lines, LF line endings, no trailing newline:

```
MetroVault donation addresses as of 2026-08-03:
Silent Payments: sp1qqt8skdfnn4xytf9yq05hg2zhf0fctrlwk4jeprhpyp4k2rj3m83j7qk8zt5paevz4gte3nld8m9vqdspwegwsjyqa2043rw7sd4wh2dh3qpewwnr
Lightning: donate@metrovault.app
On-chain: bc1qmgprkfwrmapv2rth9fl7ccql8pua6342pc97np
```

**Signing address:** `bc1qmgprkfwrmapv2rth9fl7ccql8pua6342pc97np`

**Signature (base64):**

```
AkcwRAIgGcdmpEOVICz+Q+TBRAjjRL55eYYQgzK6JCdFbuuhKUMCICE6DgTLBfHxyq+GsQ7TcGhsM/sVps5q+7bR9M2eiJKRASEDLwqwPMlGIrUeM0Ng+NTnxTmVEbPHxk2SJupDj4O7Ab8=
```

To verify: in Sparrow Wallet, open Tools → Sign/Verify Message, paste the signing address, the message, and the signature, then press Verify. MetroVault itself can also verify it (wallet → Sign Message → verify), as can any BIP-322-capable tool.

## How to Verify

1. **Compare with the website.** Every character of the addresses above must match [metrovault.app/donate](https://metrovault.app/donate/) exactly.
2. **Check the signature on this file's history.** Every commit touching this file is GPG-signed with the maintainer's key `624FEAE4FAC1F416139743034B9D1B39C9693E6D` — the same key that signs all commits in this repository:

   ```
   git log --show-signature -- docs/ADDRESSES.md
   ```

   An unsigned or differently-signed change to this file should be treated as compromise of the GitHub account, not as a legitimate update.
