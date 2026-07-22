package com.codeshare.app.data.repo

import android.content.Context
import android.net.Uri
import com.codeshare.app.data.model.AppNotification
import com.codeshare.app.data.model.Cats
import com.codeshare.app.data.model.CodeFile
import com.codeshare.app.data.model.StoredFile
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FileRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {
    private val filesCol get() = db.collection("files")

    /** جریان زنده همه فایل‌ها (جدیدترین اول) */
    fun filesFlow(): Flow<List<CodeFile>> = callbackFlow {
        val reg = filesCol.orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, _ ->
                trySend(snap?.toObjects(CodeFile::class.java) ?: emptyList())
            }
        awaitClose { reg.remove() }
    }

    fun fileFlow(fileId: String): Flow<CodeFile?> = callbackFlow {
        val reg = filesCol.document(fileId).addSnapshotListener { snap, _ ->
            trySend(snap?.toObject(CodeFile::class.java))
        }
        awaitClose { reg.remove() }
    }

    suspend fun getFile(fileId: String): CodeFile? =
        filesCol.document(fileId).get().await().toObject(CodeFile::class.java)

    /** آپلود فایل‌ها به Storage و ساخت سند Firestore */
    suspend fun uploadCodeFile(
        context: Context,
        title: String,
        description: String,
        language: String,
        category: String,
        tags: List<String>,
        fileUris: List<Uri>,
        coverUri: Uri?,
        authorId: String,
        authorName: String,
        authorPhotoUrl: String,
        onProgress: (Float) -> Unit = {},
    ): String {
        require(fileUris.isNotEmpty()) { "حداقل یک فایل انتخاب کنید" }
        val docRef = filesCol.document()
        val stored = mutableListOf<StoredFile>()
        val total = fileUris.size + (if (coverUri != null) 1 else 0)
        var done = 0

        for (uri in fileUris) {
            val meta = queryFileMeta(context, uri)
            require(meta.second <= Cats.MAX_FILE_SIZE) { "حجم «${meta.first}» بیشتر از ۱۰ مگابایت است" }
            val ext = meta.first.substringAfterLast('.', "").lowercase()
            require(ext in Cats.ALLOWED_EXTENSIONS) { "فرمت «$ext» مجاز نیست" }

            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                ?: error("خواندن «${meta.first}» ناموفق بود")
            val ref = storage.reference.child("code_files/${docRef.id}/${meta.first}")
            ref.putBytes(bytes).await()
            val url = ref.downloadUrl.await().toString()
            // متن کد برای نمایش درون‌برنامه (فایل‌های zip نمایش داده نمی‌شوند)
            val content = if (ext != "zip" && bytes.size <= Cats.MAX_INLINE_CONTENT)
                bytes.toString(Charsets.UTF_8) else ""
            stored += StoredFile(name = meta.first, url = url, size = meta.second, content = content)
            done++; onProgress(done.toFloat() / total)
        }

        var coverUrl = ""
        if (coverUri != null) {
            val bytes = context.contentResolver.openInputStream(coverUri)?.use { it.readBytes() }
            if (bytes != null) {
                val ref = storage.reference.child("covers/${docRef.id}.jpg")
                ref.putBytes(bytes).await()
                coverUrl = ref.downloadUrl.await().toString()
            }
            done++; onProgress(done.toFloat() / total)
        }

        val now = System.currentTimeMillis()
        val file = CodeFile(
            title = title, description = description, language = language,
            category = category, tags = tags, coverUrl = coverUrl,
            authorId = authorId, authorName = authorName, authorPhotoUrl = authorPhotoUrl,
            files = stored, version = 1, versionNotes = listOf("نسخه ۱ — انتشار اولیه"),
            createdAt = now, updatedAt = now,
        )
        docRef.set(file).await()
        // امتیاز آپلود + شمارنده
        db.collection("users").document(authorId)
            .update(mapOf("points" to FieldValue.increment(20), "uploadsCount" to FieldValue.increment(1)))
        notifyAll("فایل جدید 🎉", "«$title» منتشر شد", "NEW_FILE", docRef.id)
        return docRef.id
    }

    /** به‌روزرسانی اطلاعات + نسخه جدید در صورت تغییر فایل‌ها */
    suspend fun updateCodeFile(
        context: Context,
        existing: CodeFile,
        title: String,
        description: String,
        language: String,
        category: String,
        tags: List<String>,
        newFileUris: List<Uri>,
        versionNote: String,
    ) {
        val updates = mutableMapOf<String, Any>(
            "title" to title, "description" to description, "language" to language,
            "category" to category, "tags" to tags, "updatedAt" to System.currentTimeMillis(),
        )
        if (newFileUris.isNotEmpty()) {
            val stored = mutableListOf<StoredFile>()
            for (uri in newFileUris) {
                val meta = queryFileMeta(context, uri)
                val ext = meta.first.substringAfterLast('.', "").lowercase()
                require(ext in Cats.ALLOWED_EXTENSIONS) { "فرمت «$ext» مجاز نیست" }
                val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
                    ?: error("خواندن فایل ناموفق بود")
                val ref = storage.reference.child("code_files/${existing.id}/v${existing.version + 1}_${meta.first}")
                ref.putBytes(bytes).await()
                val url = ref.downloadUrl.await().toString()
                val content = if (ext != "zip" && bytes.size <= Cats.MAX_INLINE_CONTENT)
                    bytes.toString(Charsets.UTF_8) else ""
                stored += StoredFile(name = meta.first, url = url, size = meta.second, content = content)
            }
            updates["files"] = stored.map { mapOf("name" to it.name, "url" to it.url, "size" to it.size, "content" to it.content) }
            updates["version"] = existing.version + 1
            updates["versionNotes"] = existing.versionNotes +
                "نسخه ${existing.version + 1} — ${versionNote.ifBlank { "به‌روزرسانی فایل‌ها" }}"
        }
        filesCol.document(existing.id).update(updates).await()
    }

    suspend fun deleteFile(fileId: String) {
        // حذف فایل‌های Storage
        runCatching {
            val items = storage.reference.child("code_files/$fileId").listAll().await()
            items.items.forEach { it.delete().await() }
            storage.reference.child("covers/$fileId.jpg").delete().await()
        }
        // حذف نظرات و واکنش‌ها
        val comments = db.collection("comments").whereEqualTo("fileId", fileId).get().await()
        comments.documents.forEach { it.reference.delete() }
        val reactions = db.collection("reactions").whereEqualTo("fileId", fileId).get().await()
        reactions.documents.forEach { it.reference.delete() }
        filesCol.document(fileId).delete().await()
    }

    fun incrementViews(fileId: String) {
        filesCol.document(fileId).update("views", FieldValue.increment(1))
    }

    fun incrementDownloads(fileId: String) {
        filesCol.document(fileId).update("downloads", FieldValue.increment(1))
    }

    private fun queryFileMeta(context: Context, uri: Uri): Pair<String, Long> {
        var name = "file_${UUID.randomUUID().toString().take(6)}"
        var size = 0L
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val nameIdx = c.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            val sizeIdx = c.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (c.moveToFirst()) {
                if (nameIdx >= 0) name = c.getString(nameIdx) ?: name
                if (sizeIdx >= 0) size = c.getLong(sizeIdx)
            }
        }
        return name to size
    }

    private fun notifyAll(title: String, body: String, type: String, fileId: String) {
        db.collection("notifications").add(
            AppNotification(
                userId = "ALL", title = title, body = body, type = type,
                relatedFileId = fileId, createdAt = System.currentTimeMillis(),
            )
        )
    }
}
