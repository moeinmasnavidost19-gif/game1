package com.codeshare.app.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import coil.compose.AsyncImage
import com.codeshare.app.data.model.Cats
import com.codeshare.app.data.model.Cats.extToLang
import com.codeshare.app.ui.AppViewModel
import com.codeshare.app.ui.components.LangChip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadScreen(vm: AppViewModel, nav: NavHostController, editId: String?) {
    val busy by vm.busy.collectAsState()
    val progress by vm.uploadProgress.collectAsState()
    val user by vm.currentUser.collectAsState()
    val allFiles by vm.allFiles.collectAsState()

    val editing = editId?.let { id -> allFiles.find { it.id == id } }

    var title by remember(editing) { mutableStateOf(editing?.title ?: "") }
    var description by remember(editing) { mutableStateOf(editing?.description ?: "") }
    var language by remember(editing) { mutableStateOf(editing?.language ?: Cats.LANGUAGES.first()) }
    var category by remember(editing) { mutableStateOf(editing?.category ?: Cats.CATEGORIES.first()) }
    var tagInput by remember { mutableStateOf("") }
    var tags by remember(editing) { mutableStateOf(editing?.tags ?: emptyList()) }
    var fileUris by remember { mutableStateOf<List<Uri>>(emptyList()) }
    var coverUri by remember { mutableStateOf<Uri?>(null) }
    var versionNote by remember { mutableStateOf("") }

    // چند فایل همزمان
    val filePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        if (uris.isNotEmpty()) {
            fileUris = (fileUris + uris).distinct()
            // تشخیص خودکار زبان از پسوند اولین فایل
            if (editing == null) {
                uris.firstOrNull()?.lastPathSegment?.let { seg ->
                    val guessed = extToLang(seg)
                    if (guessed != "سایر") language = guessed
                }
            }
        }
    }
    val coverPicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri -> coverUri = uri }

    // فقط ادمین/مالک اجازه دارد
    if (user?.canUpload != true) {
        Scaffold { pad ->
            Column(
                Modifier.fillMaxSize().padding(pad).padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text("⛔", style = MaterialTheme.typography.headlineLarge)
                Spacer(Modifier.height(12.dp))
                Text("فقط مالک و ادمین‌ها اجازه آپلود دارند",
                    style = MaterialTheme.typography.titleMedium)
            }
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (editing != null) "ویرایش فایل ✏️" else "آپلود فایل جدید 📤") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                    }
                },
            )
        },
    ) { pad ->
        Column(
            Modifier.fillMaxSize().padding(pad).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            OutlinedTextField(
                value = title, onValueChange = { title = it },
                label = { Text("عنوان *") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )
            OutlinedTextField(
                value = description, onValueChange = { description = it },
                label = { Text("توضیحات") },
                minLines = 3, maxLines = 6,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )

            // زبان
            Text("زبان برنامه‌نویسی", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Cats.LANGUAGES) { lang ->
                    FilterChip(
                        selected = language == lang,
                        onClick = { language = lang },
                        label = { Text(lang) },
                    )
                }
            }

            // دسته
            Text("دسته‌بندی", style = MaterialTheme.typography.titleSmall)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(Cats.CATEGORIES) { cat ->
                    FilterChip(
                        selected = category == cat,
                        onClick = { category = cat },
                        label = { Text(cat) },
                    )
                }
            }

            // تگ‌ها
            OutlinedTextField(
                value = tagInput, onValueChange = { tagInput = it },
                label = { Text("تگ (با Enter یا دکمه اضافه کن)") },
                singleLine = true,
                trailingIcon = {
                    IconButton(onClick = {
                        val t = tagInput.trim().removePrefix("#")
                        if (t.isNotBlank() && t !in tags) tags = tags + t
                        tagInput = ""
                    }) { Icon(Icons.Filled.AttachFile, "افزودن تگ") }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
            )
            if (tags.isNotEmpty()) {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    items(tags) { tag ->
                        AssistChip(
                            onClick = { tags = tags - tag },
                            label = { Text("#$tag") },
                            trailingIcon = { Icon(Icons.Filled.Close, null, Modifier.size(14.dp)) },
                        )
                    }
                }
            }

            // انتخاب فایل‌ها
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            if (editing != null) "فایل‌های جدید (نسخه ${editing.version + 1})"
                            else "فایل‌های کد *",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f),
                        )
                        OutlinedButton(onClick = { filePicker.launch("*/*") }) {
                            Icon(Icons.Filled.AttachFile, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("انتخاب فایل")
                        }
                    }
                    if (editing != null && fileUris.isEmpty()) {
                        Text(
                            "بدون انتخاب فایل جدید، فقط اطلاعات ویرایش می‌شود",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    fileUris.forEach { uri ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            LangChip(extToLang(uri.lastPathSegment ?: ""))
                            Spacer(Modifier.width(8.dp))
                            Text(
                                uri.lastPathSegment?.substringAfterLast('/') ?: "فایل",
                                style = MaterialTheme.typography.bodySmall,
                                maxLines = 1, overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f),
                            )
                            IconButton(onClick = { fileUris = fileUris - uri }) {
                                Icon(Icons.Filled.Close, "حذف", Modifier.size(16.dp))
                            }
                        }
                    }
                }
            }

            // یادداشت نسخه (فقط ویرایش)
            if (editing != null) {
                OutlinedTextField(
                    value = versionNote, onValueChange = { versionNote = it },
                    label = { Text("یادداشت نسخه (چه چیزی تغییر کرد؟)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                )
            }

            // کاور
            Card(shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("تصویر کاور (اختیاری)",
                            style = MaterialTheme.typography.titleSmall,
                            modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { coverPicker.launch("image/*") }) {
                            Icon(Icons.Filled.Image, null, Modifier.size(16.dp))
                            Spacer(Modifier.width(6.dp))
                            Text("انتخاب عکس")
                        }
                    }
                    coverUri?.let {
                        AsyncImage(
                            model = it, contentDescription = null,
                            modifier = Modifier.fillMaxWidth().height(140.dp),
                        )
                    }
                }
            }

            if (busy && progress > 0f) {
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                )
                Text(
                    "در حال آپلود… ${(progress * 100).toInt()}٪",
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            Button(
                onClick = {
                    if (editing != null) {
                        vm.updateFile(editing, title, description, language, category, tags,
                            fileUris, versionNote) { nav.popBackStack() }
                    } else {
                        vm.upload(title, description, language, category, tags, fileUris, coverUri) {
                            nav.popBackStack()
                        }
                    }
                },
                enabled = !busy && title.isNotBlank() && (editing != null || fileUris.isNotEmpty()),
                modifier = Modifier.fillMaxWidth().height(54.dp),
                shape = RoundedCornerShape(14.dp),
            ) {
                if (busy) CircularProgressIndicator(Modifier.size(22.dp), strokeWidth = 2.dp)
                else {
                    Icon(Icons.Filled.CloudUpload, null)
                    Spacer(Modifier.width(8.dp))
                    Text(if (editing != null) "ذخیره تغییرات" else "انتشار فایل",
                        style = MaterialTheme.typography.titleMedium)
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}
