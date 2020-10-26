package com.strange.coder.news.util

import androidx.browser.customtabs.CustomTabsIntent

object Util {

    fun getCustomTabsIntent(): CustomTabsIntent {
        // custom tabs
        val builder = CustomTabsIntent.Builder()
        return builder.build()
    }
}