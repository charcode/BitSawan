package com.gorunjinian.metrovault.core.ui.components

import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gorunjinian.metrovault.core.qr.QRCodeUtils
import com.gorunjinian.metrovault.core.util.SecurityUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Square QR card shared by the key/descriptor export screens, with an optional
 * "Tap QR code to copy" caption below it.
 *
 * Generates the QR bitmap off the main thread whenever [data] changes (showing a
 * progress indicator meanwhile) and recycles it when the card leaves composition.
 * Tapping copies [data] to the clipboard with a 20-second auto-clear.
 *
 * @param emptyContent shown instead of the QR when [data] is empty (e.g. the
 *   export is unavailable for this wallet). Without it an empty [data] just
 *   keeps the progress indicator, matching the previous per-screen behavior.
 */
@Composable
fun TapToCopyQRCard(
    data: String,
    clipboardLabel: String,
    tapToCopyEnabled: Boolean,
    contentDescription: String,
    modifier: Modifier = Modifier,
    emptyContent: (@Composable () -> Unit)? = null
) {
    val context = LocalContext.current
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(data) {
        if (data.isNotEmpty()) {
            qrBitmap = null // Show loading
            withContext(Dispatchers.IO) {
                qrBitmap = QRCodeUtils.generateQRCode(data)
            }
        }
    }

    // Security: don't leave the QR bitmap in memory after leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            qrBitmap?.recycle()
            qrBitmap = null
            System.gc() // Hint to garbage collector
        }
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
        ) {
            val qr = qrBitmap
            when {
                data.isEmpty() && emptyContent != null -> emptyContent()
                qr != null -> {
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .then(
                                if (tapToCopyEnabled) {
                                    Modifier.clickable {
                                        SecurityUtils.copyToClipboardWithAutoClear(
                                            context = context,
                                            label = clipboardLabel,
                                            text = data,
                                            delayMs = 20_000
                                        )
                                        Toast.makeText(
                                            context,
                                            "Copied! Clipboard will clear in 20 seconds",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                } else Modifier
                            )
                    ) {
                        Image(
                            bitmap = qr.asImageBitmap(),
                            contentDescription = contentDescription,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
                else -> CircularProgressIndicator()
            }
        }

        if (tapToCopyEnabled && data.isNotEmpty()) {
            Text(
                text = "Tap QR code to copy",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
