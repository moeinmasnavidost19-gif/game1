package com.codeshare.app.data.model

import com.google.firebase.firestore.DocumentId

// ─── نقش‌های کاربری ───
enum class UserRole { OWNER, ADMIN, USER }

data class AppUser(
    @DocumentId val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoUrl: String = "",
    val bio: String = "",
    val role: String = "USER",
    val banned: Boolean = false,
    val points: Long = 0,          // سیستم امتیاز
    val uploadsCount: Long = 0,
    val commentsCount: Long = 0,
    val createdAt: Long = 0,
) {
    val userRole: UserRole get() = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER)
    val canUpload: Boolean get() = userRole == UserRole.OWNER || userRole == UserRole.ADMIN
    // سیستم سطح: هر ۱۰۰ امتیاز یک سطح
    val level: Int get() = (points / 100).toInt() + 1
    val levelTitle: String get() = when (level) {
        1 -> "تازه‌کار"
        in 2..3 -> "کدآموز"
        in 4..6 -> "برنامه‌نویس"
        in 7..10 -> "حرفه‌ای"
        in 11..15 -> "استاد کد"
        else -> "افسانه"
    }
}

// ─── فایل کد / پروژه ───
data class CodeFile(
    @DocumentId val id: String = "",
    val title: String = "",
    val description: String = "",
    val language: String = "سایر",
    val category: String = "سایر",
    val tags: List<String> = emptyList(),
    val coverUrl: String = "",
    val authorId: String = "",
    val authorName: String = "",
    val authorPhotoUrl: String = "",
    val files: List<StoredFile> = emptyList(),   // چند فایل در یک پست
    val version: Long = 1,
    val versionNotes: List<String> = emptyList(), // تاریخچه نسخه‌ها
    val views: Long = 0,
    val downloads: Long = 0,
    val likes: Long = 0,
    val dislikes: Long = 0,
    val ratingSum: Long = 0,
    val ratingCount: Long = 0,
    val commentsCount: Long = 0,
    val createdAt: Long = 0,
    val updatedAt: Long = 0,
) {
    val rating: Float get() = if (ratingCount == 0L) 0f else ratingSum.toFloat() / ratingCount
    // امتیاز ترند: تعامل اخیر وزن‌دار
    val trendScore: Double get() {
        val ageDays = ((System.currentTimeMillis() - createdAt) / 86_400_000.0).coerceAtLeast(0.1)
        return (likes * 3.0 + downloads * 2.0 + views * 0.5 + commentsCount * 2.5) / ageDays
    }
}

data class StoredFile(
    val name: String = "",
    val url: String = "",
    val size: Long = 0,
    val content: String = "",   // متن کد برای نمایش (تا سقف حجم)
)

// ─── نظر / سوال ───
data class Comment(
    @DocumentId val id: String = "",
    val fileId: String = "",
    val userId: String = "",
    val userName: String = "",
    val userPhotoUrl: String = "",
    val userRole: String = "USER",
    val text: String = "",
    val parentId: String = "",       // پاسخ تو در تو
    val isQuestion: Boolean = false, // بخش پرسش و پاسخ
    val likes: Long = 0,
    val likedBy: List<String> = emptyList(),
    val createdAt: Long = 0,
)

// ─── واکنش کاربر به فایل ───
data class Reaction(
    @DocumentId val id: String = "",
    val fileId: String = "",
    val userId: String = "",
    val liked: Boolean = false,
    val disliked: Boolean = false,
    val rating: Int = 0,          // ۰ = بدون امتیاز
    val bookmarked: Boolean = false,
)

// ─── گزارش تخلف ───
data class Report(
    @DocumentId val id: String = "",
    val targetType: String = "",   // FILE یا COMMENT
    val targetId: String = "",
    val fileId: String = "",
    val reporterId: String = "",
    val reporterName: String = "",
    val reason: String = "",
    val resolved: Boolean = false,
    val createdAt: Long = 0,
)

// ─── اعلان ───
data class AppNotification(
    @DocumentId val id: String = "",
    val userId: String = "",        // گیرنده؛ "ALL" = اعلامیه سراسری
    val title: String = "",
    val body: String = "",
    val type: String = "INFO",      // COMMENT, LIKE, NEW_FILE, ANNOUNCE, INFO
    val relatedFileId: String = "",
    val read: Boolean = false,
    val createdAt: Long = 0,
)

// ─── ثابت‌ها ───
object Cats {
    val LANGUAGES = listOf(
        "Python", "Java", "Kotlin", "JavaScript", "TypeScript", "C++", "C#", "C",
        "HTML", "CSS", "PHP", "Swift", "Go", "Rust", "Ruby", "Dart", "SQL",
        "Shell", "JSON", "XML", "Markdown", "سایر"
    )
    val CATEGORIES = listOf(
        "موبایل", "وب", "بازی", "هوش مصنوعی", "دیتابیس", "الگوریتم",
        "ابزار", "امنیت", "آموزشی", "سایر"
    )
    val ALLOWED_EXTENSIONS = setOf(
        "py", "java", "kt", "kts", "js", "jsx", "ts", "tsx", "cpp", "cc", "cxx", "h", "hpp",
        "cs", "c", "html", "htm", "css", "scss", "php", "swift", "go", "rs", "rb", "dart",
        "sql", "sh", "bat", "json", "xml", "md", "txt", "yaml", "yml", "gradle", "properties", "zip"
    )
    const val MAX_FILE_SIZE = 10L * 1024 * 1024   // ۱۰ مگابایت
    const val MAX_INLINE_CONTENT = 300_000        // حداکثر متن ذخیره‌شده برای نمایش

    fun extToLang(name: String): String = when (name.substringAfterLast('.', "").lowercase()) {
        "py" -> "Python"; "java" -> "Java"; "kt", "kts" -> "Kotlin"
        "js", "jsx" -> "JavaScript"; "ts", "tsx" -> "TypeScript"
        "cpp", "cc", "cxx", "hpp" -> "C++"; "cs" -> "C#"; "c", "h" -> "C"
        "html", "htm" -> "HTML"; "css", "scss" -> "CSS"; "php" -> "PHP"
        "swift" -> "Swift"; "go" -> "Go"; "rs" -> "Rust"; "rb" -> "Ruby"
        "dart" -> "Dart"; "sql" -> "SQL"; "sh", "bat" -> "Shell"
        "json" -> "JSON"; "xml" -> "XML"; "md" -> "Markdown"
        else -> "سایر"
    }
}
