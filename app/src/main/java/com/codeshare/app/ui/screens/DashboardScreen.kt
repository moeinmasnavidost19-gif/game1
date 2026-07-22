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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.codeshare.app.data.model.UserRole
import com.codeshare.app.ui.AppViewModel
import com.codeshare.app.ui.Routes
import com.codeshare.app.ui.components.EmptyState
import com.codeshare.app.ui.components.formatCount
import com.codeshare.app.ui.theme.langColor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(vm: AppViewModel, nav: NavHostController) {
    val currentUser by vm.currentUser.collectAsState()
    val files by vm.allFiles.collectAsState()
    val users by remember { vm.adminRepo.allUsersFlow() }.collectAsState(initial = emptyList())

    if (currentUser?.userRole != UserRole.OWNER) {
        Scaffold { pad -> EmptyState("فقط مالک به داشبورد دسترسی دارد ⛔", Modifier.padding(pad)) }
        return
    }

    val totalViews = files.sumOf { it.views }
    val totalDownloads = files.sumOf { it.downloads }
    val totalLikes = files.sumOf { it.likes }
    val totalComments = files.sumOf { it.commentsCount }
    val topFiles = files.sortedByDescending { it.downloads }.take(5)
    val langDist = files.groupBy { it.language }
        .mapValues { it.value.size }
        .entries.sortedByDescending { it.value }
        .take(6)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("داشبورد آماری 📊") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "بازگشت")
                    }
                },
            )
        },
    ) { pad ->
        LazyColumn(
            Modifier.fillMaxSize().padding(pad),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            // ─── کارت‌های آمار کلی ───
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BigStat("👥", "${users.size}", "کاربر",
                        Color(0xFF6366F1), Modifier.weight(1f))
                    BigStat("📁", "${files.size}", "فایل",
                        Color(0xFF14B8A6), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BigStat("👁️", formatCount(totalViews), "بازدید",
                        Color(0xFFF59E0B), Modifier.weight(1f))
                    BigStat("⬇️", formatCount(totalDownloads), "دانلود",
                        Color(0xFF22C55E), Modifier.weight(1f))
                }
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    BigStat("❤️", formatCount(totalLikes), "لایک",
                        Color(0xFFEF4444), Modifier.weight(1f))
                    BigStat("💬", formatCount(totalComments), "نظر",
                        Color(0xFFA855F7), Modifier.weight(1f))
                }
            }

            // ─── نمودار میله‌ای زبان‌ها ───
            if (langDist.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("توزیع زبان‌ها",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(12.dp))
                            val maxCount = langDist.maxOf { it.value }.coerceAtLeast(1)
                            langDist.forEach { (lang, count) ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(vertical = 4.dp),
                                ) {
                                    Text(lang, style = MaterialTheme.typography.labelMedium,
                                        modifier = Modifier.width(80.dp),
                                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    Box(
                                        Modifier
                                            .weight(1f)
                                            .height(22.dp)
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant),
                                    ) {
                                        Box(
                                            Modifier
                                                .fillMaxWidth(count.toFloat() / maxCount)
                                                .height(22.dp)
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(langColor(lang)),
                                        )
                                    }
                                    Spacer(Modifier.width(8.dp))
                                    Text("$count", style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }
                }
            }

            // ─── محبوب‌ترین فایل‌ها ───
            if (topFiles.isNotEmpty()) {
                item {
                    Card(shape = RoundedCornerShape(16.dp)) {
                        Column(Modifier.padding(16.dp)) {
                            Text("پردانلودترین فایل‌ها 🏆",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold)
                            Spacer(Modifier.height(8.dp))
                            topFiles.forEachIndexed { idx, f ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 6.dp)
                                        .clip(RoundedCornerShape(8.dp)),
                                ) {
                                    Box(
                                        Modifier.size(28.dp).clip(CircleShape)
                                            .background(MaterialTheme.colorScheme.primaryContainer),
                                        contentAlignment = Alignment.Center,
                                    ) {
                                        Text("${idx + 1}",
                                            style = MaterialTheme.typography.labelMedium,
                                            fontWeight = FontWeight.Bold)
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column(Modifier.weight(1f)) {
                                        Text(f.title, style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1, overflow = TextOverflow.Ellipsis)
                                        Text(f.language,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = langColor(f.language))
                                    }
                                    Text("⬇️ ${formatCount(f.downloads)}",
                                        style = MaterialTheme.typography.labelMedium)
                                }
                            }
                        }
                    }
                }
            }

            // ─── فعال‌ترین کاربران ───
            item {
                Card(shape = RoundedCornerShape(16.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("فعال‌ترین کاربران ⚡",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(8.dp))
                        users.sortedByDescending { it.points }.take(5).forEach { u ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp),
                            ) {
                                Text(u.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f),
                                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                                Text("${u.points} امتیاز",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BigStat(emoji: String, value: String, label: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)),
    ) {
        Column(
            Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(emoji, style = MaterialTheme.typography.headlineSmall)
            Text(value, style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black, color = color)
            Text(label, style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
