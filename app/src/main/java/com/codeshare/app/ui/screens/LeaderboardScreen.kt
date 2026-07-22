package com.codeshare.app.ui.screens

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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.codeshare.app.ui.AppViewModel
import com.codeshare.app.ui.Routes
import com.codeshare.app.ui.components.EmptyState
import com.codeshare.app.ui.components.FileCard
import com.codeshare.app.ui.components.RoleBadge
import com.codeshare.app.ui.components.UserAvatar

// ─── لیدربورد ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardScreen(vm: AppViewModel, nav: NavHostController) {
    val users by remember { vm.adminRepo.leaderboardFlow() }.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("برترین کاربران 🏆") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                    }
                },
            )
        },
    ) { pad ->
        if (users.isEmpty()) {
            EmptyState("هنوز کاربری نیست", Modifier.padding(pad))
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            itemsIndexed(users, key = { _, u -> u.id }) { idx, u ->
                val medal = when (idx) {
                    0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "${idx + 1}"
                }
                Card(
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = when (idx) {
                            0 -> Color(0xFFFEF3C7)
                            1 -> Color(0xFFF1F5F9)
                            2 -> Color(0xFFFFEDD5)
                            else -> MaterialTheme.colorScheme.surface
                        }
                    ),
                    modifier = Modifier.fillMaxWidth()
                        .clickable { nav.navigate(Routes.profile(u.id)) },
                ) {
                    Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(36.dp),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(medal, style = MaterialTheme.typography.titleLarge)
                        }
                        Spacer(Modifier.width(10.dp))
                        UserAvatar(u.photoUrl, 42)
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(u.displayName, style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold, color = Color(0xFF0F172A))
                                Spacer(Modifier.width(6.dp))
                                RoleBadge(u.role)
                            }
                            Text("سطح ${u.level} — ${u.levelTitle}",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF475569))
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("${u.points}", style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Black,
                                color = MaterialTheme.colorScheme.primary)
                            Text("امتیاز", style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFF475569))
                        }
                    }
                }
            }
        }
    }
}

// ─── علاقه‌مندی‌ها ───
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarksScreen(vm: AppViewModel, nav: NavHostController) {
    val user by vm.currentUser.collectAsState()
    val allFiles by vm.allFiles.collectAsState()
    val bookmarks by remember(user?.id) {
        vm.interactionRepo.bookmarksFlow(user?.id ?: "-")
    }.collectAsState(initial = emptyList())

    val bookmarkedFiles = allFiles.filter { f -> bookmarks.any { it.fileId == f.id } }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("ذخیره‌شده‌ها 🔖") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                    }
                },
            )
        },
    ) { pad ->
        if (bookmarkedFiles.isEmpty()) {
            EmptyState("هنوز چیزی ذخیره نکردی", Modifier.padding(pad))
            return@Scaffold
        }
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(bookmarkedFiles, key = { it.id }) { file ->
                FileCard(file) { nav.navigate(Routes.detail(file.id)) }
            }
        }
    }
}
