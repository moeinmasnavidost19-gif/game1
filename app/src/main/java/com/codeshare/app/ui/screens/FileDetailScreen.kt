package com.codeshare.app.ui.screens

import android.content.Intent
import android.os.Environment
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Reply
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbDownOffAlt
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ThumbUpOffAlt
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.codeshare.app.data.model.CodeFile
import com.codeshare.app.data.model.Comment
import com.codeshare.app.data.model.UserRole
import com.codeshare.app.ui.AppViewModel
import com.codeshare.app.ui.Routes
import com.codeshare.app.ui.components.CodeThemes
import com.codeshare.app.ui.components.CodeViewer
import com.codeshare.app.ui.components.LangChip
import com.codeshare.app.ui.components.RoleBadge
import com.codeshare.app.ui.components.StatChip
import com.codeshare.app.ui.components.UserAvatar
import com.codeshare.app.ui.components.formatCount
import com.codeshare.app.ui.components.timeAgo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.net.URL
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import androidx.compose.runtime.rememberCoroutineScope

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileDetailScreen(vm: AppViewModel, nav: NavHostController, fileId: String) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    val user by vm.currentUser.collectAsState()
    val file by remember(fileId) { vm.fileRepo.fileFlow(fileId) }
        .collectAsState(initial = null)
    val comments by remember(fileId) { vm.interactionRepo.commentsFlow(fileId) }
        .collectAsState(initial = emptyList())
    val reaction by remember(fileId, user?.id) {
        vm.interactionRepo.reactionFlow(fileId, user?.id ?: "-")
    }.collectAsState(initial = null)

    // شمارش بازدید — یک بار
    LaunchedEffect(fileId) { vm.fileRepo.incrementViews(fileId) }

    var selectedFileIdx by remember { mutableIntStateOf(0) }
    var codeThemeName by remember { mutableStateOf("Dark") }
    val storedTheme by vm.codeTheme.collectAsState()
    LaunchedEffect(storedTheme) { codeThemeName = storedTheme }

    var fullscreen by remember { mutableStateOf(false) }
    var themeMenuOpen by remember { mutableStateOf(false) }
    var showVersions by remember { mutableStateOf(false) }
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var reportTarget by remember { mutableStateOf<Pair<String, String>?>(null) } // type,id
    var commentTab by remember { mutableIntStateOf(0) } // 0 نظرات، 1 پرسش‌ها
    var commentText by remember { mutableStateOf("") }
    var replyTo by remember { mutableStateOf<Comment?>(null) }
    var downloading by remember { mutableStateOf(false) }

    val f = file ?: return
    val codeTheme = CodeThemes.byName(codeThemeName)
    val canManage = user?.userRole == UserRole.OWNER ||
        (user?.userRole == UserRole.ADMIN && f.authorId == user?.id)
    val isAdminOrOwner = user?.userRole == UserRole.OWNER || user?.userRole == UserRole.ADMIN

    // ─── دانلود فایل‌ها ───
    fun download(single: Int? = null) {
        scope.launch {
            downloading = true
            try {
                withContext(Dispatchers.IO) {
                    val dir = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                        ?: context.filesDir
                    if (single != null || f.files.size == 1) {
                        val sf = f.files[single ?: 0]
                        val out = File(dir, sf.name)
                        URL(sf.url).openStream().use { inp ->
                            FileOutputStream(out).use { inp.copyTo(it) }
                        }
                    } else {
                        // کل پروژه به صورت ZIP
                        val out = File(dir, "${f.title.replace(Regex("[^\\w\\u0600-\\u06FF ]"), "_")}.zip")
                        ZipOutputStream(FileOutputStream(out)).use { zip ->
                            for (sf in f.files) {
                                zip.putNextEntry(ZipEntry(sf.name))
                                URL(sf.url).openStream().use { it.copyTo(zip) }
                                zip.closeEntry()
                            }
                        }
                    }
                }
                vm.fileRepo.incrementDownloads(fileId)
                vm.message.value = "دانلود کامل شد ✅ (پوشه Downloads اپ)"
            } catch (e: Exception) {
                vm.message.value = "خطا در دانلود: ${e.localizedMessage}"
            } finally { downloading = false }
        }
    }

    // ─── حالت تمام‌صفحه کد ───
    if (fullscreen) {
        val sf = f.files.getOrNull(selectedFileIdx)
        Box(Modifier.fillMaxSize().background(codeTheme.background)) {
            Column {
                Row(
                    Modifier.fillMaxWidth().padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    IconButton(onClick = { fullscreen = false }) {
                        Icon(Icons.Filled.Close, "بستن", tint = codeTheme.text)
                    }
                    Text(sf?.name ?: "", color = codeTheme.text,
                        style = MaterialTheme.typography.titleSmall)
                }
                CodeViewer(
                    code = sf?.content ?: "",
                    language = f.language,
                    theme = codeTheme,
                    modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()),
                    fontSize = 14,
                )
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(f.title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                    }
                },
                actions = {
                    // اشتراک‌گذاری لینک
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT,
                                "«${f.title}» را در کدشیر ببین!\ncodeshare://file/${f.id}")
                        }
                        context.startActivity(Intent.createChooser(intent, "اشتراک‌گذاری"))
                    }) { Icon(Icons.Filled.Share, "اشتراک") }

                    if (canManage) {
                        IconButton(onClick = { nav.navigate(Routes.upload(f.id)) }) {
                            Icon(Icons.Filled.Edit, "ویرایش")
                        }
                        IconButton(onClick = { showDeleteConfirm = true }) {
                            Icon(Icons.Filled.Delete, "حذف", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            )
        },
    ) { pad ->
        LazyColumn(Modifier.fillMaxSize().padding(pad)) {
            // ─── کاور ───
            if (f.coverUrl.isNotBlank()) {
                item {
                    AsyncImage(
                        model = f.coverUrl, contentDescription = null,
                        modifier = Modifier.fillMaxWidth().height(180.dp),
                        contentScale = ContentScale.Crop,
                    )
                }
            }

            // ─── اطلاعات ───
            item {
                Column(Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        LangChip(f.language)
                        Spacer(Modifier.width(8.dp))
                        FilterChip(selected = false, onClick = {}, label = { Text(f.category) })
                        Spacer(Modifier.weight(1f))
                        Text("نسخه ${f.version}", style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.clickable { showVersions = true })
                        IconButton(onClick = { showVersions = true }) {
                            Icon(Icons.Filled.History, "تاریخچه نسخه", Modifier.size(18.dp))
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                    Text(f.title, style = MaterialTheme.typography.headlineSmall)
                    if (f.description.isNotBlank()) {
                        Spacer(Modifier.height(6.dp))
                        Text(f.description, style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    if (f.tags.isNotEmpty()) {
                        Spacer(Modifier.height(8.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(f.tags) { tag ->
                                Text("#$tag", color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }
                    Spacer(Modifier.height(12.dp))

                    // نویسنده
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                            .clickable { nav.navigate(Routes.profile(f.authorId)) },
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            UserAvatar(f.authorPhotoUrl, 40)
                            Spacer(Modifier.width(10.dp))
                            Column(Modifier.weight(1f)) {
                                Text(f.authorName, style = MaterialTheme.typography.titleSmall)
                                Text(timeAgo(f.createdAt), style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            StatChip(Icons.Filled.RemoveRedEye, formatCount(f.views))
                            Spacer(Modifier.width(10.dp))
                            StatChip(Icons.Filled.Download, formatCount(f.downloads))
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // ─── نوار عملیات: لایک، دیسلایک، ذخیره، دانلود ───
                    Row(
                        Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        ActionButton(
                            icon = if (reaction?.liked == true) Icons.Filled.ThumbUp
                                else Icons.Filled.ThumbUpOffAlt,
                            label = formatCount(f.likes),
                            tint = if (reaction?.liked == true) MaterialTheme.colorScheme.primary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        ) { vm.toggleLike(fileId, true) }

                        ActionButton(
                            icon = if (reaction?.disliked == true) Icons.Filled.ThumbDown
                                else Icons.Filled.ThumbDownOffAlt,
                            label = formatCount(f.dislikes),
                            tint = if (reaction?.disliked == true) MaterialTheme.colorScheme.error
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        ) { vm.toggleLike(fileId, false) }

                        ActionButton(
                            icon = if (reaction?.bookmarked == true) Icons.Filled.Bookmark
                                else Icons.Filled.BookmarkBorder,
                            label = "ذخیره",
                            tint = if (reaction?.bookmarked == true) MaterialTheme.colorScheme.tertiary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                        ) { vm.toggleBookmark(fileId) }

                        ActionButton(
                            icon = Icons.Filled.Download,
                            label = if (downloading) "…" else
                                if (f.files.size > 1) "دانلود ZIP" else "دانلود",
                            tint = MaterialTheme.colorScheme.secondary,
                        ) { if (!downloading) download() }

                        ActionButton(
                            icon = Icons.Filled.Flag,
                            label = "گزارش",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        ) { reportTarget = "FILE" to f.id }
                    }

                    Spacer(Modifier.height(10.dp))

                    // ─── امتیاز ستاره‌ای ───
                    Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(
                            Modifier.padding(12.dp).fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                        ) {
                            Text("امتیاز شما:  ", style = MaterialTheme.typography.titleSmall)
                            (1..5).forEach { star ->
                                IconButton(onClick = { vm.setRating(fileId, star) },
                                    modifier = Modifier.size(36.dp)) {
                                    Icon(
                                        if ((reaction?.rating ?: 0) >= star) Icons.Filled.Star
                                            else Icons.Filled.StarBorder,
                                        null,
                                        tint = Color(0xFFF59E0B),
                                        modifier = Modifier.size(28.dp),
                                    )
                                }
                            }
                            Spacer(Modifier.width(8.dp))
                            if (f.ratingCount > 0) {
                                Text("میانگین %.1f (%d)".format(f.rating, f.ratingCount),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            // ─── نمایش کد ───
            item {
                Column(Modifier.padding(horizontal = 16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("پیش‌نمایش کد", style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.weight(1f))
                        // انتخاب تم کد
                        Box {
                            IconButton(onClick = { themeMenuOpen = true }) {
                                Icon(Icons.Filled.Palette, "تم کد")
                            }
                            DropdownMenu(expanded = themeMenuOpen,
                                onDismissRequest = { themeMenuOpen = false }) {
                                CodeThemes.all.forEach { t ->
                                    DropdownMenuItem(
                                        text = { Text(if (t.name == codeThemeName) "✓ ${t.name}" else t.name) },
                                        onClick = {
                                            codeThemeName = t.name
                                            vm.setCodeTheme(t.name)
                                            themeMenuOpen = false
                                        },
                                    )
                                }
                            }
                        }
                        // کپی
                        IconButton(onClick = {
                            f.files.getOrNull(selectedFileIdx)?.content?.let {
                                clipboard.setText(AnnotatedString(it))
                                vm.message.value = "کد کپی شد 📋"
                            }
                        }) { Icon(Icons.Filled.ContentCopy, "کپی") }
                        // تمام‌صفحه
                        IconButton(onClick = { fullscreen = true }) {
                            Icon(Icons.Filled.Fullscreen, "تمام‌صفحه")
                        }
                    }

                    // تب فایل‌ها
                    if (f.files.size > 1) {
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(f.files.size) { idx ->
                                FilterChip(
                                    selected = idx == selectedFileIdx,
                                    onClick = { selectedFileIdx = idx },
                                    label = { Text(f.files[idx].name) },
                                )
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    val sf = f.files.getOrNull(selectedFileIdx)
                    if (sf != null && sf.content.isNotBlank()) {
                        CodeViewer(
                            code = sf.content,
                            language = f.language,
                            theme = codeTheme,
                            modifier = Modifier.fillMaxWidth().height(400.dp)
                                .verticalScroll(rememberScrollState()),
                        )
                    } else {
                        Card(shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("📦", style = MaterialTheme.typography.headlineMedium)
                                Text("پیش‌نمایش برای این فایل موجود نیست",
                                    style = MaterialTheme.typography.bodyMedium)
                                sf?.let {
                                    TextButton(onClick = { download(selectedFileIdx) }) {
                                        Text("دانلود ${it.name}")
                                    }
                                }
                            }
                        }
                    }

                    // دانلود تکی
                    if (f.files.size > 1) {
                        TextButton(onClick = { download(selectedFileIdx) }) {
                            Icon(Icons.Filled.Download, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("دانلود فقط همین فایل")
                        }
                    }
                }
            }

            // ─── نظرات و پرسش‌ها ───
            item {
                Column(Modifier.padding(16.dp)) {
                    TabRow(selectedTabIndex = commentTab) {
                        Tab(selected = commentTab == 0, onClick = { commentTab = 0 },
                            text = { Text("نظرات (${comments.count { !it.isQuestion }})") })
                        Tab(selected = commentTab == 1, onClick = { commentTab = 1 },
                            text = { Text("پرسش و پاسخ (${comments.count { it.isQuestion }})") })
                    }
                    Spacer(Modifier.height(12.dp))

                    // فرم ارسال
                    replyTo?.let { r ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.AutoMirrored.Filled.Reply, null, Modifier.size(14.dp))
                            Text(" پاسخ به ${r.userName}",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary)
                            IconButton(onClick = { replyTo = null }) {
                                Icon(Icons.Filled.Close, null, Modifier.size(14.dp))
                            }
                        }
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = commentText, onValueChange = { commentText = it },
                            placeholder = {
                                Text(if (commentTab == 0) "نظرت را بنویس…" else "سوالت را بپرس…")
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(14.dp),
                            maxLines = 4,
                        )
                        IconButton(onClick = {
                            if (commentText.isNotBlank()) {
                                vm.addComment(fileId, commentText.trim(),
                                    replyTo?.id ?: "", commentTab == 1)
                                commentText = ""; replyTo = null
                            }
                        }) {
                            Icon(Icons.Filled.Send, "ارسال",
                                tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    Spacer(Modifier.height(8.dp))
                }
            }

            // لیست نظرات (والدها + پاسخ‌های تو در تو)
            val visible = comments.filter { it.isQuestion == (commentTab == 1) }
            val parents = visible.filter { it.parentId.isBlank() }
            items(parents, key = { it.id }) { c ->
                Column(Modifier.padding(horizontal = 16.dp)) {
                    CommentItem(c, vm, isAdminOrOwner, user?.id,
                        onReply = { replyTo = c },
                        onReport = { reportTarget = "COMMENT" to c.id })
                    visible.filter { it.parentId == c.id }.forEach { child ->
                        Row {
                            Spacer(Modifier.width(32.dp))
                            Box(Modifier.weight(1f)) {
                                CommentItem(child, vm, isAdminOrOwner, user?.id,
                                    onReply = { replyTo = c },
                                    onReport = { reportTarget = "COMMENT" to child.id })
                            }
                        }
                    }
                    HorizontalDivider(Modifier.padding(vertical = 6.dp))
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    // ─── دیالوگ تاریخچه نسخه‌ها ───
    if (showVersions) {
        AlertDialog(
            onDismissRequest = { showVersions = false },
            title = { Text("تاریخچه نسخه‌ها 📜") },
            text = {
                Column {
                    f.versionNotes.reversed().forEach { note ->
                        Text("• $note", style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(vertical = 4.dp))
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showVersions = false }) { Text("بستن") }
            },
        )
    }

    // ─── دیالوگ حذف ───
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("حذف فایل") },
            text = { Text("مطمئنی؟ این کار قابل بازگشت نیست.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteConfirm = false
                    vm.deleteFile(fileId) { nav.popBackStack() }
                }) { Text("حذف", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("انصراف") }
            },
        )
    }

    // ─── دیالوگ گزارش ───
    reportTarget?.let { (type, id) ->
        var reason by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { reportTarget = null },
            title = { Text("گزارش تخلف 🚩") },
            text = {
                OutlinedTextField(
                    value = reason, onValueChange = { reason = it },
                    label = { Text("دلیل گزارش") },
                    modifier = Modifier.fillMaxWidth(),
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (reason.isNotBlank()) {
                        vm.report(type, id, fileId, reason)
                        reportTarget = null
                    }
                }) { Text("ارسال گزارش") }
            },
            dismissButton = {
                TextButton(onClick = { reportTarget = null }) { Text("انصراف") }
            },
        )
    }
}

@Composable
private fun ActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick).padding(8.dp),
    ) {
        Icon(icon, label, tint = tint)
        Spacer(Modifier.height(2.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = tint)
    }
}

@Composable
private fun CommentItem(
    c: Comment,
    vm: AppViewModel,
    isAdminOrOwner: Boolean,
    currentUserId: String?,
    onReply: () -> Unit,
    onReport: () -> Unit,
) {
    Row(Modifier.padding(vertical = 6.dp)) {
        UserAvatar(c.userPhotoUrl, 34)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(c.userName, style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold)
                Spacer(Modifier.width(6.dp))
                RoleBadge(c.userRole)
                Spacer(Modifier.weight(1f))
                Text(timeAgo(c.createdAt), style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(2.dp))
            Text(c.text, style = MaterialTheme.typography.bodyMedium)
            Row(verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = { vm.toggleCommentLike(c.id) }) {
                    Icon(
                        if (currentUserId != null && currentUserId in c.likedBy)
                            Icons.Filled.ThumbUp else Icons.Filled.ThumbUpOffAlt,
                        null, Modifier.size(14.dp),
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(formatCount(c.likes), style = MaterialTheme.typography.labelSmall)
                }
                TextButton(onClick = onReply) {
                    Text("پاسخ", style = MaterialTheme.typography.labelSmall)
                }
                TextButton(onClick = onReport) {
                    Text("گزارش", style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                if (isAdminOrOwner || c.userId == currentUserId) {
                    TextButton(onClick = { vm.deleteComment(c.id, c.fileId) }) {
                        Text("حذف", style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.error)
                    }
                }
            }
        }
    }
}
