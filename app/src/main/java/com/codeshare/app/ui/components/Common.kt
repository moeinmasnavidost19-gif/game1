package com.codeshare.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.codeshare.app.data.model.CodeFile
import com.codeshare.app.data.model.UserRole
import com.codeshare.app.ui.theme.AdminPurple
import com.codeshare.app.ui.theme.OwnerGold
import com.codeshare.app.ui.theme.langColor
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** نشان نقش کنار اسم */
@Composable
fun RoleBadge(role: String) {
    val userRole = runCatching { UserRole.valueOf(role) }.getOrDefault(UserRole.USER)
    if (userRole == UserRole.USER) return
    val (color, text, icon) = when (userRole) {
        UserRole.OWNER -> Triple(OwnerGold, "مالک", Icons.Filled.WorkspacePremium)
        UserRole.ADMIN -> Triple(AdminPurple, "ادمین", Icons.Filled.Shield)
        else -> return
    }
    Surface(
        color = color.copy(alpha = 0.15f),
        shape = RoundedCornerShape(8.dp),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(12.dp))
            Spacer(Modifier.width(3.dp))
            Text(text, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

/** چیپ رنگی زبان برنامه‌نویسی */
@Composable
fun LangChip(lang: String) {
    val color = langColor(lang)
    Surface(color = color.copy(alpha = 0.15f), shape = RoundedCornerShape(8.dp)) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Box(Modifier.size(8.dp).clip(CircleShape).background(color))
            Spacer(Modifier.width(5.dp))
            Text(lang, style = MaterialTheme.typography.labelMedium, color = color, fontWeight = FontWeight.Bold)
        }
    }
}

/** آواتار کاربر */
@Composable
fun UserAvatar(photoUrl: String, size: Int = 40) {
    if (photoUrl.isNotBlank()) {
        AsyncImage(
            model = photoUrl, contentDescription = null,
            modifier = Modifier.size(size.dp).clip(CircleShape),
            contentScale = ContentScale.Crop,
        )
    } else {
        Box(
            modifier = Modifier.size(size.dp).clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                Icons.Filled.Person, null,
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.size((size * 0.6).dp),
            )
        }
    }
}

/** آمار کوچک با آیکون */
@Composable
fun StatChip(icon: ImageVector, value: String, tint: Color = MaterialTheme.colorScheme.onSurfaceVariant) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, tint = tint, modifier = Modifier.size(15.dp))
        Spacer(Modifier.width(3.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, color = tint)
    }
}

fun formatCount(n: Long): String = when {
    n >= 1_000_000 -> "%.1fM".format(n / 1_000_000.0)
    n >= 1_000 -> "%.1fK".format(n / 1_000.0)
    else -> n.toString()
}

fun formatDate(millis: Long): String {
    if (millis == 0L) return ""
    return SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(Date(millis))
}

fun timeAgo(millis: Long): String {
    val diff = System.currentTimeMillis() - millis
    val minutes = diff / 60_000
    val hours = diff / 3_600_000
    val days = diff / 86_400_000
    return when {
        minutes < 1 -> "همین الان"
        minutes < 60 -> "$minutes دقیقه پیش"
        hours < 24 -> "$hours ساعت پیش"
        days < 30 -> "$days روز پیش"
        days < 365 -> "${days / 30} ماه پیش"
        else -> "${days / 365} سال پیش"
    }
}

/** کارت فایل در لیست‌ها */
@Composable
fun FileCard(file: CodeFile, isTrending: Boolean = false, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column {
            if (file.coverUrl.isNotBlank()) {
                AsyncImage(
                    model = file.coverUrl, contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(140.dp),
                    contentScale = ContentScale.Crop,
                )
            }
            Column(Modifier.padding(14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    LangChip(file.language)
                    Spacer(Modifier.width(8.dp))
                    if (isTrending) {
                        Text("🔥", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.width(4.dp))
                    }
                    Spacer(Modifier.weight(1f))
                    Text(
                        timeAgo(file.createdAt),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Spacer(Modifier.height(8.dp))
                Text(
                    file.title,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2, overflow = TextOverflow.Ellipsis,
                )
                if (file.description.isNotBlank()) {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        file.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2, overflow = TextOverflow.Ellipsis,
                    )
                }
                Spacer(Modifier.height(10.dp))
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    UserAvatar(file.authorPhotoUrl, size = 22)
                    Text(
                        file.authorName,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f),
                    )
                    StatChip(Icons.Filled.RemoveRedEye, formatCount(file.views))
                    StatChip(Icons.Filled.Download, formatCount(file.downloads))
                    StatChip(Icons.Filled.Favorite, formatCount(file.likes), tint = Color(0xFFEF4444))
                    if (file.ratingCount > 0) {
                        StatChip(Icons.Filled.Star, "%.1f".format(file.rating), tint = Color(0xFFF59E0B))
                    }
                }
            }
        }
    }
}

/** حالت خالی */
@Composable
fun EmptyState(text: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth().padding(40.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            Icons.Filled.Code, null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
            modifier = Modifier.size(64.dp),
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
