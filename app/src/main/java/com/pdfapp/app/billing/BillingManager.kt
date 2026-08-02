package com.pdfapp.app.billing

import android.app.Activity
import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.*
import com.pdfapp.app.data.model.SubscriptionState
import com.pdfapp.app.data.model.SubscriptionType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BillingManager @Inject constructor(
    private val activity: Activity
) : PurchasesUpdatedListener, BillingClientStateListener {

    private var billingClient: BillingClient
    private val _subscriptionState = MutableStateFlow(SubscriptionState())
    val subscriptionState: StateFlow<SubscriptionState> = _subscriptionState.asStateFlow()

    private val subscriptionIds = listOf(
        SubscriptionType.Monthly.skuId,
        SubscriptionType.Yearly.skuId
    )
    
    private val purchaseTokenStore = mutableMapOf<String, String>()

    init {
        billingClient = BillingClient.newBuilder(activity)
            .setListener(this)
            .enablePendingPurchases()
            .build()
        
        connectToGooglePlayBilling()
    }

    private fun connectToGooglePlayBilling() {
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            Log.d("BillingManager", "Billing setup finished")
            queryPurchases()
        } else {
            Log.e("BillingManager", "Billing setup failed: ${billingResult.debugMessage}")
            _subscriptionState.value = _subscriptionState.value.copy(
                error = "Billing setup failed"
            )
        }
    }

    override fun onBillingServiceDisconnected() {
        Log.d("BillingManager", "Billing service disconnected")
        // Try to reconnect after a delay
        billingClient.startConnection(this)
    }

    override fun onPurchasesUpdated(billingResult: BillingResult, purchases: List<Purchase>?) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else if (billingResult.responseCode == BillingClient.BillingResponseCode.USER_CANCELED) {
            _subscriptionState.value = _subscriptionState.value.copy(
                error = "Purchase canceled"
            )
        } else {
            _subscriptionState.value = _subscriptionState.value.copy(
                error = billingResult.debugMessage
            )
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
            // Grant entitlement to the user
            purchase.products.forEach { productId ->
                purchaseTokenStore[productId] = purchase.purchaseToken
            }
            
            _subscriptionState.value = SubscriptionState(
                isPro = true,
                isLoading = false
            )
            
            // Acknowledge the purchase
            val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            
            billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    Log.d("BillingManager", "Purchase acknowledged")
                }
            }
        }
    }

    fun launchBillingFlow(subscriptionType: SubscriptionType) {
        val flowParams = BillingFlowParams.newBuilder()
            .setSubscriptionUpdateParams(
                BillingFlowParams.SubscriptionUpdateParams.newBuilder()
                    .setSubscriptionOfferDetailsId(subscriptionType.skuId)
                    .build()
            )
            .build()
        
        billingClient.launchBillingFlow(activity, flowParams)
    }

    fun queryPurchases() {
        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true)
        
        val queryPurchaseParams = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()
        
        billingClient.queryPurchasesAsync(queryPurchaseParams) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val activePurchase = purchases?.find { it.purchaseState == Purchase.PurchaseState.PURCHASED }
                if (activePurchase != null) {
                    _subscriptionState.value = SubscriptionState(
                        isPro = true,
                        isLoading = false
                    )
                } else {
                    _subscriptionState.value = SubscriptionState(
                        isPro = false,
                        isLoading = false
                    )
                }
            } else {
                _subscriptionState.value = _subscriptionState.value.copy(
                    isLoading = false,
                    error = "Failed to query purchases"
                )
            }
        }
    }

    fun getAvailableSubscriptions(): List<SubscriptionType> {
        return listOf(
            SubscriptionType.Monthly,
            SubscriptionType.Yearly,
            SubscriptionType.Lifetime
        )
    }

    fun isProUser(): Boolean {
        return _subscriptionState.value.isPro
    }

    fun cleanup() {
        if (billingClient.isReady) {
            billingClient.endConnection()
        }
    }
}
