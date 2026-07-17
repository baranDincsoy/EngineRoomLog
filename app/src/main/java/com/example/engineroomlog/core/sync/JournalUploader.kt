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

    suspend fun uploadPending(context: Context): UploadReport {
        val folder = fleetFolder() ?: return UploadReport(0, 0, notConnected = true)

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
        var pending = 0
        for (pdf in localPdfs.sortedBy { it.name }) {
            if (pdf.name in remoteNames) continue
            try {
                remoteRef.child(pdf.name).putFile(android.net.Uri.fromFile(pdf)).await()
                uploaded++
            } catch (e: Exception) {
                pending++   // network dropped mid-batch; the rest waits for next run
            }
        }
        return UploadReport(uploaded, pending)
    }

    data class UploadReport(
        val uploaded: Int,
        val pending: Int,
        val notConnected: Boolean = false,
        val failed: Boolean = false
    )
}