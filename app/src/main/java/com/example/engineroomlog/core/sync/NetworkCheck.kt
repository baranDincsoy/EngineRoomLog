package com.example.engineroomlog.core.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

object NetworkCheck {
    fun isOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val caps = cm.getNetworkCapabilities(cm.activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}