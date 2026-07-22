package com.codeshare.app.data.repo

import com.codeshare.app.data.model.AppNotification
import com.codeshare.app.data.model.Comment
import com.codeshare.app.data.model.Reaction
import com.codeshare.app.data.model.Report
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class InteractionRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    // ─── نظرات ───

    fun commentsFlow(fileId: String): Flow<List<Comment>> = callbackFlow {
        val reg = db.collection("comments")
            .whereEqualTo("fileId", fileId)
            .addSnapshotListener { snap, _ ->
                val list = snap?.toObjects(Comment::class.java) ?: emptyList()
                trySend(list.sortedBy { it.createdAt })
            }
        awaitClose { reg.remove() }
    }

    suspend fun addComment(comment: Comment) {
        db.collection("comments").add(comment.copy(createdAt = System.currentTimeMillis())).await()
        db.collection("files").document(comment.fileId)
            .update("commentsCount", FieldValue.increment(1))
        db.collection("users").document(comment.userId)
            .update(mapOf("points" to FieldValue.increment(5), "commentsCount" to FieldValue.increment(1)))
        // اعلان برای نویسنده فایل
        val file = db.collection("files").document(comment.fileId).get().await()
        val authorId = file.getString("authorId") ?: return
        if (authorId != comment.userId) {
            db.collection("notifications").add(
                AppNotification(
                    userId = authorId,
                    title = if (comment.isQuestion) "سوال جدید ❓" else "نظر جدید 💬",
                    body = "${comment.userName}: ${comment.text.take(80)}",
                    type = "COMMENT", relatedFileId = comment.fileId,
                    createdAt = System.currentTimeMillis(),
                )
            )
        }
    }

    suspend fun deleteComment(commentId: String, fileId: String) {
        db.collection("comments").document(commentId).delete().await()
        db.collection("files").document(fileId)
            .update("commentsCount", FieldValue.increment(-1))
    }

    suspend fun toggleCommentLike(commentId: String, userId: String) {
        val ref = db.collection("comments").document(commentId)
        db.runTransaction { tx ->
            val snap = tx.get(ref)
            @Suppress("UNCHECKED_CAST")
            val likedBy = (snap.get("likedBy") as? List<String>) ?: emptyList()
            if (userId in likedBy) {
                tx.update(ref, "likedBy", FieldValue.arrayRemove(userId), "likes", FieldValue.increment(-1))
            } else {
                tx.update(ref, "likedBy", FieldValue.arrayUnion(userId), "likes", FieldValue.increment(1))
            }
        }.await()
    }

    // ─── واکنش‌ها (لایک / امتیاز / ذخیره) ───

    fun reactionFlow(fileId: String, userId: String): Flow<Reaction?> = callbackFlow {
        val reg = db.collection("reactions").document("${fileId}_$userId")
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObject(Reaction::class.java))
            }
        awaitClose { reg.remove() }
    }

    fun bookmarksFlow(userId: String): Flow<List<Reaction>> = callbackFlow {
        val reg = db.collection("reactions")
            .whereEqualTo("userId", userId)
            .whereEqualTo("bookmarked", true)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects(Reaction::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun toggleLike(fileId: String, userId: String, like: Boolean) {
        val ref = db.collection("reactions").document("${fileId}_$userId")
        val fileRef = db.collection("files").document(fileId)
        val current = ref.get().await().toObject(Reaction::class.java)
            ?: Reaction(fileId = fileId, userId = userId)

        var likeDelta = 0L; var dislikeDelta = 0L
        val updated = if (like) {
            if (current.liked) { likeDelta = -1; current.copy(liked = false) }
            else {
                likeDelta = 1
                if (current.disliked) dislikeDelta = -1
                current.copy(liked = true, disliked = false)
            }
        } else {
            if (current.disliked) { dislikeDelta = -1; current.copy(disliked = false) }
            else {
                dislikeDelta = 1
                if (current.liked) likeDelta = -1
                current.copy(disliked = true, liked = false)
            }
        }
        ref.set(updated).await()
        val updates = mutableMapOf<String, Any>()
        if (likeDelta != 0L) updates["likes"] = FieldValue.increment(likeDelta)
        if (dislikeDelta != 0L) updates["dislikes"] = FieldValue.increment(dislikeDelta)
        if (updates.isNotEmpty()) fileRef.update(updates).await()

        // اعلان لایک برای نویسنده
        if (likeDelta > 0) {
            val file = fileRef.get().await()
            val authorId = file.getString("authorId") ?: return
            if (authorId != userId) {
                db.collection("notifications").add(
                    AppNotification(
                        userId = authorId, title = "لایک جدید ❤️",
                        body = "فایل «${file.getString("title")}» لایک شد",
                        type = "LIKE", relatedFileId = fileId,
                        createdAt = System.currentTimeMillis(),
                    )
                )
            }
        }
    }

    suspend fun setRating(fileId: String, userId: String, stars: Int) {
        val ref = db.collection("reactions").document("${fileId}_$userId")
        val fileRef = db.collection("files").document(fileId)
        val current = ref.get().await().toObject(Reaction::class.java)
            ?: Reaction(fileId = fileId, userId = userId)
        val oldStars = current.rating
        ref.set(current.copy(rating = stars)).await()
        val sumDelta = (stars - oldStars).toLong()
        val countDelta = if (oldStars == 0) 1L else 0L
        fileRef.update(
            mapOf(
                "ratingSum" to FieldValue.increment(sumDelta),
                "ratingCount" to FieldValue.increment(countDelta),
            )
        ).await()
    }

    suspend fun toggleBookmark(fileId: String, userId: String) {
        val ref = db.collection("reactions").document("${fileId}_$userId")
        val current = ref.get().await().toObject(Reaction::class.java)
            ?: Reaction(fileId = fileId, userId = userId)
        ref.set(current.copy(bookmarked = !current.bookmarked)).await()
    }

    // ─── گزارش تخلف ───

    suspend fun submitReport(report: Report) {
        db.collection("reports").add(report.copy(createdAt = System.currentTimeMillis())).await()
    }

    fun reportsFlow(): Flow<List<Report>> = callbackFlow {
        val reg = db.collection("reports")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects(Report::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    suspend fun resolveReport(reportId: String) {
        db.collection("reports").document(reportId).update("resolved", true).await()
    }
}
