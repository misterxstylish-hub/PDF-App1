package com.pdfapp.app.data.model

sealed class SubscriptionType(val skuId: String, val title: String, val description: String) {
    object Monthly : SubscriptionType(
        skuId = "pdf_pro_monthly",
        title = "Monthly Pro",
        description = "Unlock all features for \$4.99/month"
    )
    
    object Yearly : SubscriptionType(
        skuId = "pdf_pro_yearly",
        title = "Yearly Pro",
        description = "Unlock all features for \$39.99/year (Save 33%)"
    )
    
    object Lifetime : SubscriptionType(
        skuId = "pdf_pro_lifetime",
        title = "Lifetime Pro",
        description = "One-time payment of \$79.99 for lifetime access"
    )
}

data class SubscriptionState(
    val isPro: Boolean = false,
    val subscriptionType: SubscriptionType? = null,
    val expiryDate: Long? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)

enum class FeatureAccess {
    FREE_WITH_WATERMARK,
    PRO_UNLIMITED
}
