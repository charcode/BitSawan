package com.gorunjinian.metrovault.feature.wallet.create

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.gorunjinian.metrovault.data.model.DerivationPaths
import com.gorunjinian.metrovault.domain.Wallet
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * ViewModel for the stateless (memory-only) wallet import wizard.
 * Manages the 3-step flow: Configuration -> SeedQR Scan -> Passphrase.
 * Nothing is persisted — the wallet lives only in memory until lock/exit.
 */
class ImportStatelessViewModel(application: Application) : AndroidViewModel(application) {

    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    // Dependencies
    private val wallet: Wallet by lazy { Wallet.getInstance(context) }

    // ========== UI State ==========

    data class UiState(
        // Current step: 1 = Configuration, 2 = Scan, 3 = Passphrase
        val currentStep: Int = 1,

        // Step 1: Configuration
        val selectedDerivationPath: String = DerivationPaths.NATIVE_SEGWIT,
        val accountNumber: Int = 0,
        val isTestnet: Boolean = false,

        // Step 2: Scan
        val scannedMnemonic: List<String>? = null,
        val scanError: String = "",

        // Step 3: Passphrase
        val usePassphrase: Boolean = false,
        val passphrase: String = "",
        val confirmPassphrase: String = "",
        val realtimeFingerprint: String = "",

        // Common
        val errorMessage: String = "",
        val isCreating: Boolean = false
    ) {
        // Full derivation path with the account number applied
        val fullDerivationPath: String
            get() = DerivationPaths.withAccountNumber(selectedDerivationPath, accountNumber)
    }

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    // ========== Events ==========

    sealed class ImportStatelessEvent {
        object WalletCreated : ImportStatelessEvent()
        object NavigateBack : ImportStatelessEvent()
    }

    private val _events = MutableSharedFlow<ImportStatelessEvent>()
    val events: SharedFlow<ImportStatelessEvent> = _events.asSharedFlow()

    // ========== Step Navigation ==========

    fun goToNextStep() {
        _uiState.update { it.copy(currentStep = it.currentStep + 1) }
    }

    /**
     * Steps back through the wizard, wiping the sensitive data each step no longer owns.
     * From step 1, wipes everything and emits [ImportStatelessEvent.NavigateBack].
     */
    fun goToPreviousStep() {
        when (_uiState.value.currentStep) {
            1 -> {
                clearSensitiveData()
                viewModelScope.launch {
                    _events.emit(ImportStatelessEvent.NavigateBack)
                }
            }
            2 -> _uiState.update {
                it.copy(currentStep = 1, scanError = "", scannedMnemonic = null)
            }
            3 -> _uiState.update {
                it.copy(
                    currentStep = 2,
                    scannedMnemonic = null,
                    passphrase = "",
                    confirmPassphrase = "",
                    usePassphrase = false,
                    errorMessage = ""
                )
            }
        }
    }

    // ========== Step 1: Configuration ==========

    fun setDerivationPath(path: String) {
        _uiState.update { it.copy(selectedDerivationPath = path) }
    }

    fun setAccountNumber(accountNumber: Int) {
        _uiState.update { it.copy(accountNumber = accountNumber) }
    }

    /**
     * Toggles testnet mode and updates the derivation path accordingly.
     * Preserves the current address type (purpose) when switching.
     */
    fun setTestnetMode(enabled: Boolean) {
        _uiState.update { state ->
            state.copy(
                isTestnet = enabled,
                selectedDerivationPath = DerivationPaths.forNetwork(state.selectedDerivationPath, enabled)
            )
        }
    }

    // ========== Step 2: Scan ==========

    fun onMnemonicScanned(mnemonic: List<String>) {
        _uiState.update { it.copy(scannedMnemonic = mnemonic, currentStep = 3) }
    }

    fun setScanError(message: String) {
        _uiState.update { it.copy(scanError = message) }
    }

    // ========== Step 3: Passphrase ==========

    fun setUsePassphrase(use: Boolean) {
        _uiState.update { it.copy(usePassphrase = use) }
    }

    fun setPassphrase(passphrase: String) {
        _uiState.update { it.copy(passphrase = passphrase) }
    }

    fun setConfirmPassphrase(passphrase: String) {
        _uiState.update { it.copy(confirmPassphrase = passphrase) }
    }

    /**
     * Updates the real-time fingerprint preview from the scanned mnemonic and passphrase.
     * Uses computeFingerprintOnly to avoid race conditions with createStatelessWallet.
     */
    fun updateRealtimeFingerprint() {
        viewModelScope.launch {
            val state = _uiState.value
            val mnemonic = state.scannedMnemonic ?: return@launch
            val passphrase = if (state.usePassphrase) state.passphrase else ""

            val fingerprint = withContext(Dispatchers.IO) {
                try {
                    wallet.computeFingerprintOnly(mnemonic, passphrase, state.fullDerivationPath)
                } catch (_: Exception) {
                    null
                }
            }
            _uiState.update { it.copy(realtimeFingerprint = fingerprint?.uppercase() ?: "") }
        }
    }

    fun createWallet() {
        val state = _uiState.value
        val mnemonic = state.scannedMnemonic ?: return

        val passphraseError = validateBip39Passphrase(
            state.usePassphrase, state.passphrase, state.confirmPassphrase
        )
        if (passphraseError != null) {
            _uiState.update { it.copy(errorMessage = passphraseError) }
            return
        }

        _uiState.update { it.copy(errorMessage = "", isCreating = true) }

        val finalPassphrase = if (state.usePassphrase) state.passphrase else ""

        viewModelScope.launch {
            val walletState = withContext(Dispatchers.IO) {
                wallet.createStatelessWallet(mnemonic, finalPassphrase, state.fullDerivationPath)
            }
            if (walletState != null) {
                clearSensitiveData()
                _events.emit(ImportStatelessEvent.WalletCreated)
            } else {
                _uiState.update { it.copy(errorMessage = "Failed to create wallet", isCreating = false) }
            }
        }
    }

    // ========== Cleanup ==========

    private fun clearSensitiveData() {
        _uiState.update {
            it.copy(
                scannedMnemonic = null,
                passphrase = "",
                confirmPassphrase = "",
                realtimeFingerprint = ""
            )
        }
    }

    /**
     * Clears all sensitive data when the ViewModel is destroyed.
     */
    override fun onCleared() {
        super.onCleared()
        clearSensitiveData()
    }
}
