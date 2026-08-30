package com.gorunjinian.metrovault.feature.wallet.create.recovery

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Matrix
import android.graphics.Paint
import android.view.Surface as AndroidSurface
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.gorunjinian.metrovault.R
import com.gorunjinian.metrovault.core.ui.components.InfoCard
import com.gorunjinian.metrovault.core.ui.components.InfoTone
import com.gorunjinian.metrovault.core.ui.components.MetroTopBar
import com.gorunjinian.metrovault.core.ui.components.SecureOutlinedTextField
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.util.concurrent.atomic.AtomicBoolean

private data class RecoveryCaptureItem(
    val id: Long,
    val text: String,
    val confirmed: Boolean,
    val fromPhoto: Boolean,
)

private data class RecoveryPhoto(
    val id: Long,
    val bitmap: Bitmap,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfflineMnemonicRecoveryDialog(
    expectedWordCount: Int,
    onDismiss: () -> Unit,
    onConfirmed: (List<String>) -> Unit,
) {
    val context = LocalContext.current
    val items = remember { mutableStateListOf<RecoveryCaptureItem>() }
    val photos = remember { mutableStateListOf<RecoveryPhoto>() }
    val pendingBitmapWipes = remember { mutableStateListOf<Bitmap>() }
    var nextId by remember { mutableLongStateOf(1L) }
    var nextPhotoId by remember { mutableLongStateOf(1L) }
    var selectedPhotoId by remember { mutableStateOf<Long?>(null) }
    var showCamera by remember { mutableStateOf(true) }
    var format by remember { mutableStateOf(RecoveryMnemonicFormat.WORDS) }
    var indexConvention by remember { mutableStateOf<Bip39IndexConvention?>(null) }
    var pendingFormat by remember { mutableStateOf<RecoveryMnemonicFormat?>(null) }
    var showDiscardDialog by remember { mutableStateOf(false) }
    var captureError by remember { mutableStateOf<String?>(null) }

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) captureError = "Camera permission is required for photo recovery"
    }

    val parsed by remember {
        derivedStateOf {
            RecoveryTextParser.parseMnemonic(
                capturedItems = items.map { it.text },
                format = format,
                expectedWordCount = expectedWordCount,
                indexConvention = indexConvention,
            )
        }
    }
    val everyItemConfirmed by remember {
        derivedStateOf { items.isNotEmpty() && items.all { it.confirmed } }
    }

    fun requestDismiss() {
        if (items.isEmpty() && photos.isEmpty()) onDismiss() else showDiscardDialog = true
    }

    BackHandler { requestDismiss() }

    DisposableEffect(Unit) {
        onDispose {
            // Compose strings are immutable, but removing every reference keeps their
            // lifetime bounded to this non-saveable recovery session.
            items.clear()
            photos.forEach { wipeBitmap(it.bitmap) }
            photos.clear()
            pendingBitmapWipes.forEach(::wipeBitmap)
            pendingBitmapWipes.clear()
            captureError = null
        }
    }

    LaunchedEffect(pendingBitmapWipes.size) {
        if (pendingBitmapWipes.isNotEmpty()) {
            androidx.compose.runtime.withFrameNanos { }
            pendingBitmapWipes.toList().forEach(::wipeBitmap)
            pendingBitmapWipes.clear()
        }
    }

    Dialog(
        onDismissRequest = ::requestDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
            decorFitsSystemWindows = false,
        ),
    ) {
        Surface(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                topBar = {
                    MetroTopBar(
                        title = "Offline Photo Recovery",
                        onBack = ::requestDismiss,
                    )
                },
            ) { padding ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    InfoCard(
                        text = "Photos and transcribed text stay only in this session. Nothing is saved or sent anywhere. Manually transcribe and confirm every item.",
                        tone = InfoTone.Warning,
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        RecoveryMnemonicFormat.entries.forEach { choice ->
                            FilterChip(
                                selected = format == choice,
                                onClick = {
                                    if (choice != format) {
                                        if (items.isEmpty()) {
                                            format = choice
                                            indexConvention = null
                                        } else {
                                            pendingFormat = choice
                                        }
                                    }
                                },
                                label = {
                                    Text(if (choice == RecoveryMnemonicFormat.WORDS) "BIP39 words" else "Word indexes")
                                },
                            )
                        }
                    }

                    if (format == RecoveryMnemonicFormat.INDEXES) {
                        Text("Choose the index convention", fontWeight = FontWeight.SemiBold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Bip39IndexConvention.entries.forEach { convention ->
                                FilterChip(
                                    selected = indexConvention == convention,
                                    onClick = {
                                        indexConvention = convention
                                        items.indices.forEach { index ->
                                            items[index] = items[index].copy(confirmed = false)
                                        }
                                    },
                                    label = {
                                        Text(
                                            if (convention == Bip39IndexConvention.ZERO_BASED) {
                                                "0-based (0–2047)"
                                            } else {
                                                "1-based (1–2048)"
                                            },
                                        )
                                    },
                                )
                            }
                        }
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp),
                        shape = RoundedCornerShape(16.dp),
                    ) {
                        val selectedPhoto = photos.firstOrNull { it.id == selectedPhotoId }
                        if (!showCamera && selectedPhoto != null) {
                            PhotoReview(
                                photo = selectedPhoto,
                                onTransform = { transform ->
                                    val index = photos.indexOfFirst { it.id == selectedPhoto.id }
                                    if (index >= 0) {
                                        val replacement = transform(selectedPhoto.bitmap)
                                        photos[index] = selectedPhoto.copy(bitmap = replacement)
                                        pendingBitmapWipes += selectedPhoto.bitmap
                                    }
                                },
                                onDelete = {
                                    val index = photos.indexOfFirst { it.id == selectedPhoto.id }
                                    if (index >= 0) {
                                        val removed = photos.removeAt(index)
                                        pendingBitmapWipes += removed.bitmap
                                        selectedPhotoId = photos.getOrNull(index.coerceAtMost(photos.lastIndex))?.id
                                        showCamera = photos.isEmpty()
                                    }
                                },
                            )
                        } else if (hasCameraPermission) {
                            OfflineCameraPreview(
                                modifier = Modifier.fillMaxSize(),
                                onCaptured = { bitmap, recognizedItems ->
                                    captureError = null
                                    val photo = RecoveryPhoto(nextPhotoId++, bitmap)
                                    photos += photo
                                    selectedPhotoId = photo.id
                                    showCamera = false
                                    recognizedItems.ifEmpty { listOf("") }.forEach { recognized ->
                                        items += RecoveryCaptureItem(
                                            id = nextId++,
                                            text = recognized,
                                            confirmed = false,
                                            fromPhoto = true,
                                        )
                                    }
                                },
                                onError = { message -> captureError = message },
                            )
                        } else {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                Button(onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }) {
                                    Text("Allow camera")
                                }
                            }
                        }
                    }

                    if (photos.isNotEmpty()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            photos.forEachIndexed { index, photo ->
                                FilterChip(
                                    selected = !showCamera && selectedPhotoId == photo.id,
                                    onClick = {
                                        selectedPhotoId = photo.id
                                        showCamera = false
                                    },
                                    label = { Text("Photo ${index + 1}") },
                                )
                            }
                            FilterChip(
                                selected = showCamera,
                                onClick = { showCamera = true },
                                label = { Text("Take another") },
                            )
                        }
                    }

                    captureError?.let {
                        InfoCard(text = it, tone = InfoTone.Danger)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = "Review ${items.size}/$expectedWordCount captured items",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        TextButton(
                            onClick = {
                                items += RecoveryCaptureItem(
                                    id = nextId++,
                                    text = "",
                                    confirmed = false,
                                    fromPhoto = false,
                                )
                            },
                        ) {
                            Text("Add manually")
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        itemsIndexed(items, key = { _, item -> item.id }) { index, item ->
                            RecoveryItemCard(
                                position = index + 1,
                                item = item,
                                format = format,
                                onTextChange = { value ->
                                    items[index] = item.copy(text = value, confirmed = false)
                                },
                                onConfirm = {
                                    items[index] = item.copy(confirmed = true)
                                },
                                onSuggestion = { suggestion ->
                                    items[index] = item.copy(text = suggestion, confirmed = false)
                                },
                                onMoveUp = if (index > 0) {
                                    {
                                        val moved = items.removeAt(index)
                                        items.add(index - 1, moved.copy(confirmed = false))
                                    }
                                } else null,
                                onMoveDown = if (index < items.lastIndex) {
                                    {
                                        val moved = items.removeAt(index)
                                        items.add(index + 1, moved.copy(confirmed = false))
                                    }
                                } else null,
                                onDelete = { items.removeAt(index) },
                            )
                        }
                    }

                    HorizontalDivider()
                    RecoveryValidationSummary(parsed = parsed, everyItemConfirmed = everyItemConfirmed)
                    Button(
                        onClick = { onConfirmed(parsed.words) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        enabled = parsed.canConfirm && everyItemConfirmed,
                    ) {
                        Text("Use reviewed seed phrase")
                    }
                }
            }
        }
    }

    if (showDiscardDialog) {
        AlertDialog(
            onDismissRequest = { showDiscardDialog = false },
            title = { Text("Discard recovery session?") },
            text = { Text("Captured and corrected items will be cleared from memory.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        items.clear()
                        showDiscardDialog = false
                        onDismiss()
                    },
                ) { Text("Discard") }
            },
            dismissButton = {
                TextButton(onClick = { showDiscardDialog = false }) { Text("Keep reviewing") }
            },
        )
    }

    pendingFormat?.let { requested ->
        AlertDialog(
            onDismissRequest = { pendingFormat = null },
            title = { Text("Change recovery format?") },
            text = { Text("Changing format clears the current captured sequence to prevent reinterpretation.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        items.clear()
                        photos.forEach { pendingBitmapWipes += it.bitmap }
                        photos.clear()
                        selectedPhotoId = null
                        showCamera = true
                        format = requested
                        indexConvention = null
                        pendingFormat = null
                    },
                ) { Text("Clear and change") }
            },
            dismissButton = {
                TextButton(onClick = { pendingFormat = null }) { Text("Cancel") }
            },
        )
    }
}

@Composable
private fun PhotoReview(
    photo: RecoveryPhoto,
    onTransform: ((Bitmap) -> Bitmap) -> Unit,
    onDelete: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Image(
            bitmap = photo.bitmap.asImageBitmap(),
            contentDescription = "Captured recovery photo",
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentScale = ContentScale.Fit,
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
        ) {
            TextButton(onClick = { onTransform { rotateBitmap(it, -90f) } }) { Text("Rotate left") }
            TextButton(onClick = { onTransform { rotateBitmap(it, 90f) } }) { Text("Rotate right") }
            TextButton(onClick = { onTransform(::grayscaleBitmap) }) { Text("Grayscale") }
            TextButton(onClick = onDelete) { Text("Delete") }
        }
    }
}

private fun rotateBitmap(source: Bitmap, degrees: Float): Bitmap {
    val matrix = Matrix().apply { postRotate(degrees) }
    return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
}

private fun grayscaleBitmap(source: Bitmap): Bitmap {
    val result = Bitmap.createBitmap(source.width, source.height, Bitmap.Config.ARGB_8888)
    val paint = Paint().apply {
        colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
    }
    Canvas(result).drawBitmap(source, 0f, 0f, paint)
    return result
}

private fun wipeBitmap(bitmap: Bitmap) {
    if (bitmap.isRecycled) return
    if (bitmap.isMutable) {
        bitmap.eraseColor(Color.TRANSPARENT)
    }
    bitmap.recycle()
}

@Composable
private fun RecoveryItemCard(
    position: Int,
    item: RecoveryCaptureItem,
    format: RecoveryMnemonicFormat,
    onTextChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onSuggestion: (String) -> Unit,
    onMoveUp: (() -> Unit)?,
    onMoveDown: (() -> Unit)?,
    onDelete: () -> Unit,
) {
    val suggestions = remember(item.text, format) {
        if (format == RecoveryMnemonicFormat.WORDS && item.text.isNotBlank()) {
            RecoveryTextParser.suggestionsFor(item.text)
                .filterNot { it == item.text.lowercase() }
        } else {
            emptyList()
        }
    }

    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (item.confirmed) {
                MaterialTheme.colorScheme.secondaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            },
        ),
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "$position · ${if (item.fromPhoto) "Photo transcription" else "Manual"}",
                    fontWeight = FontWeight.SemiBold,
                )
                Row {
                    IconButton(onClick = { onMoveUp?.invoke() }, enabled = onMoveUp != null) {
                        Icon(
                            painter = painterResource(R.drawable.ic_keyboard_arrow_up),
                            contentDescription = "Move item up",
                        )
                    }
                    IconButton(onClick = { onMoveDown?.invoke() }, enabled = onMoveDown != null) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_down),
                            contentDescription = "Move item down",
                        )
                    }
                    IconButton(onClick = onDelete) {
                        Icon(
                            painter = painterResource(R.drawable.ic_close),
                            contentDescription = "Delete item",
                        )
                    }
                }
            }

            SecureOutlinedTextField(
                value = item.text,
                onValueChange = onTextChange,
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (format == RecoveryMnemonicFormat.WORDS) "BIP39 word" else "Decimal index") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = if (format == RecoveryMnemonicFormat.WORDS) {
                        KeyboardType.Password
                    } else {
                        KeyboardType.NumberPassword
                    },
                ),
                isPasswordField = true,
            )

            if (suggestions.isNotEmpty()) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    suggestions.forEach { suggestion ->
                        OutlinedButton(onClick = { onSuggestion(suggestion) }) {
                            Text(suggestion)
                        }
                    }
                }
            }

            Button(
                onClick = onConfirm,
                enabled = item.text.isNotBlank() && !item.confirmed,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(if (item.confirmed) "User confirmed" else "Confirm this item")
            }
        }
    }
}

@Composable
private fun RecoveryValidationSummary(
    parsed: RecoveryMnemonicResult,
    everyItemConfirmed: Boolean,
) {
    val (message, tone) = when {
        parsed.problems.isNotEmpty() -> {
            val first = parsed.problems.first()
            first.message to InfoTone.Danger
        }
        !parsed.hasExpectedWordCount -> {
            "Recovered ${parsed.words.size} of ${parsed.expectedWordCount} required words" to InfoTone.Warning
        }
        !parsed.checksumValid -> {
            "All words resolve, but the BIP39 checksum is invalid. Review the physical backup again." to InfoTone.Danger
        }
        !everyItemConfirmed -> {
            "Checksum is valid. Confirm every item yourself before continuing." to InfoTone.Warning
        }
        else -> {
            "Every item is user-confirmed and the BIP39 checksum is valid." to InfoTone.Info
        }
    }
    InfoCard(text = message, tone = tone)
}

@Composable
private fun OfflineCameraPreview(
    modifier: Modifier = Modifier,
    onCaptured: (Bitmap, List<String>) -> Unit,
    onError: (String) -> Unit,
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val mainExecutor = remember(context) { ContextCompat.getMainExecutor(context) }
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }
    val recognizer = remember {
        TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    val isActive = remember { AtomicBoolean(true) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var isProcessing by remember { mutableStateOf(false) }

    DisposableEffect(lifecycleOwner, previewView) {
        var disposed = false
        var boundProvider: ProcessCameraProvider? = null
        var boundPreview: Preview? = null
        var boundCapture: ImageCapture? = null
        val providerFuture = ProcessCameraProvider.getInstance(context)
        val listener = Runnable {
            if (disposed) return@Runnable
            try {
                val provider = providerFuture.get()
                val preview = Preview.Builder().build().also {
                    it.surfaceProvider = previewView.surfaceProvider
                }
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                    .setTargetRotation(previewView.display?.rotation ?: AndroidSurface.ROTATION_0)
                    .build()

                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    capture,
                )
                boundProvider = provider
                boundPreview = preview
                boundCapture = capture
                imageCapture = capture
            } catch (_: Exception) {
                onError("Camera could not be started on this device")
            }
        }
        providerFuture.addListener(listener, mainExecutor)

        onDispose {
            isActive.set(false)
            disposed = true
            val provider = boundProvider
            val preview = boundPreview
            val capture = boundCapture
            if (provider != null && preview != null && capture != null) {
                try {
                    provider.unbind(preview, capture)
                } catch (_: Exception) {
                    // Cleanup is best-effort; never log camera/session details.
                }
            }
            imageCapture = null
            recognizer.close()
        }
    }

    Box(modifier = modifier) {
        AndroidView(factory = { previewView }, modifier = Modifier.fillMaxSize())
        Button(
            onClick = {
                val capture = imageCapture ?: return@Button
                isProcessing = true
                capture.takePicture(
                    mainExecutor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            processCapturedImage(
                                image = image,
                                recognizer = recognizer,
                                onCaptured = { bitmap, recognizedItems ->
                                    if (isActive.get()) {
                                        isProcessing = false
                                        onCaptured(bitmap, recognizedItems)
                                    } else {
                                        wipeBitmap(bitmap)
                                    }
                                },
                                onError = {
                                    if (isActive.get()) {
                                        isProcessing = false
                                        onError(it)
                                    }
                                },
                            )
                        }

                        override fun onError(exception: ImageCaptureException) {
                            isProcessing = false
                            onError("Photo capture failed. Try again.")
                        }
                    },
                )
            },
            enabled = imageCapture != null && !isProcessing,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(12.dp),
        ) {
            Text(if (isProcessing) "Recognizing…" else "Capture in memory")
        }
    }
}

private fun processCapturedImage(
    image: ImageProxy,
    recognizer: com.google.mlkit.vision.text.TextRecognizer,
    onCaptured: (Bitmap, List<String>) -> Unit,
    onError: (String) -> Unit,
) {
    val rawBitmap = try {
        image.toBitmap()
    } catch (_: RuntimeException) {
        image.close()
        onError("Camera returned an unreadable image")
        return
    }
    val rotationDegrees = image.imageInfo.rotationDegrees
    val displayBitmap = if (rotationDegrees == 0) {
        rawBitmap
    } else {
        rotateBitmap(rawBitmap, rotationDegrees.toFloat()).also { wipeBitmap(rawBitmap) }
    }

    val input = InputImage.fromBitmap(displayBitmap, 0)
    recognizer.process(input)
        .addOnSuccessListener { result ->
            val elements = result.textBlocks
                .flatMap { block -> block.lines }
                .flatMap { line -> line.elements }
                .map { element -> element.text.trim() }
                .filter { it.isNotEmpty() }
            onCaptured(displayBitmap, elements)
        }
        .addOnFailureListener {
            wipeBitmap(displayBitmap)
            onError("Offline text recognition failed. Retake the photo or add items manually.")
        }
        .addOnCompleteListener {
            // Closing releases the only captured image buffer; no file is created.
            image.close()
        }
}
