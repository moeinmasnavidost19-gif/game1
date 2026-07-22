package com.codeshare.app.data.repo

import com.codeshare.app.data.model.AppUser
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    val currentUid: String? get() = auth.currentUser?.uid

    /** جریان زنده پروفایل کاربر فعلی */
    fun currentUserFlow(): Flow<AppUser?> = callbackFlow {
        var docListener: com.google.firebase.firestore.ListenerRegistration? = null
        val authListener = FirebaseAuth.AuthStateListener { fa ->
            docListener?.remove()
            val uid = fa.currentUser?.uid
            if (uid == null) {
                trySend(null)
            } else {
                docListener = db.collection("users").document(uid)
                    .addSnapshotListener { snap, _ ->
                        trySend(snap?.toObject(AppUser::class.java))
                    }
            }
        }
        auth.addAuthStateListener(authListener)
        awaitClose {
            auth.removeAuthStateListener(authListener)
            docListener?.remove()
        }
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
        checkBanned()
    }

    suspend fun signUp(email: String, password: String, name: String) {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        val user = result.user ?: error("ساخت حساب ناموفق بود")
        user.updateProfile(userProfileChangeRequest { displayName = name }).await()
        createUserDocIfMissing(user.uid, email, name, "")
    }

    suspend fun signInWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = auth.signInWithCredential(credential).await()
        val user = result.user ?: error("ورود گوگل ناموفق بود")
        createUserDocIfMissing(
            user.uid, user.email ?: "", user.displayName ?: "کاربر",
            user.photoUrl?.toString() ?: ""
        )
        checkBanned()
    }

    suspend fun sendPasswordReset(email: String) {
        auth.sendPasswordResetEmail(email).await()
    }

    fun signOut() = auth.signOut()

    private suspend fun checkBanned() {
        val uid = currentUid ?: return
        val doc = db.collection("users").document(uid).get().await()
        if (doc.getBoolean("banned") == true) {
            auth.signOut()
            error("حساب شما مسدود شده است")
        }
    }

    /** اولین کاربر ثبت‌نامی → مالک (OWNER) */
    private suspend fun createUserDocIfMissing(uid: String, email: String, name: String, photo: String) {
        val ref = db.collection("users").document(uid)
        val existing = ref.get().await()
        if (!existing.exists()) {
            val isFirst = db.collection("users").limit(1).get().await().isEmpty
            val user = AppUser(
                email = email,
                displayName = name,
                photoUrl = photo,
                role = if (isFirst) "OWNER" else "USER",
                createdAt = System.currentTimeMillis(),
            )
            ref.set(user).await()
        }
    }
}
