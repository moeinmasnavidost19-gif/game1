package com.codeshare.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.codeshare.app.data.model.Cats
import com.codeshare.app.data.model.UserRole
import com.codeshare.app.ui.AppViewModel
import com.codeshare.app.ui.Routes
import com.codeshare.app.ui.components.EmptyState
import com.codeshare.app.ui.components.FileCard
import com.codeshare.app.ui.components.UserAvatar

private enum class SortMode(val label: String) {
    NEWEST("جدیدترین"),
    POPULAR("محبوب‌ترین"),
    MOST_DOWNLOADED("پردانلودترین"),
    TOP_RATED("بالاترین امتیاز"),
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: AppViewModel, nav: NavHostController) {
    val files by vm.allFiles.collectAsState()
    val user by vm.currentUser.collectAsState()

    var query by remember { mutableStateOf("") }
    var showSearch by remember { mutableStateOf(false) }
    var selectedLang by remember { mutableStateOf<String?>(null) }
    var selectedCat by remember { mutableStateOf<String?>(null) }
    var sortMode by remember { mutableStateOf(SortMode.NEWEST) }
    var sortMenuOpen by remember { mutableStateOf(false) }
    var trendingOnly by remember { mutableStateOf(false) }

    // فیلتر و مرتب‌سازی
    val filtered = files
        .filter { f ->
            (query.isBlank() || f.title.contains(query, true) ||
                f.description.contains(query, true) ||
                f.tags.any { it.contains(query, true) }) &&
            (selectedLang == null || f.language == selectedLang) &&
            (selectedCat == null || f.category == selectedCat)
        }
        .let { list ->
            when (sortMode) {
                SortMode.NEWEST -> list.sortedByDescending { it.createdAt }
                SortMode.POPULAR -> list.sortedByDescending { it.likes }
                SortMode.MOST_DOWNLOADED -> list.sortedByDescending { it.downloads }
                SortMode.TOP_RATED -> list.sortedByDescending { it.rating }
            }
        }
        .let { list -> if (trendingOnly) list.sortedByDescending { it.trendScore }.take(20) else list }

    val trendingIds = files.sortedByDescending { it.trendScore }.take(5).map { it.id }.toSet()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    if (showSearch) {
                        OutlinedTextField(
                            value = query, onValueChange = { query = it },
                            placeholder = { Text("جستجو در عنوان، توضیح، تگ…") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                        )
                    } else {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("کدشیر", fontWeight = FontWeight.Black)
                            Spacer(Modifier.width(6.dp))
                            Text("</>", fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { showSearch = !showSearch; if (!showSearch) query = "" }) {
                        Icon(if (showSearch) Icons.Filled.Close else Icons.Filled.Search, "جستجو")
                    }
                    IconButton(onClick = { nav.navigate(Routes.NOTIFICATIONS) }) {
                        BadgedBox(badge = {}) { Icon(Icons.Filled.Notifications, "اعلان‌ها") }
                    }
                    Box {
                        IconButton(onClick = { sortMenuOpen = true }) {
                            Icon(Icons.AutoMirrored.Filled.Sort, "مرتب‌سازی")
                        }
                        DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
                            SortMode.entries.forEach { m ->
                                DropdownMenuItem(
                                    text = { Text(if (m == sortMode) "✓ ${m.label}" else m.label) },
                                    onClick = { sortMode = m; sortMenuOpen = false },
                                )
                            }
                        }
                    }
                    user?.let { u ->
                        IconButton(onClick = { nav.navigate(Routes.profile(u.id)) }) {
                            UserAvatar(u.photoUrl, size = 32)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        floatingActionButton = {
            if (user?.canUpload == true) {
                FloatingActionButton(
                    onClick = { nav.navigate(Routes.upload()) },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                    shape = CircleShape,
                ) {
                    Icon(Icons.Filled.Add, "آپلود فایل")
                }
            }
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {

            // ─── نوار میان‌بر ───
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                item {
                    FilterChip(
                        selected = trendingOnly,
                        onClick = { trendingOnly = !trendingOnly },
                        label = { Text("ترندینگ") },
                        leadingIcon = {
                            Icon(Icons.Filled.LocalFireDepartment, null,
                                modifier = Modifier.size(16.dp))
                        },
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { nav.navigate(Routes.BOOKMARKS) },
                        label = { Text("ذخیره‌ها") },
                        leadingIcon = { Icon(Icons.Filled.Bookmark, null, modifier = Modifier.size(16.dp)) },
                    )
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { nav.navigate(Routes.LEADERBOARD) },
                        label = { Text("برترین‌ها") },
                        leadingIcon = { Icon(Icons.Filled.EmojiEvents, null, modifier = Modifier.size(16.dp)) },
                    )
                }
                if (user?.userRole == UserRole.OWNER || user?.userRole == UserRole.ADMIN) {
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { nav.navigate(Routes.ADMIN) },
                            label = { Text("مدیریت") },
                            leadingIcon = {
                                Icon(Icons.Filled.AdminPanelSettings, null, modifier = Modifier.size(16.dp))
                            },
                        )
                    }
                }
                if (user?.userRole == UserRole.OWNER) {
                    item {
                        FilterChip(
                            selected = false,
                            onClick = { nav.navigate(Routes.DASHBOARD) },
                            label = { Text("داشبورد") },
                            leadingIcon = { Icon(Icons.Filled.Dashboard, null, modifier = Modifier.size(16.dp)) },
                        )
                    }
                }
                item {
                    FilterChip(
                        selected = false,
                        onClick = { nav.navigate(Routes.SETTINGS) },
                        label = { Text("تنظیمات") },
                        leadingIcon = { Icon(Icons.Filled.Settings, null, modifier = Modifier.size(16.dp)) },
                    )
                }
            }

            // ─── فیلتر زبان ───
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(Cats.LANGUAGES) { lang ->
                    FilterChip(
                        selected = selectedLang == lang,
                        onClick = { selectedLang = if (selectedLang == lang) null else lang },
                        label = { Text(lang) },
                    )
                }
            }
            Spacer(Modifier.height(4.dp))

            // ─── فیلتر دسته ───
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(Cats.CATEGORIES) { cat ->
                    FilterChip(
                        selected = selectedCat == cat,
                        onClick = { selectedCat = if (selectedCat == cat) null else cat },
                        label = { Text(cat) },
                    )
                }
            }

            // ─── لیست فایل‌ها ───
            if (filtered.isEmpty()) {
                EmptyState(
                    if (files.isEmpty()) "هنوز فایلی منتشر نشده است"
                    else "نتیجه‌ای پیدا نشد 🔍"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(filtered, key = { it.id }) { file ->
                        FileCard(
                            file = file,
                            isTrending = file.id in trendingIds,
                            onClick = { nav.navigate(Routes.detail(file.id)) },
                        )
                    }
                }
            }
        }
    }
}
