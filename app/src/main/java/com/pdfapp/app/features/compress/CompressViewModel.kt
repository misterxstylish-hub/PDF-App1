package com.pdfapp.app.features.compress

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.pdfapp.app.billing.BillingManager
import com.pdfapp.app.data.repository.PdfRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class CompressViewModel @Inject constructor(
    application: Application,
    private val pdfRepository: PdfRepository,
    private val billingManager: BillingManager
) : AndroidViewModel(application) {

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _result = MutableStateFlow<String?>(null)
    val result: StateFlow<String?> = _result.asStateFlow()

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    init {
        viewModelScope.launch {
            billingManager.subscriptionState.collect { state ->
                _isPro.value = state.isPro
            }
        }
    }

    fun compressPdf(inputFile: File, quality: Float = 0.7f, targetSizeKB: Long? = null) {
        viewModelScope.launch {
            _isProcessing.value = true
            _result.value = null

            val outputFile = pdfRepository.createTempFile(getApplication(), "compressed_", ".pdf")
            
            val result = pdfRepository.compressPdf(
                context = getApplication(),
                inputFile = inputFile,
                outputFile = outputFile,
                quality = quality,
                targetSizeKB = if (_isPro.value) targetSizeKB else null,
                isProUser = _isPro.value
            )

            result.onSuccess {
                _result.value = it.absolutePath
            }.onFailure { error ->
                _result.value = "Error: ${error.message}"
            }

            _isProcessing.value = false
        }
    }
}
