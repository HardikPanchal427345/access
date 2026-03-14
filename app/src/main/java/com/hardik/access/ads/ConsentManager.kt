package com.hardik.access.ads

import android.app.Activity
import com.google.android.ump.ConsentInformation
import com.google.android.ump.ConsentRequestParameters
import com.google.android.ump.FormError
import com.google.android.ump.UserMessagingPlatform

class ConsentManager(private val activity: Activity) {

    private val consentInformation: ConsentInformation =
        UserMessagingPlatform.getConsentInformation(activity)

    private val requestParameters: ConsentRequestParameters =
        ConsentRequestParameters.Builder()
            .setTagForUnderAgeOfConsent(false)
            .build()

    fun requestConsent(onResult: (canRequestAds: Boolean, privacyOptionsRequired: Boolean) -> Unit) {
        consentInformation.requestConsentInfoUpdate(
            activity,
            requestParameters,
            {
                UserMessagingPlatform.loadAndShowConsentFormIfRequired(activity) {
                    onResult(consentInformation.canRequestAds(), isPrivacyOptionsRequired())
                }
            },
            {
                // If consent info fails to refresh, keep UX functional and fallback safely.
                onResult(consentInformation.canRequestAds(), isPrivacyOptionsRequired())
            }
        )
    }

    fun showPrivacyOptionsForm(onComplete: (FormError?) -> Unit) {
        UserMessagingPlatform.showPrivacyOptionsForm(activity, onComplete)
    }

    fun isPrivacyOptionsRequired(): Boolean =
        consentInformation.privacyOptionsRequirementStatus ==
            ConsentInformation.PrivacyOptionsRequirementStatus.REQUIRED
}
