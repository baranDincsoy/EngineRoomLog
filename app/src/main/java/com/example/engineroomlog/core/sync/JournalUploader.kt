package com.example.engineroomlog.core.sync

import android.content.Context
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await
import java.io.File

// Ships exported journal PDFs to the fleet's cloud space.
// The queue IS the filesystem: local files not present remotely are the backlog.
object JournalUploader {

    // fleetId comes from the device auth; '@' and '.' are fine in Storage paths,
    // but we sanitize to keep paths predictable
    private fun fleetFolder(): String? =
        FleetConnection.fleetId?.replace("@", "_at_")?.replace(".", "_")

    private const val PREFS = "sync_cache"
    private const val KEY_UPLOADED = "uploaded_pdfs"

    private fun cacheUploaded(context: Context, names: Set<String>) {
        // Store as a single newline-joined string — more reliable than putStringSet
        val joined = names.joinToString("\n")
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit().putString(KEY_UPLOADED, joined).apply()
    }

    private fun cachedUploaded(context: Context): Set<String> {
        val joined = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(KEY_UPLOADED, "") ?: ""
        val set = if (joined.isEmpty()) emptySet() else joined.split("\n").toSet()
        return set
    }
    suspend fun uploadPending(context: Context): UploadReport {
        val folder = fleetFolder() ?: return UploadReport(0, 0, notConnected = true)
        if (!NetworkCheck.isOnline(context)) return UploadReport(0, 0, notConnected = true)
        val localDir = File(context.filesDir, "journals")
        val localPdfs = localDir.listFiles { f -> f.extension == "pdf" } ?: emptyArray()
        if (localPdfs.isEmpty()) return UploadReport(0, 0)

        val remoteRef = FirebaseStorage.getInstance().reference
            .child("vessels").child(folder).child("journals")

        val remoteNames: Set<String> = try {
            remoteRef.listAll().await().items.map { it.name }.toSet()
        } catch (e: Exception) {
            return UploadReport(0, localPdfs.size, failed = true)
        }

        var uploaded = 0
        val newlyUploaded = mutableListOf<String>()
        var pending = 0
        for (pdf in localPdfs.sortedBy { it.name }) {
            if (pdf.name in remoteNames) continue
            try {
                val metadata = com.google.firebase.storage.StorageMetadata.Builder()
                    .setCustomMetadata("sha256", com.example.engineroomlog.core.pdf.PdfHasher.sha256(pdf))
                    .build()
                remoteRef.child(pdf.name).putFile(android.net.Uri.fromFile(pdf), metadata).await()
                uploaded++
                newlyUploaded.add(pdf.name)
            } catch (e: Exception) {
                pending++   // network dropped mid-batch; the rest waits for next run
            }
        }
        cacheUploaded(context, remoteNames + newlyUploaded)
        return UploadReport(uploaded, pending)
    }

    data class UploadReport(
        val uploaded: Int,
        val pending: Int,
        val notConnected: Boolean = false,
        val failed: Boolean = false
    )

    // Names of journals already in the cloud — cached so the status
    // survives offline periods (a ship is offline most of its life)
    suspend fun remoteJournalNames(context: Context): Set<String> {
        val folder = fleetFolder() ?: return cachedUploaded(context)
        if (!NetworkCheck.isOnline(context)) return cachedUploaded(context)
        return try {
            val names = FirebaseStorage.getInstance().reference
                .child("vessels").child(folder).child("journals")
                .listAll().await().items.map { it.name }.toSet()
            cacheUploaded(context, names)
            names
        } catch (e: Exception) {
            cachedUploaded(context)   // offline: last known truth beats a blank stare
        }
    }

    data class VerifyReport(val verified: Int, val mismatched: List<String>, val unchecked: Int)

    // Re-hash local files and compare with the cloud's recorded fingerprints
    suspend fun verifyIntegrity(context: Context): VerifyReport {
        val folder = fleetFolder() ?: return VerifyReport(0, emptyList(), 0)
        val localDir = File(context.filesDir, "journals")
        val localPdfs = localDir.listFiles { f -> f.extension == "pdf" } ?: emptyArray()

        val remoteRef = com.google.firebase.storage.FirebaseStorage.getInstance().reference
            .child("vessels").child(folder).child("journals")

        var verified = 0
        var unchecked = 0
        val mismatched = mutableListOf<String>()

        for (pdf in localPdfs) {
            try {
                val meta = remoteRef.child(pdf.name).metadata.await()
                val cloudHash = meta.getCustomMetadata("sha256")
                when {
                    cloudHash == null -> unchecked++   // uploaded before hashing existed
                    cloudHash == com.example.engineroomlog.core.pdf.PdfHasher.sha256(pdf) -> verified++
                    else -> mismatched.add(pdf.name)
                }
            } catch (e: Exception) {
                unchecked++   // not uploaded yet, or offline
            }
        }
        return VerifyReport(verified, mismatched, unchecked)
    }
}