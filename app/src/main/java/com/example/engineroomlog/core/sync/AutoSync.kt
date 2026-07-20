package com.example.engineroomlog.core.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// Fires uploadPending automatically: once at app start, and whenever
// the device regains internet — the "arriving at port" moment.
object AutoSync {

    private var started = false

    fun start(context: Context, scope: CoroutineScope) {
        if (started) return   // register once per process, not per activity recreation
        started = true

        val appContext = context.applicationContext

        // Attempt 1: app start (harmless if offline or not connected — uploader just returns)
        scope.launch {
            JournalUploader.uploadPending(appContext)
            EntrySyncer.syncPending(appContext)
        }

        // Attempt 2+: every time internet becomes available
        val cm = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        cm.registerNetworkCallback(request, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                scope.launch {
                    JournalUploader.uploadPending(appContext)
                    EntrySyncer.syncPending(appContext)
                }
            }
        })
    }
}