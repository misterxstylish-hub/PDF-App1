package com.pdfapp.app.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.pdfapp.app.billing.BillingManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val billingManager: BillingManager
) : ViewModel() {

    private val _isPro = MutableStateFlow(false)
    val isPro: StateFlow<Boolean> = _isPro.asStateFlow()

    init {
        viewModelScope.launch {
            billingManager.subscriptionState.collect { state ->
                _isPro.value = state.isPro
            }
        }
    }

    fun checkProStatus() {
        billingManager.queryPurchases()
    }
}
