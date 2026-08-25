package com.gorunjinian.metrovault.core.ui.components

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorunjinian.metrovault.core.util.SecurityUtils

/**
 * Monospaced card showing a single copyable value (address, xpub, WIF key, …) with an
 * optional caption above it.
 *
 * Tapping copies [value] through [SecurityUtils.copyToClipboard]. When [sensitive] is
 * true the card is tinted with the error container colour *and* the clipboard entry is
 * auto-cleared after [SecurityUtils.SENSITIVE_CLIPBOARD_CLEAR_MS] — the two are bound
 * together deliberately so a screen cannot render a value as "secret" while leaving it
 * in the clipboard indefinitely.
 *
 * @param clipboardLabel label attached to the clipboard entry; also used in the toast
 * @param label caption rendered above the card; omit for a bare value card
 */
@Composable
fun CopyableValueCard(
    value: String,
    clipboardLabel: String,
    modifier: Modifier = Modifier,
    label: String? = null,
    sensitive: Boolean = false,
    enabled: Boolean = true,
    textStyle: TextStyle = MaterialTheme.typography.bodyMedium
) {
    val context = LocalContext.current

    Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label,
                style = MaterialTheme.typography.titleSmall,
                color = if (sensitive) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
        }
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (enabled) {
                        Modifier.clickable {
                            SecurityUtils.copyToClipboard(
                                context = context,
                                label = clipboardLabel,
                                text = value,
                                sensitive = sensitive
                            )
                            Toast.makeText(
                                context,
                                if (sensitive) "$clipboardLabel copied — clipboard clears in 20 seconds"
                                else "$clipboardLabel copied",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else Modifier
                ),
            colors = CardDefaults.cardColors(
                containerColor = if (sensitive) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.secondaryContainer
            )
        ) {
            Text(
                text = value,
                modifier = Modifier.padding(16.dp),
                style = textStyle,
                fontFamily = FontFamily.Monospace,
                fontWeight = if (sensitive) FontWeight.Medium else FontWeight.Normal
            )
        }
    }
}
