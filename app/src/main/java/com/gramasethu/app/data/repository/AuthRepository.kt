package com.gramasethu.app.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.gramasethu.app.data.model.User
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthRepository handles all login/register logic.
 * It talks to Firebase Auth and Firestore.
 *
 * @Singleton means only ONE instance exists in the whole app
 * @Inject means Hilt will create this automatically
 */
@Singleton
class AuthRepository @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Get currently logged in user
    val currentUser: FirebaseUser? get() = auth.currentUser

    // Check if someone is already logged in
    val isLoggedIn: Boolean get() = auth.currentUser != null

    /**
     * Register a new user with email and password
     * Returns Result.success or Result.failure
     */
    suspend fun register(
        email: String,
        password: String,
        name: String,
        village: String
    ): Result<FirebaseUser> {
        return try {
            // Create account in Firebase Auth
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val firebaseUser = result.user!!

            // Save extra user info in Firestore
            val user = User(
                uid = firebaseUser.uid,
                email = email,
                name = name,
                village = village,
                isGramaKavalu = false
            )
            firestore.collection("users")
                .document(firebaseUser.uid)
                .set(user)
                .await()

            Result.success(firebaseUser)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Login existing user
     */
    suspend fun login(email: String, password: String): Result<FirebaseUser> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            Result.success(result.user!!)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Logout current user
     */
    fun logout() {
        auth.signOut()
    }
}