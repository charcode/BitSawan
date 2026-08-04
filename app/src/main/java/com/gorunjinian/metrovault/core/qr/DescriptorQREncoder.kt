package com.gorunjinian.metrovault.core.qr

import com.gorunjinian.bbqr.FileType
import com.gorunjinian.bbqr.SplitResult
import com.gorunjinian.bcur.UR
import com.gorunjinian.bcur.UREncoder
import com.gorunjinian.bcur.registry.UROutputDescriptor
import com.gorunjinian.metrovault.core.logging.AppLog

/**
 * Content format options for multisig descriptor export.
 */
enum class ContentFormat(val displayName: String) {
    DESCRIPTOR("Descriptor"),
    BSMS("BSMS")
}

/**
 * Encodes multisig wallet descriptors (raw or BSMS-formatted) into single-frame
 * or animated QR codes. Counterpart to [DescriptorQRScanner].
 *
 * Supported QR encodings:
 *  - BC-UR v1: `ur:bytes/` for broad compatibility with legacy wallets
 *  - BBQr: multi-frame BBQr with low density
 *  - BC-UR v2: `ur:output-descriptor/` for raw descriptors (UR 2.0 standard),
 *    `ur:bytes/` for BSMS multi-line text
 */
object DescriptorQREncoder {

    private const val TAG = "DescriptorQREncoder"
    private const val QR_SIZE = 512

    fun encode(
        content: String,
        format: OutputFormat,
        contentFormat: ContentFormat
    ): AnimatedQRResult? {
        return when (format) {
            OutputFormat.UR_LEGACY -> encodeAsUR(content, format) {
                UR.fromBytes(content.toByteArray(Charsets.UTF_8))
            }
            OutputFormat.BBQR -> encodeAsBBQr(content)
            OutputFormat.UR_MODERN -> encodeAsUR(content, format) {
                when (contentFormat) {
                    // UROutputDescriptor encodes as CBOR map with SOURCE key
                    ContentFormat.DESCRIPTOR -> UROutputDescriptor(content).toUR()
                    ContentFormat.BSMS -> UR.fromBytes(content.toByteArray(Charsets.UTF_8))
                }
            }
        }
    }

    /**
     * Shared BC-UR encode path — v1 and v2 differ only in how the UR is built.
     * Falls back to a plain-text QR when UR encoding fails.
     */
    private fun encodeAsUR(
        content: String,
        format: OutputFormat,
        buildUr: () -> UR
    ): AnimatedQRResult? {
        return try {
            val encoder = UREncoder(buildUr(), 250, 50, 0)

            if (encoder.isSinglePart()) {
                val urString = encoder.nextPart()
                QRCodeGenerator.generateQRCode(urString.uppercase(), size = QR_SIZE)?.let {
                    AnimatedQRResult(
                        frames = listOf(it),
                        totalParts = 1,
                        isAnimated = false,
                        format = format
                    )
                }
            } else {
                val seqLen = encoder.seqLen
                val frameStrings = mutableListOf<String>()
                repeat(seqLen) {
                    frameStrings.add(encoder.nextPart().uppercase())
                }

                QRCodeGenerator.generateConsistentQRCodes(frameStrings, size = QR_SIZE)?.let {
                    AnimatedQRResult(
                        frames = it,
                        totalParts = it.size,
                        isAnimated = true,
                        recommendedFrameDelayMs = 500,
                        format = format
                    )
                }
            }
        } catch (e: Exception) {
            AppLog.e(TAG) { "${format.displayName} generation failed: ${e.message}" }
            // Fall back to plain text
            QRCodeGenerator.generateQRCode(content, size = QR_SIZE)?.let {
                AnimatedQRResult(
                    frames = listOf(it),
                    totalParts = 1,
                    isAnimated = false,
                    format = format
                )
            }
        }
    }

    private fun encodeAsBBQr(descriptor: String): AnimatedQRResult? {
        return try {
            val descriptorBytes = descriptor.toByteArray(Charsets.UTF_8)
            val options = DensitySettings.getBBQrSplitOptions(QRDensity.LOW)
            AppLog.d(TAG) { "BBQr descriptor: ${descriptorBytes.size} bytes" }

            val splitResult = SplitResult.fromData(descriptorBytes, FileType.UnicodeText, options)
            val frameContents = splitResult.parts

            AppLog.d(TAG) { "BBQr descriptor: ${frameContents.size} frames (version=${splitResult.version})" }

            val bitmaps = if (frameContents.size > 1) {
                QRCodeGenerator.generateConsistentQRCodes(frameContents, size = QR_SIZE)
            } else {
                frameContents.mapNotNull { frame ->
                    QRCodeGenerator.generateQRCode(frame, size = QR_SIZE)
                }
            }

            bitmaps?.let {
                AnimatedQRResult(
                    frames = it,
                    totalParts = it.size,
                    isAnimated = it.size > 1,
                    recommendedFrameDelayMs = 500,
                    format = OutputFormat.BBQR
                )
            }
        } catch (e: Exception) {
            AppLog.e(TAG) { "BBQr generation failed: ${e.message}" }
            null
        }
    }
}
