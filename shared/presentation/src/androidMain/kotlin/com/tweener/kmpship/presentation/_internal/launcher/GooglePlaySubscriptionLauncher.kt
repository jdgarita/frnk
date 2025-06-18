package com.tweener.kmpship.presentation._internal.launcher

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import io.github.aakira.napier.Napier

/**
 * @author Vivien Mahe
 * @since 21/01/2025
 */
class GooglePlaySubscriptionLauncher(private val context: Context) : MobileStoreSubscriptionLauncher {

    override fun open(packageName: String, productId: String?) {
        try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                val baseUrl = "https://play.google.com/store/account/subscriptions"
                data = Uri.parse(productId?.let { "$baseUrl?sku=$it&package=$packageName" } ?: baseUrl)
                setPackage("com.android.vending") // Ensures it opens in Google Play
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(intent)
        } catch (exception: ActivityNotFoundException) {
            Napier.e(exception) { "Couldn't open Google Play on the device!" }
        }
    }
}
