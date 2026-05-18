package com.gramasethu.app.data.model

/**
 * Bridge represents one bridge/culvert in Karnataka.
 * This data is stored in Firebase Firestore.
 */
data class Bridge(
    val id: String = "",
    val name: String = "",           // Bridge name e.g. "NH-48 Culvert"
    val latitude: Double = 0.0,      // GPS location
    val longitude: Double = 0.0,
    val status: BridgeStatus = BridgeStatus.OPEN,
    val reportedBy: String = "",     // User who last updated
    val lastUpdated: Long = 0L,      // Timestamp in milliseconds
    val village: String = "",        // Nearest village
    val alternateRoute: String = ""  // Text description of alternate route
)

/**
 * BridgeStatus — the 3 possible states of a bridge
 */
enum class BridgeStatus {
    OPEN,       // Safe to cross ✅
    DAMAGED,    // Use caution ⚠️
    SUBMERGED   // Do NOT cross 🚨
}