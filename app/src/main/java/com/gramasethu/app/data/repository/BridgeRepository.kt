package com.gramasethu.app.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.gramasethu.app.data.model.Bridge
import com.gramasethu.app.data.model.BridgeStatus
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * BridgeRepository handles all bridge data operations.
 * It reads and writes bridge data to Firestore.
 */
@Singleton
class BridgeRepository @Inject constructor(
    private val firestore: FirebaseFirestore
) {
    // Reference to the "bridges" collection in Firestore
    private val bridgesCollection = firestore.collection("bridges")

    /**
     * Get all bridges as a real-time Flow.
     * When ANY bridge updates in Firestore,
     * this Flow automatically emits the new list!
     */
    fun getBridges(): Flow<List<Bridge>> = callbackFlow {
        val listener = bridgesCollection
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val bridges = snapshot?.documents?.mapNotNull { doc ->
                    try {
                        Bridge(
                            id = doc.id,
                            name = doc.getString("name") ?: "",
                            latitude = doc.getDouble("latitude") ?: 0.0,
                            longitude = doc.getDouble("longitude") ?: 0.0,
                            status = BridgeStatus.valueOf(
                                doc.getString("status") ?: "OPEN"
                            ),
                            reportedBy = doc.getString("reportedBy") ?: "",
                            lastUpdated = doc.getLong("lastUpdated") ?: 0L,
                            village = doc.getString("village") ?: "",
                            alternateRoute = doc.getString("alternateRoute") ?: ""
                        )
                    } catch (e: Exception) {
                        null
                    }
                } ?: emptyList()

                trySend(bridges)
            }
        // When Flow is cancelled, remove the Firestore listener
        awaitClose { listener.remove() }
    }

    /**
     * Update bridge status — called when user taps
     * Open / Damaged / Submerged button
     */
    suspend fun updateBridgeStatus(
        bridgeId: String,
        status: BridgeStatus,
        reportedBy: String
    ): Result<Unit> {
        return try {
            bridgesCollection.document(bridgeId)
                .update(
                    mapOf(
                        "status" to status.name,
                        "reportedBy" to reportedBy,
                        "lastUpdated" to System.currentTimeMillis()
                    )
                ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Add sample bridges to Firestore.
     * Call this ONCE to populate the database with
     * Karnataka bridge data for testing.
     */
    suspend fun addSampleBridges() {
        val sampleBridges = listOf(
            mapOf(
                "name" to "NH-48 Culvert Bridge",
                "latitude" to 12.9716,
                "longitude" to 77.5946,
                "status" to "OPEN",
                "reportedBy" to "system",
                "lastUpdated" to System.currentTimeMillis(),
                "village" to "Bengaluru Rural",
                "alternateRoute" to "Take SH-17 via Nelamangala"
            ),
            mapOf(
                "name" to "Arkavathi River Bridge",
                "latitude" to 13.0012,
                "longitude" to 77.5700,
                "status" to "SUBMERGED",
                "reportedBy" to "system",
                "lastUpdated" to System.currentTimeMillis(),
                "village" to "Dobbspet",
                "alternateRoute" to "Use NH-648 bridge 3km north"
            ),
            mapOf(
                "name" to "Shimsha Stream Crossing",
                "latitude" to 12.9500,
                "longitude" to 77.6200,
                "status" to "DAMAGED",
                "reportedBy" to "system",
                "lastUpdated" to System.currentTimeMillis(),
                "village" to "Maddur",
                "alternateRoute" to "Take the Mandya bypass road"
            ),
            mapOf(
                "name" to "Cauvery Canal Bridge",
                "latitude" to 12.9300,
                "longitude" to 77.6100,
                "status" to "OPEN",
                "reportedBy" to "system",
                "lastUpdated" to System.currentTimeMillis(),
                "village" to "Srirangapatna",
                "alternateRoute" to "No alternate needed"
            ),
            mapOf(
                "name" to "Hemavathi Culvert",
                "latitude" to 13.0200,
                "longitude" to 77.5500,
                "status" to "SUBMERGED",
                "reportedBy" to "system",
                "lastUpdated" to System.currentTimeMillis(),
                "village" to "Hassan",
                "alternateRoute" to "Use Hassan-Belur highway bridge"
            )
        )

        sampleBridges.forEach { bridge ->
            bridgesCollection.add(bridge).await()
        }
    }
}