package com.gorunjinian.metrovault.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * Semantic severity of an [InfoCard]. Screens pick a tone rather than a colour so the
 * "is this a red warning or a grey note" decision stays consistent app-wide.
 */
enum class InfoTone {
    /** Plain explanatory copy. */
    Neutral,

    /** Guidance or next steps. */
    Info,

    /** Something to be careful about, but not destructive. */
    Warning,

    /** Secrets, irreversible actions, failures. */
    Danger,

    /** Callout that is neither advisory nor severe — just visually distinct. */
    Accent
}

@Composable
@ReadOnlyComposable
private fun InfoTone.container(): Color = when (this) {
    InfoTone.Neutral -> MaterialTheme.colorScheme.surfaceVariant
    InfoTone.Info -> MaterialTheme.colorScheme.primaryContainer
    InfoTone.Warning -> MaterialTheme.colorScheme.tertiaryContainer
    InfoTone.Danger -> MaterialTheme.colorScheme.errorContainer
    InfoTone.Accent -> MaterialTheme.colorScheme.secondaryContainer
}

/**
 * Full-width card holding a single block of explanatory text, tinted by [tone].
 *
 * The matching `on…Container` content colour is derived automatically by
 * [CardDefaults.cardColors], so callers should not set a text colour to restate it.
 *
 * Cards that pair a heading with body copy, or that mix in other composables, are
 * deliberately not covered here — build those from [Card] directly.
 *
 * @param containerAlpha tints the container down for a subdued variant; leave at 1f
 *   for the standard banner.
 */
@Composable
fun InfoCard(
    text: String,
    tone: InfoTone,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = LocalTextStyle.current,
    textAlign: TextAlign? = null,
    containerAlpha: Float = 1f
) {
    val container = tone.container()
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (containerAlpha == 1f) container
            else container.copy(alpha = containerAlpha)
        )
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(16.dp),
            style = textStyle,
            textAlign = textAlign
        )
    }
}
