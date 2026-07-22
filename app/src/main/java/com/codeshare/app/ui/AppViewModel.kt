package com.codeshare.app.ui

import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.codeshare.app.data.model.AppNotification
import com.codeshare.app.data.model.AppUser
import com.codeshare.app.data.model.CodeFile
import com.codeshare.app.data.model.Comment
import com.codeshare.app.data.model.Report
import com.codeshare.app.data.repo.AdminRepository
import com.codeshare.app.data.repo.AuthRepository
import com.codeshare.app.data.repo.FileRepository
import com.codeshare.app.data.repo.InteractionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

private val Context.dataStore by preferencesDataStore("settings")
private val KEY_DARK = booleanPreferencesKey("dark_theme")
private val KEY_CODE_THEME = stringPreferencesKey("code_theme")

class AppViewModel(app: Application) : AndroidViewModel(app) {
    val authRepo = AuthRepository()
    val fileRepo = FileRepository()
    val interactionRepo = InteractionRepository()
    val adminRepo = AdminRepository()

    /** کاربر فعلی (null = خارج شده) */
    val currentUser: StateFlow<AppUser?> = authRepo.currentUserFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    /** همه فایل‌ها به صورت زنده */
    val allFiles: StateFlow<List<CodeFile>> = fileRepo.filesFlow()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /** تنظیمات تم */
    val darkTheme: StateFlow<Boolean?> = app.dataStore.data
        .map { it[KEY_DARK] }
        .stateIn(viewModelScope, SharingStarted.Eagerly, null)

    val codeTheme: StateFlow<String> = app.dataStore.data
        .map { it[KEY_CODE_THEME] ?: "Dark" }
        .stateIn(viewModelScope, SharingStarted.Eagerly, "Dark")

    fun setDarkTheme(dark: Boolean?) = viewModelScope.launch {
        getApplication<Application>().dataStore.edit {
            if (dark == null) it.remove(KEY_DARK) else it[KEY_DARK] = dark
        }
    }

    fun setCodeTheme(theme: String) = viewModelScope.launch {
        getApplication<Application>().dataStore.edit { it[KEY_CODE_THEME] = theme }
    }

    // ─── وضعیت عملیات ───
    val busy = MutableStateFlow(false)
    val uploadProgress = MutableStateFlow(0f)
    val message = MutableStateFlow<String?>(null)

    fun clearMessage() { message.value = null }

    private inline fun runOp(crossinline block: suspend () -> Unit) {
        viewModelScope.launch {
            busy.value = true
            try { block() } catch (e: Exception) {
                message.value = e.localizedMessage ?: "خطای ناشناخته"
            } finally { busy.value = false }
        }
    }

    // ─── احراز هویت ───
    fun signIn(email: String, pass: String) = runOp { authRepo.signIn(email, pass) }
    fun signUp(email: String, pass: String, name: String) = runOp { authRepo.signUp(email, pass, name) }
    fun signInGoogle(idToken: String) = runOp { authRepo.signInWithGoogle(idToken) }
    fun resetPassword(email: String) = runOp {
        authRepo.sendPasswordReset(email)
        message.value = "ایمیل بازیابی رمز ارسال شد ✅"
    }
    fun signOut() = authRepo.signOut()

    // ─── فایل ───
    fun upload(
        title: String, desc: String, lang: String, cat: String, tags: List<String>,
        uris: List<Uri>, cover: Uri?, onDone: (String) -> Unit,
    ) {
        val user = currentUser.value ?: return
        viewModelScope.launch {
            busy.value = true; uploadProgress.value = 0f
            try {
                val id = fileRepo.uploadCodeFile(
                    getApplication(), title, desc, lang, cat, tags, uris, cover,
                    user.id, user.displayName, user.photoUrl,
                ) { uploadProgress.value = it }
                message.value = "فایل با موفقیت منتشر شد 🎉"
                onDone(id)
            } catch (e: Exception) {
                message.value = e.localizedMessage ?: "آپلود ناموفق بود"
            } finally { busy.value = false }
        }
    }

    fun updateFile(
        existing: CodeFile, title: String, desc: String, lang: String, cat: String,
        tags: List<String>, newUris: List<Uri>, versionNote: String, onDone: () -> Unit,
    ) = viewModelScope.launch {
        busy.value = true
        try {
            fileRepo.updateCodeFile(getApplication(), existing, title, desc, lang, cat, tags, newUris, versionNote)
            message.value = "به‌روزرسانی انجام شد ✅"
            onDone()
        } catch (e: Exception) {
            message.value = e.localizedMessage ?: "به‌روزرسانی ناموفق بود"
        } finally { busy.value = false }
    }

    fun deleteFile(fileId: String, onDone: () -> Unit) = runOp {
        fileRepo.deleteFile(fileId)
        message.value = "فایل حذف شد"
        onDone()
    }

    // ─── تعامل ───
    fun addComment(fileId: String, text: String, parentId: String, isQuestion: Boolean) {
        val user = currentUser.value ?: return
        runOp {
            interactionRepo.addComment(
                Comment(
                    fileId = fileId, userId = user.id, userName = user.displayName,
                    userPhotoUrl = user.photoUrl, userRole = user.role,
                    text = text, parentId = parentId, isQuestion = isQuestion,
                )
            )
        }
    }

    fun deleteComment(commentId: String, fileId: String) = runOp {
        interactionRepo.deleteComment(commentId, fileId)
    }

    fun toggleCommentLike(commentId: String) {
        val uid = currentUser.value?.id ?: return
        runOp { interactionRepo.toggleCommentLike(commentId, uid) }
    }

    fun toggleLike(fileId: String, like: Boolean) {
        val uid = currentUser.value?.id ?: return
        runOp { interactionRepo.toggleLike(fileId, uid, like) }
    }

    fun setRating(fileId: String, stars: Int) {
        val uid = currentUser.value?.id ?: return
        runOp { interactionRepo.setRating(fileId, uid, stars) }
    }

    fun toggleBookmark(fileId: String) {
        val uid = currentUser.value?.id ?: return
        runOp { interactionRepo.toggleBookmark(fileId, uid) }
    }

    fun report(targetType: String, targetId: String, fileId: String, reason: String) {
        val user = currentUser.value ?: return
        runOp {
            interactionRepo.submitReport(
                Report(
                    targetType = targetType, targetId = targetId, fileId = fileId,
                    reporterId = user.id, reporterName = user.displayName, reason = reason,
                )
            )
            message.value = "گزارش ثبت شد. ممنون از همکاری شما 🙏"
        }
    }

    // ─── ادمین ───
    fun setRole(userId: String, role: String) = runOp {
        adminRepo.setRole(userId, role)
        message.value = "نقش کاربر تغییر کرد ✅"
    }

    fun setBanned(userId: String, banned: Boolean) = runOp {
        adminRepo.setBanned(userId, banned)
        message.value = if (banned) "کاربر مسدود شد" else "کاربر رفع مسدودی شد"
    }

    fun updateProfile(name: String, bio: String) {
        val uid = currentUser.value?.id ?: return
        runOp {
            adminRepo.updateProfile(uid, name, bio)
            message.value = "پروفایل به‌روزرسانی شد ✅"
        }
    }

    fun sendAnnouncement(title: String, body: String) = runOp {
        adminRepo.sendAnnouncement(title, body)
        message.value = "اعلامیه برای همه ارسال شد 📢"
    }

    fun resolveReport(reportId: String) = runOp { interactionRepo.resolveReport(reportId) }

    fun markRead(notif: AppNotification) = viewModelScope.launch {
        if (notif.userId != "ALL") adminRepo.markNotificationRead(notif.id)
    }
}
