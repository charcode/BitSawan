package com.gorunjinian.metrovault.feature.wallet.create.recovery

import com.gorunjinian.metrovault.lib.bitcoin.BIP39_ENGLISH_WORDLIST
import com.gorunjinian.metrovault.lib.bitcoin.MnemonicCode
import kotlin.math.min

enum class RecoveryMnemonicFormat {
    WORDS,
    INDEXES,
}

enum class Bip39IndexConvention {
    ZERO_BASED,
    ONE_BASED,
}

enum class AsciiCodeSeparator(val delimiter: Char) {
    SPACE(' '),
    COMMA(','),
    HYPHEN('-'),
}

data class RecoveryTokenProblem(
    val position: Int,
    val token: String,
    val message: String,
    val suggestions: List<String> = emptyList(),
)

data class RecoveryMnemonicResult(
    val words: List<String>,
    val problems: List<RecoveryTokenProblem>,
    val expectedWordCount: Int,
    val checksumValid: Boolean,
) {
    val hasExpectedWordCount: Boolean get() = words.size == expectedWordCount
    val canConfirm: Boolean get() = problems.isEmpty() && hasExpectedWordCount && checksumValid
}

data class AsciiDecodeResult(
    val value: String?,
    val error: String?,
) {
    val isValid: Boolean get() = value != null && error == null
}

/**
 * Strict, deterministic conversion for user-reviewed recovery text.
 *
 * OCR confidence is deliberately outside this class. Machine output must first be
 * shown to and confirmed by the user; this parser only determines whether the
 * confirmed tokens have one unambiguous BIP39 interpretation.
 */
object RecoveryTextParser {
    private val decimal = Regex("[0-9]+")
    private val letters = Regex("[A-Za-z]+")

    fun parseMnemonic(
        capturedItems: List<String>,
        format: RecoveryMnemonicFormat,
        expectedWordCount: Int,
        indexConvention: Bip39IndexConvention? = null,
        allowUniquePrefixes: Boolean = true,
        wordList: List<String> = BIP39_ENGLISH_WORDLIST,
    ): RecoveryMnemonicResult {
        require(wordList.size == 2048) { "BIP39 word list must contain 2048 words" }
        require(expectedWordCount in setOf(12, 15, 18, 21, 24)) {
            "Unsupported BIP39 word count"
        }

        // One reviewed tile represents exactly one word/index. Never silently split a
        // tile into several secrets because that would make positions and confirmations
        // ambiguous in the review UI.
        val tokens = capturedItems.map(String::trim)

        val problems = mutableListOf<RecoveryTokenProblem>()
        val words = when (format) {
            RecoveryMnemonicFormat.WORDS -> tokens.mapIndexedNotNull { index, token ->
                resolveWordToken(
                    token = token,
                    position = index + 1,
                    allowUniquePrefixes = allowUniquePrefixes,
                    wordList = wordList,
                    problems = problems,
                )
            }

            RecoveryMnemonicFormat.INDEXES -> {
                if (indexConvention == null) {
                    problems += RecoveryTokenProblem(
                        position = 0,
                        token = "",
                        message = "Choose whether indexes are 0-based or 1-based",
                    )
                    emptyList()
                } else {
                    tokens.mapIndexedNotNull { index, token ->
                        resolveIndexToken(
                            token = token,
                            position = index + 1,
                            convention = indexConvention,
                            wordList = wordList,
                            problems = problems,
                        )
                    }
                }
            }
        }

        val checksumValid = problems.isEmpty() &&
            words.size == expectedWordCount &&
            try {
                MnemonicCode.validate(words)
                true
            } catch (_: Exception) {
                false
            }

        return RecoveryMnemonicResult(
            words = words,
            problems = problems,
            expectedWordCount = expectedWordCount,
            checksumValid = checksumValid,
        )
    }

    /** A literal BIP39 passphrase must never be trimmed, normalized, or corrected. */
    fun preserveLiteralPassphrase(value: String): String = value

    /**
     * Decodes an explicitly selected, 7-bit ASCII representation.
     * The chosen separator is the only accepted separator; mixed formats fail.
     */
    fun decodeAsciiCodes(value: String, separator: AsciiCodeSeparator): AsciiDecodeResult {
        if (value.isEmpty()) return AsciiDecodeResult(null, "Enter at least one ASCII code")
        if (value.first() == separator.delimiter || value.last() == separator.delimiter) {
            return AsciiDecodeResult(null, "ASCII codes cannot start or end with the separator")
        }

        val tokens = value.split(separator.delimiter)
        if (tokens.any { it.isEmpty() || !decimal.matches(it) }) {
            return AsciiDecodeResult(null, "Use decimal ASCII codes with only the selected separator")
        }

        val output = StringBuilder(tokens.size)
        tokens.forEachIndexed { index, token ->
            val code = token.toIntOrNull()
                ?: return AsciiDecodeResult(null, "ASCII code ${index + 1} is invalid")
            if (code !in 0..127) {
                return AsciiDecodeResult(null, "ASCII code ${index + 1} must be between 0 and 127")
            }
            output.append(code.toChar())
        }
        return AsciiDecodeResult(output.toString(), null)
    }

    fun suggestionsFor(
        token: String,
        wordList: List<String> = BIP39_ENGLISH_WORDLIST,
        limit: Int = 3,
    ): List<String> {
        val normalized = token.lowercase()
        if (normalized.isBlank()) return emptyList()
        val prefixMatches = wordList.filter { it.startsWith(normalized) }
        if (prefixMatches.isNotEmpty()) return prefixMatches.take(limit)

        val threshold = if (normalized.length <= 4) 1 else 2
        return wordList.asSequence()
            .map { word -> word to levenshtein(normalized, word) }
            .filter { (_, distance) -> distance <= threshold }
            .sortedWith(compareBy<Pair<String, Int>> { it.second }.thenBy { it.first })
            .map { it.first }
            .take(limit)
            .toList()
    }

    private fun resolveWordToken(
        token: String,
        position: Int,
        allowUniquePrefixes: Boolean,
        wordList: List<String>,
        problems: MutableList<RecoveryTokenProblem>,
    ): String? {
        val normalized = token.lowercase()
        if (!letters.matches(token)) {
            problems += RecoveryTokenProblem(
                position = position,
                token = token,
                message = "Word $position contains non-letter characters",
                suggestions = suggestionsFor(token.filter(Char::isLetter), wordList),
            )
            return null
        }
        if (normalized in wordList) return normalized

        if (allowUniquePrefixes && normalized.length >= 4) {
            val matches = wordList.filter { it.startsWith(normalized) }
            if (matches.size == 1) return matches.single()
            if (matches.size > 1) {
                problems += RecoveryTokenProblem(
                    position = position,
                    token = token,
                    message = "Word $position is an ambiguous abbreviation",
                    suggestions = matches.take(3),
                )
                return null
            }
        }

        problems += RecoveryTokenProblem(
            position = position,
            token = token,
            message = "Word $position is not in the BIP39 English list",
            suggestions = suggestionsFor(normalized, wordList),
        )
        return null
    }

    private fun resolveIndexToken(
        token: String,
        position: Int,
        convention: Bip39IndexConvention,
        wordList: List<String>,
        problems: MutableList<RecoveryTokenProblem>,
    ): String? {
        if (!decimal.matches(token)) {
            problems += RecoveryTokenProblem(
                position = position,
                token = token,
                message = "Index $position is not a decimal number",
            )
            return null
        }
        val rawIndex = token.toIntOrNull()
        val validRange = when (convention) {
            Bip39IndexConvention.ZERO_BASED -> 0..2047
            Bip39IndexConvention.ONE_BASED -> 1..2048
        }
        if (rawIndex == null || rawIndex !in validRange) {
            problems += RecoveryTokenProblem(
                position = position,
                token = token,
                message = when (convention) {
                    Bip39IndexConvention.ZERO_BASED -> "Index $position must be between 0 and 2047"
                    Bip39IndexConvention.ONE_BASED -> "Index $position must be between 1 and 2048"
                },
            )
            return null
        }
        val zeroBased = when (convention) {
            Bip39IndexConvention.ZERO_BASED -> rawIndex
            Bip39IndexConvention.ONE_BASED -> rawIndex - 1
        }
        return wordList[zeroBased]
    }

    private fun levenshtein(left: String, right: String): Int {
        if (left.isEmpty()) return right.length
        if (right.isEmpty()) return left.length
        var previous = IntArray(right.length + 1) { it }
        var current = IntArray(right.length + 1)
        left.forEachIndexed { leftIndex, leftChar ->
            current[0] = leftIndex + 1
            right.forEachIndexed { rightIndex, rightChar ->
                current[rightIndex + 1] = min(
                    min(current[rightIndex] + 1, previous[rightIndex + 1] + 1),
                    previous[rightIndex] + if (leftChar == rightChar) 0 else 1,
                )
            }
            val swap = previous
            previous = current
            current = swap
        }
        return previous[right.length]
    }
}
