package com.gorunjinian.metrovault.feature.wallet.create

/**
 * Validation for the BIP39 passphrase step shared by the create/import wizards.
 * Returns a user-facing error message, or null when the input is valid.
 */
fun validateBip39Passphrase(
    usePassphrase: Boolean,
    passphrase: String,
    confirmPassphrase: String
): String? = when {
    !usePassphrase -> null
    passphrase.isEmpty() -> "Please enter a BIP39 passphrase or turn off the toggle to continue without one"
    passphrase != confirmPassphrase -> "BIP39 passphrases do not match"
    else -> null
}
