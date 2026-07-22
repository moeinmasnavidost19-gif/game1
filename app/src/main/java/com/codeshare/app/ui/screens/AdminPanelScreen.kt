package com.codeshare.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.codeshare.app.data.model.UserRole
import com.codeshare.app.ui.AppViewModel
import com.codeshare.app.ui.Routes
import com.codeshare.app.ui.components.EmptyState
import com.codeshare.app.ui.components.RoleBadge
import com.codeshare.app.ui.components.UserAvatar
import com.codeshare.app.ui.components.timeAgo

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(vm: AppViewModel, nav: NavHostController) {
    val currentUser by vm.currentUser.collectAsState()
    val users by remember { vm.adminRepo.allUsersFlow() }.collectAsState(initial = emptyList())
    val reports by remember { vm.interactionRepo.reportsFlow() }.collectAsState(initial = emptyList())

    val isOwner = currentUser?.userRole == UserRole.OWNER
    val isAdmin = currentUser?.userRole == UserRole.ADMIN || isOwner

    var tab by remember { mutableIntStateOf(0) }
    var announceOpen by remember { mutableStateOf(false) }

    if (!isAdmin) {
        Scaffold { pad -> EmptyState("دسترسی ندارید ⛔", Modifier.padding(pad)) }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("پنل مدیریت 🛡️") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                    }
                },
                actions = {
                    if (isOwner) {
                        TextButton(onClick = { announceOpen = true }) { Text("📢 اعلامیه") }
                    }
                },
            )
        },
    ) { pad ->
        Column(Modifier.fillMaxSize().padding(pad)) {
            TabRow(selectedTabIndex = tab) {
                Tab(selected = tab == 0, onClick = { tab = 0 },
                    text = { Text("کاربران (${users.size})") })
                Tab(selected = tab == 1, onClick = { tab = 1 },
                    text = { Text("گزارش‌ها (${reports.count { !it.resolved }})") })
            }

            when (tab) {
                0 -> LazyColumn(
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(users, key = { it.id }) { u ->
                        Card(shape = RoundedCornerShape(14.dp), modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    UserAvatar(u.photoUrl, 40)
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(u.displayName,
                                                style = MaterialTheme.typography.titleSmall,
                                                fontWeight = FontWeight.Bold)
                                            Spacer(Modifier.width(6.dp))
                                            RoleBadge(u.role)
                                            if (u.banned) {
                                                Spacer(Modifier.width(6.dp))
                                                Text("🚫 مسدود",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                        Text(u.email, style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        Text("${u.points} امتیاز • ${u.uploadsCount} فایل",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                                // فقط مالک می‌تواند نقش بدهد؛ مالک قابل تغییر نیست
                                if (isOwner && u.userRole != UserRole.OWNER) {
                                    Spacer(Modifier.padding(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        if (u.userRole == UserRole.ADMIN) {
                                            OutlinedButton(onClick = { vm.setRole(u.id, "USER") }) {
                                                Text("عزل از ادمینی")
                                            }
                                        } else {
                                            Button(onClick = { vm.setRole(u.id, "ADMIN") }) {
                                                Text("ارتقا به ادمین ⬆️")
                                            }
                                        }
                                        OutlinedButton(
                                            onClick = { vm.setBanned(u.id, !u.banned) },
                                            colors = androidx.compose.material3.ButtonDefaults
                                                .outlinedButtonColors(
                                                    contentColor = MaterialTheme.colorScheme.error),
                                        ) {
                                            Text(if (u.banned) "رفع مسدودی" else "مسدود کردن")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                1 -> {
                    val pending = reports.filter { !it.resolved }
                    if (pending.isEmpty()) {
                        EmptyState("گزارشی وجود ندارد ✅")
                    } else LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(pending, key = { it.id }) { r ->
                            Card(
                                shape = RoundedCornerShape(14.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                        .copy(alpha = 0.3f)),
                                modifier = Modifier.fillMaxWidth(),
                            ) {
                                Column(Modifier.padding(12.dp)) {
                                    Text(
                                        if (r.targetType == "FILE") "🚩 گزارش فایل" else "🚩 گزارش نظر",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold,
                                    )
                                    Text("گزارش‌دهنده: ${r.reporterName} • ${timeAgo(r.createdAt)}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(Modifier.padding(2.dp))
                                    Text("دلیل: ${r.reason}",
                                        style = MaterialTheme.typography.bodyMedium)
                                    Spacer(Modifier.padding(4.dp))
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = {
                                            nav.navigate(Routes.detail(r.fileId))
                                        }) { Text("مشاهده") }
                                        Button(onClick = { vm.resolveReport(r.id) }) {
                                            Text("بررسی شد ✓")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // ─── اعلامیه سراسری ───
    if (announceOpen) {
        var aTitle by remember { mutableStateOf("") }
        var aBody by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { announceOpen = false },
            title = { Text("اعلامیه سراسری 📢") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(value = aTitle, onValueChange = { aTitle = it },
                        label = { Text("عنوان") }, singleLine = true)
                    OutlinedTextField(value = aBody, onValueChange = { aBody = it },
                        label = { Text("متن پیام") }, maxLines = 4)
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (aTitle.isNotBlank()) {
                        vm.sendAnnouncement(aTitle, aBody)
                        announceOpen = false
                    }
                }) { Text("ارسال به همه") }
            },
            dismissButton = {
                TextButton(onClick = { announceOpen = false }) { Text("انصراف") }
            },
        )
    }
}
