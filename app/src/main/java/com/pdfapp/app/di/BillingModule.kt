package com.pdfapp.app.di

import android.app.Activity
import com.pdfapp.app.billing.BillingManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.scopes.ActivityScoped

@Module
@InstallIn(ActivityComponent::class)
object BillingModule {

    @Provides
    @ActivityScoped
    fun provideBillingManager(activity: Activity): BillingManager {
        return BillingManager(activity)
    }
}
