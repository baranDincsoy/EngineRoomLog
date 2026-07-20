package com.example.engineroomlog.core.sync

import android.content.Context
import com.example.engineroomlog.data.local.database.DatabaseProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await

// Mirrors saved entries to the fleet's cloud space, one document per entry.
// Room is the queue: syncedAt == null (or older than postedAt) means pending.
object EntrySyncer {

    private fun fleetFolder(): String? =
        FleetConnection.fleetId?.replace("@", "_at_")?.replace(".", "_")

    data class SyncReport(val synced: Int, val failed: Int, val notConnected: Boolean = false)

    suspend fun syncPending(context: Context): SyncReport {
        val folder = fleetFolder() ?: return SyncReport(0, 0, notConnected = true)
        if (!NetworkCheck.isOnline(context)) return SyncReport(0, 0, notConnected = true)
        val db = DatabaseProvider.getDatabase(context)
        val logEntryDao = db.logEntryDao()
        val readingDao = db.readingDao()

        val vessel = db.vesselProfileDao().getActiveVessels()
            .first()
            .firstOrNull() ?: return SyncReport(0, 0)

        val pending = logEntryDao.getUnsyncedEntries(vessel.id)
        if (pending.isEmpty()) return SyncReport(0, 0)

        val entriesRef = FirebaseFirestore.getInstance()
            .collection("vessels").document(folder)
            .collection("entries")

        var synced = 0
        var failed = 0
        val syncStartedAt = System.currentTimeMillis()
        for (entry in pending) {
            try {
                val readings = readingDao.getReadingsForEntry(entry.id)
                val doc = mapOf(
                    "timestamp" to entry.timestamp,
                    "collectedByName" to entry.collectedByName,
                    "collectedByCrewId" to entry.collectedByCrewId,
                    "status" to entry.status.name,
                    "remarks" to entry.remarks,
                    "watch" to entry.watch,
                    "postedByName" to entry.postedByName,
                    "postedAt" to entry.postedAt,
                    "readings" to readings.associate { it.parameterId.toString() to it.value }
                )
                entriesRef.document(entry.id.toString())
                    .set(doc, SetOptions.merge()).await()
                logEntryDao.markSynced(entry.id, syncStartedAt)
                synced++
            } catch (e: Exception) {
                failed++   // network dropped; the rest stays queued for next run
            }
        }
        return SyncReport(synced, failed)
    }
}