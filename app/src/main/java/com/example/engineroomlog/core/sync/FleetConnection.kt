package com.example.engineroomlog.core.sync

import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.tasks.await

// Thin wrapper around Firebase Auth: the DEVICE (not the person) signs in once.
// Crew login stays local; this identity only decides where PDFs are shipped.
object FleetConnection {

    private val auth: FirebaseAuth get() = FirebaseAuth.getInstance()

    val isConnected: Boolean get() = auth.currentUser != null
    val fleetId: String? get() = auth.currentUser?.email

    suspend fun connect(email: String, password: String): Result<Unit> =
        try {
            auth.signInWithEmailAndPassword(email.trim(), password).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }

    fun disconnect() = auth.signOut()
}