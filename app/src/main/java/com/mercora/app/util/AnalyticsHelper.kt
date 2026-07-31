package com.mercora.app.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.ktx.Firebase

object AnalyticsHelper {

    private val analytics: FirebaseAnalytics? by lazy {
        try {
            Firebase.analytics
        } catch (e: Exception) {
            null
        }
    }

    fun logScreenView(screenName: String, screenClass: String? = null) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            screenClass?.let { putString(FirebaseAnalytics.Param.SCREEN_CLASS, it) }
        }
        analytics?.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, params)
    }

    fun logLogin(method: String = "email") {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        analytics?.logEvent(FirebaseAnalytics.Event.LOGIN, params)
    }

    fun logSignUp(method: String = "email") {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, method)
        }
        analytics?.logEvent(FirebaseAnalytics.Event.SIGN_UP, params)
    }

    fun logShare(contentType: String, itemId: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
        }
        analytics?.logEvent(FirebaseAnalytics.Event.SHARE, params)
    }

    fun logPurchase(value: Double, currency: String = "MXN", itemCount: Int = 1) {
        val params = Bundle().apply {
            putDouble(FirebaseAnalytics.Param.VALUE, value)
            putString(FirebaseAnalytics.Param.CURRENCY, currency)
            putLong(FirebaseAnalytics.Param.QUANTITY, itemCount.toLong())
        }
        analytics?.logEvent(FirebaseAnalytics.Event.PURCHASE, params)
    }

    fun logViewItem(itemId: String, itemName: String?, category: String?) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
            itemName?.let { putString(FirebaseAnalytics.Param.ITEM_NAME, it) }
            category?.let { putString(FirebaseAnalytics.Param.ITEM_CATEGORY, it) }
        }
        analytics?.logEvent(FirebaseAnalytics.Event.VIEW_ITEM, params)
    }

    fun logAddToCart(itemId: String, value: Double) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
            putDouble(FirebaseAnalytics.Param.VALUE, value)
        }
        analytics?.logEvent(FirebaseAnalytics.Event.ADD_TO_CART, params)
    }

    fun logBeginCheckout(value: Double, currency: String = "MXN") {
        val params = Bundle().apply {
            putDouble(FirebaseAnalytics.Param.VALUE, value)
            putString(FirebaseAnalytics.Param.CURRENCY, currency)
        }
        analytics?.logEvent(FirebaseAnalytics.Event.BEGIN_CHECKOUT, params)
    }

    fun logSearch(searchTerm: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.SEARCH_TERM, searchTerm)
        }
        analytics?.logEvent(FirebaseAnalytics.Event.SEARCH, params)
    }

    fun logEngagement(contentType: String, itemId: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
        }
        analytics?.logEvent(FirebaseAnalytics.Event.SELECT_CONTENT, params)
    }

    fun logLike(contentType: String, itemId: String) {
        val params = Bundle().apply {
            putString(FirebaseAnalytics.Param.CONTENT_TYPE, contentType)
            putString(FirebaseAnalytics.Param.ITEM_ID, itemId)
        }
        analytics?.logEvent("like", params)
    }

    fun logFollow(targetUserId: String) {
        val params = Bundle().apply {
            putString("target_user_id", targetUserId)
        }
        analytics?.logEvent("follow", params)
    }

    fun logCreatePost() {
        analytics?.logEvent("create_post", null)
    }

    fun logStartStream() {
        analytics?.logEvent("start_stream", null)
    }

    fun logError(errorType: String, message: String?) {
        val params = Bundle().apply {
            putString("error_type", errorType)
            message?.let { putString("error_message", it) }
        }
        analytics?.logEvent("app_error", params)
    }

    fun logAppUpdate(didUpdate: Boolean, fromVersion: String?, toVersion: String?) {
        val params = Bundle().apply {
            putString("did_update", didUpdate.toString())
            fromVersion?.let { putString("from_version", it) }
            toVersion?.let { putString("to_version", it) }
        }
        analytics?.logEvent("app_update", params)
    }
}
