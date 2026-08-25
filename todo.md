# BitSawan roadmap

## Enhanced physical entropy input

Physical entropy remains optional. The recommended mode mixes it with secure device randomness; the explicit reproducible mode uses it alone only after enforcing the selected strength threshold. Hashing normalizes an input representation and must never be presented as creating additional entropy. Freeze every serialization with explicit source/version domain separation, document it, and protect it with deterministic test vectors.

- [x] Keep `Device + physical` as the recommended default.
- [x] Add an explicit reproducible `Physical only` mode that never adds device randomness and is blocked below the selected 128/256-bit estimated source threshold.
- [x] Preserve offline-converter compatibility for `COIN:` and `DICE:` deterministic inputs and freeze a versioned Cards format.

### Coin Toss

- [x] Support Heads/Tails entry.
- [x] Provide remove-last/undo and reset controls, live toss count, and estimated entropy progress.
- [x] Freeze and document deterministic conversion and add fixed test vectors.

### Dice Rolls

- [x] Support six-sided die results 1–6.
- [x] Provide remove-last/undo and reset controls plus the live sequence and count.
- [x] Show estimated entropy as `n * log2(6)`.
- [x] Review MetroVault's current bit-packing conversion for bias, discarded entropy, and compatibility before preserving or changing it.
- [x] Freeze and document deterministic conversion and add fixed test vectors.

### Cards

- [x] Add `Cards` to the `Coin Toss | Dice Rolls | Cards` source selector.
- [x] Model a standard 52-card deck with suits in a frozen canonical order, ranks `A,2,3,4,5,6,7,8,9,10,J,Q,K`, and canonical IDs `0..51`.
- [x] Default to **with replacement**: allow repeats and estimate `n * log2(52)` bits (about 23 draws for 128 bits and 45 for 256 bits).
- [x] Instruct with-replacement users to return each drawn card to the bottom and make a fresh random 1–5 cuts/splits at unpredictable positions before the next draw.
- [x] Support **without replacement**: disable/reject used cards and estimate `log2(52! / (52-n)!)`. Document that a full deck provides about 225.58 bits and cannot independently supply 256 bits.
- [x] Show the replacement toggle only for Cards; show the selected sequence, remove-last/undo, reset, and exact/estimated progress in a mobile-friendly Compose layout.
- [x] Do not treat card IDs as uniformly distributed bytes. Define canonical serialization, source/version domain separation, and SHA-256 normalization before feeding the optional entropy into wallet generation.
- [x] Add deterministic vectors and tests for duplicates, mode changes, entropy boundaries, and all serialization/hash formats.

## Camera-assisted wallet recovery (future work; do not implement yet)

Build an entirely offline, airplane-mode-compatible recovery assistant with manual confirmation/editing at every stage. Never use cloud OCR or a network dependency, and investigate support for older Android and Samsung hardware.

### Seed words and indexes

- [ ] Investigate multiple photos of paper or metal washers containing full English BIP39 words or safely resolvable abbreviations, including arbitrary orientation, curved/reflected steel, glare, and difficult lighting.
- [ ] Add crop, rotation, perspective correction, contrast/glare preprocessing, confidence-ranked candidate readings, BIP39-dictionary constraints, and checksum-assisted validation.
- [ ] Support decimal BIP39 word indexes only after explicitly selecting 0-based or 1-based input; never guess the convention.
- [ ] Validate every reconstructed mnemonic against the BIP39 English list and checksum, but never accept a reconstruction solely because its checksum passes. Clearly distinguish OCR output from user-confirmed input.

### BIP39 passphrase

- [ ] Investigate literal ASCII/text passphrases and numeric ASCII character-code representations.
- [ ] Define unambiguous separators and encoding conventions before implementation.
- [ ] Preserve exact case, spaces, and punctuation; never autocorrect; require explicit user verification before derivation.

### Security, architecture, and tests

- [ ] Evaluate fully offline Android-compatible OCR and image-processing libraries for size, hardware support, licensing, update provenance, and behavior without bundled network services.
- [ ] Do not log or store photos, seeds, passphrases, private keys, or OCR results unless the user explicitly requests storage. Wipe sensitive temporary data where practical.
- [ ] Threat-model screenshots, clipboard use, recent-app previews, crash reporting, caches, temporary files, and Android backup/restore.
- [ ] Add tests using synthetic washer images and consented real-world-style washer images covering rotation, blur, glare, curvature, partial words, ambiguous characters, multiple washers, and low-confidence results.
