package com.gramasethu.app.data.model

/**
 * User data class represents a person using the app.
 * These details are saved in Firebase Firestore.
 */
data class User(
    val uid: String = "",           // Firebase unique ID
    val email: String = "",         // User email
    val name: String = "",          // Display name
    val village: String = "",       // Which village they belong to
    val isGramaKavalu: Boolean = false  // Is this person a local reporter?
)