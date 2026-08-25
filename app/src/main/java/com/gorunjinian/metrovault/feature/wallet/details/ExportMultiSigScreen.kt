package com.gorunjinian.metrovault.feature.wallet.details

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.gorunjinian.metrovault.R
import com.gorunjinian.metrovault.core.ui.components.MetroTopBar
import com.gorunjinian.metrovault.core.ui.components.SegmentedToggle
import com.gorunjinian.metrovault.domain.service.multisig.BSMS
import com.gorunjinian.metrovault.core.qr.AnimatedQRResult
import com.gorunjinian.metrovault.core.qr.ContentFormat
import com.gorunjinian.metrovault.core.qr.DescriptorQREncoder
import com.gorunjinian.metrovault.core.qr.OutputFormat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * ExportMultiSigScreen - Displays the multisig wallet descriptor as QR code.
 *
 * Supports two toggles:
 * 1. Content format: Descriptor (raw) or BSMS (formatted per BSMS 1.0 spec)
 * 2. QR encoding format: BC-UR v1, BBQr, BC-UR v2
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportMultiSigScreen(
    descriptor: String,
    firstAddress: String,
    onBack: () -> Unit
) {
    // Content format state: Descriptor or BSMS
    var selectedContentFormat by remember { mutableStateOf(ContentFormat.DESCRIPTOR) }
    
    // QR encoding format state
    var selectedQRFormat by remember { mutableStateOf(OutputFormat.BBQR) }
    
    // QR code result state
    var qrResult by remember { mutableStateOf<AnimatedQRResult?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    // Animation state for multi-frame QR
    var currentFrame by remember { mutableIntStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }
    
    // Prepare content based on selected content format
    val contentToEncode = remember(descriptor, selectedContentFormat, firstAddress) {
        when (selectedContentFormat) {
            ContentFormat.DESCRIPTOR -> descriptor
            // BSMS (BIP-0129) Descriptor Record: 4 lines, LF separated
            ContentFormat.BSMS -> BSMS.formatDescriptor(descriptor, firstAddress)
        }
    }
    
    // Security: Clear QR data when leaving the screen
    DisposableEffect(Unit) {
        onDispose {
            qrResult?.frames?.forEach { it.recycle() }
            qrResult = null
            System.gc()
        }
    }
    
    // Generate QR code when content or QR format changes
    LaunchedEffect(contentToEncode, selectedQRFormat, selectedContentFormat) {
        isLoading = true
        currentFrame = 0
        qrResult = withContext(Dispatchers.IO) {
            DescriptorQREncoder.encode(contentToEncode, selectedQRFormat, selectedContentFormat)
        }
        isLoading = false
    }
    
    // Auto-advance frames for animated QR
    LaunchedEffect(qrResult, isPaused) {
        val result = qrResult ?: return@LaunchedEffect
        if (!result.isAnimated || isPaused) return@LaunchedEffect
        
        while (true) {
            delay(result.recommendedFrameDelayMs.milliseconds)
            currentFrame = (currentFrame + 1) % result.frames.size
        }
    }

    Scaffold(
        topBar = {
            MetroTopBar(
                title = "Export Descriptor",
                onBack = onBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            
            // Content format toggle: Descriptor / BSMS
            SegmentedToggle(
                options = ContentFormat.entries.map { it.displayName },
                selectedIndex = ContentFormat.entries.indexOf(selectedContentFormat),
                onSelect = { index -> selectedContentFormat = ContentFormat.entries[index] },
                modifier = Modifier.fillMaxWidth()
            )
            
            // QR encoding format toggle: BC-UR v1 / BBQr / BC-UR v2
            SegmentedToggle(
                options = OutputFormat.entries.map { it.displayName },
                selectedIndex = OutputFormat.entries.indexOf(selectedQRFormat),
                onSelect = { index -> selectedQRFormat = OutputFormat.entries[index] },
                modifier = Modifier.fillMaxWidth()
            )
            
            // Info card about the descriptor
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Multisig wallet configuration. Import this descriptor into your coordinator wallet to watch or spend from this wallet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            // QR Code display
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
            ) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (qrResult != null && qrResult!!.frames.isNotEmpty()) {
                    val safeFrame = currentFrame.coerceIn(0, qrResult!!.frames.lastIndex)
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color.White
                        )
                    ) {
                        Image(
                            bitmap = qrResult!!.frames[safeFrame].asImageBitmap(),
                            contentDescription = "Descriptor QR Code",
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                } else {
                    Text(
                        text = "Failed to generate QR code",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            
            // Playback controls for animated QR
            if (qrResult?.isAnimated == true) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous frame button
                    IconButton(
                        onClick = {
                            val total = qrResult!!.frames.size
                            currentFrame = (currentFrame - 1 + total) % total
                        },
                        enabled = isPaused
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_left),
                            contentDescription = "Previous Frame",
                            modifier = Modifier.size(32.dp),
                            tint = if (isPaused) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                    
                    // Pause/Play button
                    FilledIconButton(
                        onClick = { isPaused = !isPaused },
                        modifier = Modifier.size(56.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = if (isPaused) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Icon(
                            painter = painterResource(if (isPaused) R.drawable.ic_play_arrow else R.drawable.ic_pause),
                            contentDescription = if (isPaused) "Play" else "Pause",
                            modifier = Modifier.size(32.dp),
                            tint = if (isPaused) MaterialTheme.colorScheme.onPrimary
                                   else MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                    
                    // Next frame button
                    IconButton(
                        onClick = {
                            val total = qrResult!!.frames.size
                            currentFrame = (currentFrame + 1) % total
                        },
                        enabled = isPaused
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_chevron_right),
                            contentDescription = "Next Frame",
                            modifier = Modifier.size(32.dp),
                            tint = if (isPaused) MaterialTheme.colorScheme.primary 
                                   else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                        )
                    }
                }
                
                // Frame counter
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "Frame ${currentFrame + 1}/${qrResult!!.totalParts}",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            // Done button
            Button(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Done")
            }
        }
    }
}

