package com.gorunjinian.metrovault.feature.wallet.create

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import com.gorunjinian.metrovault.data.model.DerivationPaths
import com.gorunjinian.metrovault.core.qr.SeedQRUtils
import com.gorunjinian.metrovault.core.ui.components.SecureOutlinedTextField
import com.gorunjinian.metrovault.core.qr.configureForQRScanning
import com.journeyapps.barcodescanner.CompoundBarcodeView
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds

/**
 * Import screen for stateless wallets via SeedQR.
 * Flow: Step 1 (Configuration) → Step 2 (QR Scan) → Step 3 (Passphrase) → WalletDetailsScreen
 * Does not persist anything - wallet exists only in memory.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportStatelessScreen(
    viewModel: ImportStatelessViewModel = viewModel(),
    onBack: () -> Unit,
    onWalletCreated: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ImportStatelessViewModel.ImportStatelessEvent.WalletCreated -> onWalletCreated()
                is ImportStatelessViewModel.ImportStatelessEvent.NavigateBack -> onBack()
            }
        }
    }

    // Calculate fingerprint in real-time when mnemonic or passphrase changes (Step 3)
    LaunchedEffect(
        uiState.scannedMnemonic, uiState.passphrase, uiState.usePassphrase,
        uiState.selectedDerivationPath, uiState.accountNumber
    ) {
        delay(150.milliseconds)  // Debounce
        viewModel.updateRealtimeFingerprint()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        when (uiState.currentStep) {
                            1 -> "Stateless Import"
                            2 -> "Scan SeedQR"
                            3 -> "Passphrase (Optional)"
                            else -> "Stateless Import"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { viewModel.goToPreviousStep() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        when (uiState.currentStep) {
            1 -> Step1Configuration(
                modifier = Modifier.padding(padding),
                selectedDerivationPath = uiState.selectedDerivationPath,
                accountNumber = uiState.accountNumber,
                isTestnet = uiState.isTestnet,
                onDerivationPathChange = { viewModel.setDerivationPath(it) },
                onAccountNumberChange = { viewModel.setAccountNumber(it) },
                onTestnetChange = { viewModel.setTestnetMode(it) },
                onNext = { viewModel.goToNextStep() }
            )

            2 -> Step2ScanSeedQR(
                modifier = Modifier.padding(padding),
                errorMessage = uiState.scanError,
                onMnemonicScanned = { viewModel.onMnemonicScanned(it) },
                onError = { viewModel.setScanError(it) }
            )

            3 -> Step3Passphrase(
                modifier = Modifier.padding(padding),
                wordCount = uiState.scannedMnemonic?.size ?: 0,
                fingerprint = uiState.realtimeFingerprint,
                usePassphrase = uiState.usePassphrase,
                passphrase = uiState.passphrase,
                confirmPassphrase = uiState.confirmPassphrase,
                errorMessage = uiState.errorMessage,
                isCreating = uiState.isCreating,
                onUsePassphraseChange = { viewModel.setUsePassphrase(it) },
                onPassphraseChange = { viewModel.setPassphrase(it) },
                onConfirmPassphraseChange = { viewModel.setConfirmPassphrase(it) },
                onImport = { viewModel.createWallet() }
            )
        }
    }
}

// ========== Step 1: Configuration ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Step1Configuration(
    modifier: Modifier = Modifier,
    selectedDerivationPath: String,
    accountNumber: Int,
    isTestnet: Boolean,
    onDerivationPathChange: (String) -> Unit,
    onAccountNumberChange: (Int) -> Unit,
    onTestnetChange: (Boolean) -> Unit,
    onNext: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Info card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = "Stateless Wallet",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "This wallet will exist only in memory and will be wiped when you lock or exit the app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // Testnet toggle row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Address Type",
                    style = MaterialTheme.typography.headlineSmall
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Testnet",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isTestnet) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Switch(
                        checked = isTestnet,
                        onCheckedChange = onTestnetChange
                    )
                }
            }
            
            // Address type selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select the Bitcoin address type for this wallet.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    var addressTypeExpanded by remember { mutableStateOf(false) }
                    
                    val options = if (isTestnet) {
                        listOf(
                            Triple("Taproot", "tb1p...", DerivationPaths.TAPROOT_TESTNET),
                            Triple("Native SegWit", "tb1q...", DerivationPaths.NATIVE_SEGWIT_TESTNET),
                            Triple("Nested SegWit", "2...", DerivationPaths.NESTED_SEGWIT_TESTNET),
                            Triple("Legacy", "m/n...", DerivationPaths.LEGACY_TESTNET)
                        )
                    } else {
                        listOf(
                            Triple("Taproot", "bc1p...", DerivationPaths.TAPROOT),
                            Triple("Native SegWit", "bc1q...", DerivationPaths.NATIVE_SEGWIT),
                            Triple("Nested SegWit", "3...", DerivationPaths.NESTED_SEGWIT),
                            Triple("Legacy", "1...", DerivationPaths.LEGACY)
                        )
                    }
                    
                    val currentPurpose = DerivationPaths.getPurpose(selectedDerivationPath)
                    val selectedOption = options.find { DerivationPaths.getPurpose(it.third) == currentPurpose } ?: options[1]
                    val isDefaultAddressType = currentPurpose == 84
                    
                    ExposedDropdownMenuBox(
                        expanded = addressTypeExpanded,
                        onExpandedChange = { addressTypeExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = selectedOption.first,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Address Type") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = addressTypeExpanded) },
                            suffix = if (isDefaultAddressType) {
                                {
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiaryContainer,
                                        contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "Default",
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                            } else null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable, true)
                        )
                        ExposedDropdownMenu(
                            expanded = addressTypeExpanded,
                            onDismissRequest = { addressTypeExpanded = false }
                        ) {
                            options.forEachIndexed { index, (label, example, path) ->
                                DropdownMenuItem(
                                    text = {
                                        Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                            Text(text = label, style = MaterialTheme.typography.titleMedium)
                                            Text(
                                                text = "Example: $example",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                    },
                                    onClick = {
                                        onDerivationPathChange(path)
                                        addressTypeExpanded = false
                                    },
                                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp)
                                )
                                if (index < options.size - 1) {
                                    HorizontalDivider(
                                        modifier = Modifier.padding(horizontal = 16.dp),
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            
            // Account Number
            Text(
                text = "Account Number (Advanced)",
                style = MaterialTheme.typography.titleMedium
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "BIP44 account index in derivation path. Default is 0.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = accountNumber.toString(),
                        onValueChange = { value ->
                            val num = value.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0
                            onAccountNumberChange(num)
                        },
                        label = { Text("Account Number") },
                        placeholder = { Text("0") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Next")
        }
    }
}

// ========== Step 2: Scan SeedQR ==========

@Suppress("UNUSED_PARAMETER")
@Composable
private fun Step2ScanSeedQR(
    modifier: Modifier = Modifier,
    errorMessage: String,
    onMnemonicScanned: (List<String>) -> Unit,
    onError: (String) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    // Check if permission is already granted on composition
    var hasCameraPermission by remember { 
        mutableStateOf(
            androidx.core.content.ContextCompat.checkSelfPermission(
                context, 
                Manifest.permission.CAMERA
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED
        )
    }
    var barcodeView: CompoundBarcodeView? by remember { mutableStateOf(null) }
    
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }
    
    // Request camera permission if not already granted
    LaunchedEffect(hasCameraPermission) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }
    
    // Lifecycle observer for scanner
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            val scanner = barcodeView
            if (scanner != null) {
                when (event) {
                    Lifecycle.Event.ON_RESUME -> {
                        try { scanner.resume() } catch (_: Exception) { }
                    }
                    Lifecycle.Event.ON_PAUSE -> {
                        try { scanner.pause() } catch (_: Exception) { }
                    }
                    else -> {}
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            try { barcodeView?.pause() } catch (_: Exception) { }
        }
    }
    
    // Resume camera when view is ready
    LaunchedEffect(barcodeView) {
        if (barcodeView != null) {
            try { barcodeView?.resume() } catch (_: Exception) { }
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        if (!hasCameraPermission) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Camera Permission Required",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Text(
                        text = "Please grant camera permission to scan SeedQR codes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) }
                    ) {
                        Text("Grant Permission")
                    }
                }
            }
        } else {
            Text(
                text = "Scan a SeedQR code to import your wallet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            // QR Scanner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        factory = { ctx ->
                            CompoundBarcodeView(ctx).apply {
                                barcodeView = this
                                configureForQRScanning()
                                setStatusText("")
                                decodeContinuous { result ->
                                    result.text?.let { scannedText ->
                                        val rawBytes = result.rawBytes
                                        val decodedWords = SeedQRUtils.decodeSeedQR(scannedText, rawBytes, ctx)
                                        
                                        if (decodedWords != null) {
                                            onMnemonicScanned(decodedWords)
                                            pause()
                                        }
                                    }
                                }
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
            
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
    }
}

// ========== Step 3: Passphrase ==========

@Composable
private fun Step3Passphrase(
    modifier: Modifier = Modifier,
    wordCount: Int,
    fingerprint: String,
    usePassphrase: Boolean,
    passphrase: String,
    confirmPassphrase: String,
    errorMessage: String,
    isCreating: Boolean,
    onUsePassphraseChange: (Boolean) -> Unit,
    onPassphraseChange: (String) -> Unit,
    onConfirmPassphraseChange: (String) -> Unit,
    onImport: () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Success card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "SeedQR Scanned",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Surface(
                            color = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "$wordCount words",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                    if (fingerprint.isNotEmpty()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Fingerprint:",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                            )
                            Text(
                                text = fingerprint,
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
            
            Text(
                text = "BIP39 Passphrase (Optional)",
                style = MaterialTheme.typography.headlineSmall
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "If your seed phrase has a BIP39 passphrase (25th word), enable it here.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Switch(
                    checked = usePassphrase,
                    onCheckedChange = onUsePassphraseChange
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Use BIP39 passphrase")
            }
            
            if (usePassphrase) {
                SecureOutlinedTextField(
                    value = passphrase,
                    onValueChange = onPassphraseChange,
                    label = { Text("BIP39 Passphrase") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isPasswordField = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                )
                
                SecureOutlinedTextField(
                    value = confirmPassphrase,
                    onValueChange = onConfirmPassphraseChange,
                    label = { Text("Confirm Passphrase") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isPasswordField = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onImport() })
                )
            }
            
            if (errorMessage.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = errorMessage,
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
        }
        
        // Warning card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.tertiaryContainer
            )
        ) {
            Text(
                text = "This wallet will be wiped when you lock or leave the app.",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
        }
        
        Button(
            onClick = onImport,
            modifier = Modifier.fillMaxWidth(),
            enabled = !isCreating
        ) {
            if (isCreating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Open Stateless Wallet")
            }
        }
    }
}
