package com.gorunjinian.metrovault.feature.wallet.create.recovery

import com.gorunjinian.metrovault.lib.bitcoin.BIP39_ENGLISH_WORDLIST
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class RecoveryTextParserTest {
    private val validMnemonic = List(11) { "abandon" } + "about"

    @Test
    fun `full BIP39 words require valid count and checksum`() {
        val result = RecoveryTextParser.parseMnemonic(
            capturedItems = validMnemonic,
            format = RecoveryMnemonicFormat.WORDS,
            expectedWordCount = 12,
        )

        assertEquals(validMnemonic, result.words)
        assertTrue(result.problems.isEmpty())
        assertTrue(result.checksumValid)
        assertTrue(result.canConfirm)
    }

    @Test
    fun `unique four-letter abbreviations resolve deterministically`() {
        val abbreviated = List(11) { "aban" } + "abou"

        val result = RecoveryTextParser.parseMnemonic(
            capturedItems = abbreviated,
            format = RecoveryMnemonicFormat.WORDS,
            expectedWordCount = 12,
        )

        assertEquals(validMnemonic, result.words)
        assertTrue(result.canConfirm)
    }

    @Test
    fun `short or unknown words are never guessed`() {
        val result = RecoveryTextParser.parseMnemonic(
            capturedItems = List(11) { "abandon" } + "abo",
            format = RecoveryMnemonicFormat.WORDS,
            expectedWordCount = 12,
        )

        assertFalse(result.canConfirm)
        assertEquals(1, result.problems.size)
        assertEquals("abo", result.problems.single().token)
    }

    @Test
    fun `punctuated OCR token is rejected instead of normalized silently`() {
        val result = RecoveryTextParser.parseMnemonic(
            capturedItems = List(11) { "abandon" } + "about!",
            format = RecoveryMnemonicFormat.WORDS,
            expectedWordCount = 12,
        )

        assertFalse(result.canConfirm)
        assertTrue(result.problems.single().message.contains("non-letter"))
    }

    @Test
    fun `one review tile cannot silently expand into multiple words`() {
        val result = RecoveryTextParser.parseMnemonic(
            capturedItems = List(10) { "abandon" } + "abandon about",
            format = RecoveryMnemonicFormat.WORDS,
            expectedWordCount = 12,
        )

        assertFalse(result.canConfirm)
        assertEquals(1, result.problems.size)
        assertTrue(result.problems.single().message.contains("non-letter"))
    }

    @Test
    fun `zero-based indexes map exactly to BIP39 list`() {
        val indexes = List(11) { "0" } + "3"
        val result = RecoveryTextParser.parseMnemonic(
            capturedItems = indexes,
            format = RecoveryMnemonicFormat.INDEXES,
            expectedWordCount = 12,
            indexConvention = Bip39IndexConvention.ZERO_BASED,
        )

        assertEquals(validMnemonic, result.words)
        assertTrue(result.canConfirm)
    }

    @Test
    fun `one-based indexes map exactly to BIP39 list`() {
        val indexes = List(11) { "1" } + "4"
        val result = RecoveryTextParser.parseMnemonic(
            capturedItems = indexes,
            format = RecoveryMnemonicFormat.INDEXES,
            expectedWordCount = 12,
            indexConvention = Bip39IndexConvention.ONE_BASED,
        )

        assertEquals(validMnemonic, result.words)
        assertTrue(result.canConfirm)
    }

    @Test
    fun `index convention is mandatory`() {
        val result = RecoveryTextParser.parseMnemonic(
            capturedItems = List(12) { "1" },
            format = RecoveryMnemonicFormat.INDEXES,
            expectedWordCount = 12,
            indexConvention = null,
        )

        assertFalse(result.canConfirm)
        assertTrue(result.problems.single().message.contains("0-based or 1-based"))
    }

    @Test
    fun `index bounds follow selected convention`() {
        val zeroBased = RecoveryTextParser.parseMnemonic(
            capturedItems = List(11) { "0" } + "2048",
            format = RecoveryMnemonicFormat.INDEXES,
            expectedWordCount = 12,
            indexConvention = Bip39IndexConvention.ZERO_BASED,
        )
        val oneBased = RecoveryTextParser.parseMnemonic(
            capturedItems = List(11) { "1" } + "0",
            format = RecoveryMnemonicFormat.INDEXES,
            expectedWordCount = 12,
            indexConvention = Bip39IndexConvention.ONE_BASED,
        )

        assertFalse(zeroBased.canConfirm)
        assertFalse(oneBased.canConfirm)
        assertTrue(zeroBased.problems.single().message.contains("0 and 2047"))
        assertTrue(oneBased.problems.single().message.contains("1 and 2048"))
    }

    @Test
    fun `valid words with invalid checksum cannot be confirmed`() {
        val result = RecoveryTextParser.parseMnemonic(
            capturedItems = List(11) { "abandon" } + "ability",
            format = RecoveryMnemonicFormat.WORDS,
            expectedWordCount = 12,
        )

        assertTrue(result.problems.isEmpty())
        assertFalse(result.checksumValid)
        assertFalse(result.canConfirm)
    }

    @Test
    fun `literal passphrase preserves case spaces punctuation and unicode exactly`() {
        val literal = "  My PASS phrase! café\t\n"
        assertEquals(literal, RecoveryTextParser.preserveLiteralPassphrase(literal))
    }

    @Test
    fun `ASCII codes require explicit separator and preserve characters`() {
        val result = RecoveryTextParser.decodeAsciiCodes(
            value = "65,32,122,33",
            separator = AsciiCodeSeparator.COMMA,
        )

        assertTrue(result.isValid)
        assertEquals("A z!", result.value)
    }

    @Test
    fun `ASCII decoding rejects mixed separators and non-ASCII values`() {
        val mixed = RecoveryTextParser.decodeAsciiCodes(
            value = "65, 66",
            separator = AsciiCodeSeparator.COMMA,
        )
        val outOfRange = RecoveryTextParser.decodeAsciiCodes(
            value = "65-128",
            separator = AsciiCodeSeparator.HYPHEN,
        )

        assertFalse(mixed.isValid)
        assertNull(mixed.value)
        assertFalse(outOfRange.isValid)
        assertNull(outOfRange.value)
    }

    @Test
    fun `suggestions are constrained to BIP39 word list`() {
        val suggestions = RecoveryTextParser.suggestionsFor("abotu")

        assertTrue("about" in suggestions)
        assertTrue(suggestions.all { it in BIP39_ENGLISH_WORDLIST })
    }
}
