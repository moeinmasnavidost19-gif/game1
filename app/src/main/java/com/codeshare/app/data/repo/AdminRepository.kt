package com.codeshare.app.data.repo

import com.codeshare.app.data.model.AppNotification
import com.codeshare.app.data.model.AppUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AdminRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    fun allUsersFlow(): Flow<List<AppUser>> = callbackFlow {
        val reg = db.collection("users")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects(AppUser::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    /** لیدربورد: برترین کاربران بر اساس امتیاز */
    fun leaderboardFlow(): Flow<List<AppUser>> = callbackFlow {
        val reg = db.collection("users")
            .orderBy("points", Query.Direction.DESCENDING)
            .limit(50)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects(AppUser::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun setRole(userId: String, role: String) {
        db.collection("users").document(userId).update("role", role).await()
        val title = if (role == "ADMIN") "تبریک! 🎉 شما ادمین شدید" else "تغییر نقش"
        val body = when (role) {
            "ADMIN" -> "حالا می‌توانید فایل‌های خود را به اشتراک بگذارید"
            else -> "نقش شما به کاربر عادی تغییر کرد"
        }
        db.collection("notifications").add(
            AppNotification(
                userId = userId, title = title, body = body, type = "INFO",
                createdAt = System.currentTimeMillis(),
            )
        )
    }

    suspend fun setBanned(userId: String, banned: Boolean) {
        db.collection("users").document(userId).update("banned", banned).await()
    }

    suspend fun updateProfile(userId: String, name: String, bio: String) {
        db.collection("users").document(userId)
            .update(mapOf("displayName" to name, "bio" to bio)).await()
    }

    /** اعلامیه سراسری از طرف مالک */
    suspend fun sendAnnouncement(title: String, body: String) {
        db.collection("notifications").add(
            AppNotification(
                userId = "ALL", title = "📢 $title", body = body, type = "ANNOUNCE",
                createdAt = System.currentTimeMillis(),
            )
        ).await()
    }

    // ─── اعلان‌ها ───

    fun notificationsFlow(userId: String): Flow<List<AppNotification>> = callbackFlow {
        val personal = db.collection("notifications")
            .whereIn("userId", listOf(userId, "ALL"))
            .addSnapshotListener { snap, _ ->
                val list = snap?.toObjects(AppNotification::class.java) ?: emptyList()
                trySend(list.sortedByDescending { it.createdAt }.take(100))
            }
        awaitClose { personal.remove() }
    }

    suspend fun markNotificationRead(notifId: String) {
        runCatching {
            db.collection("notifications").document(notifId).update("read", true).await()
        }
    }
}
