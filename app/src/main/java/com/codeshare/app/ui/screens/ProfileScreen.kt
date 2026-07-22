package com.codeshare.app.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
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
import com.codeshare.app.ui.components.formatDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(vm: AppViewModel, nav: NavHostController, userId: String) {
    val currentUser by vm.currentUser.collectAsState()
    val allFiles by vm.allFiles.collectAsState()
    val allUsers by remember { vm.adminRepo.allUsersFlow() }.collectAsState(initial = emptyList())

    val profile = allUsers.find { it.id == userId }
        ?: currentUser?.takeIf { it.id == userId }
    val isSelf = currentUser?.id == userId
    val userFiles = allFiles.filter { it.authorId == userId }

    var editOpen by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (isSelf) "پروفایل من" else "پروفایل") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                    }
                },
                actions = {
                    if (isSelf) {
                        IconButton(onClick = { editOpen = true }) {
                            Icon(Icons.Filled.Edit, "ویرایش پروفایل")
                        }
                        IconButton(onClick = {
                            vm.signOut()
                            nav.navigate(Routes.AUTH) { popUpTo(0) { inclusive = true } }
                        }) {
                            Icon(Icons.AutoMirrored.Filled.Logout, "خروج",
                                tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
            )
        },
    ) { pad ->
        val p = profile
        if (p == null) {
            EmptyState("کاربر پیدا نشد", Modifier.padding(pad))
            return@Scaffold
        }

        LazyColumn(Modifier.fillMaxSize().padding(pad)) {
            item {
                // ─── هدر گرادیانی ───
                Box(
                    Modifier.fillMaxWidth().background(
                        Brush.verticalGradient(
                            listOf(MaterialTheme.colorScheme.primary,
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.6f))
                        )
                    ).padding(24.dp),
                ) {
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        UserAvatar(p.photoUrl, 88)
                        Spacer(Modifier.height(10.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(p.displayName, style = MaterialTheme.typography.headlineSmall,
                                color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(Modifier.width(8.dp))
                            RoleBadge(p.role)
                        }
                        if (p.bio.isNotBlank()) {
                            Spacer(Modifier.height(4.dp))
                            Text(p.bio, style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.85f))
                        }
                        Spacer(Modifier.height(4.dp))
                        Text("عضویت: ${formatDate(p.createdAt)}",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f))
                    }
                }
            }

            item {
                // ─── سطح و امتیاز ───
                Card(
                    modifier = Modifier.padding(16.dp).fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer),
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("⭐", style = MaterialTheme.typography.headlineMedium)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text("سطح ${p.level} — ${p.levelTitle}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer)
                                Text("${p.points} امتیاز",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        val progress = (p.points % 100) / 100f
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(8.dp),
                        )
                        Text("${100 - (p.points % 100)} امتیاز تا سطح بعد",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onTertiaryContainer)
                    }
                }
            }

            item {
                // ─── آمار ───
                Row(
                    Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    StatCard("📁", "${userFiles.size}", "فایل", Modifier.weight(1f))
                    StatCard("💬", "${p.commentsCount}", "نظر", Modifier.weight(1f))
                    StatCard("❤️", "${userFiles.sumOf { it.likes }}", "لایک", Modifier.weight(1f))
                    StatCard("⬇️", "${userFiles.sumOf { it.downloads }}", "دانلود", Modifier.weight(1f))
                }
                Spacer(Modifier.height(16.dp))
            }

            if (userFiles.isNotEmpty()) {
                item {
                    Text("فایل‌های ${if (isSelf) "من" else p.displayName}",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(horizontal = 16.dp))
                    Spacer(Modifier.height(8.dp))
                }
                items(userFiles, key = { it.id }) { file ->
                    Box(Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
                        FileCard(file) { nav.navigate(Routes.detail(file.id)) }
                    }
                }
            }
            item { Spacer(Modifier.height(40.dp)) }
        }
    }

    // ─── دیالوگ ویرایش پروفایل ───
    if (editOpen && profile != null) {
        var name by remember { mutableStateOf(profile.displayName) }
        var bio by remember { mutableStateOf(profile.bio) }
        AlertDialog(
            onDismissRequest = { editOpen = false },
            title = { Text("ویرایش پروفایل ✏️") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = name, onValueChange = { name = it },
                        label = { Text("نام نمایشی") }, singleLine = true)
                    OutlinedTextField(value = bio, onValueChange = { bio = it },
                        label = { Text("بیو") }, maxLines = 3)
                }
            },
            confirmButton = {
                Button(onClick = {
                    vm.updateProfile(name, bio)
                    editOpen = false
                }) { Text("ذخیره") }
            },
            dismissButton = {
                TextButton(onClick = { editOpen = false }) { Text("انصراف") }
            },
        )
    }
}

@Composable
private fun StatCard(emoji: String, value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = RoundedCornerShape(14.dp)) {
        Column(
            Modifier.padding(vertical = 12.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, style = MaterialTheme.typography.titleLarge)
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
