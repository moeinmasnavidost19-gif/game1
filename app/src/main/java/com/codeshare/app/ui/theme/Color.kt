package com.codeshare.app.ui.theme

import androidx.compose.ui.graphics.Color

// ─── پالت اصلی: بنفش-نیلی حرفه‌ای ───
val Primary = Color(0xFF6366F1)          // Indigo 500
val PrimaryDark = Color(0xFF4F46E5)      // Indigo 600
val PrimaryLight = Color(0xFFA5B4FC)     // Indigo 300
val PrimaryContainer = Color(0xFFE0E7FF) // Indigo 100
val OnPrimaryContainer = Color(0xFF1E1B4B)

val Secondary = Color(0xFF14B8A6)        // Teal 500
val SecondaryContainer = Color(0xFFCCFBF1)
val OnSecondaryContainer = Color(0xFF042F2E)

val Tertiary = Color(0xFFF59E0B)         // Amber 500
val TertiaryContainer = Color(0xFFFEF3C7)
val OnTertiaryContainer = Color(0xFF451A03)

val ErrorColor = Color(0xFFEF4444)
val SuccessColor = Color(0xFF22C55E)
val WarningColor = Color(0xFFF97316)

// ─── پس‌زمینه‌ها ───
val BgLight = Color(0xFFF8FAFC)
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceVariantLight = Color(0xFFF1F5F9)

val BgDark = Color(0xFF0F172A)           // Slate 900
val SurfaceDark = Color(0xFF1E293B)      // Slate 800
val SurfaceVariantDark = Color(0xFF334155)

// ─── رنگ نقش‌ها ───
val OwnerGold = Color(0xFFFBBF24)
val AdminPurple = Color(0xFFA855F7)
val UserGray = Color(0xFF94A3B8)

// ─── رنگ اختصاصی هر زبان برنامه‌نویسی ───
val LangColors: Map<String, Color> = mapOf(
    "Python" to Color(0xFF3776AB),
    "Java" to Color(0xFFED8B00),
    "Kotlin" to Color(0xFF7F52FF),
    "JavaScript" to Color(0xFFF7DF1E),
    "TypeScript" to Color(0xFF3178C6),
    "C++" to Color(0xFF00599C),
    "C#" to Color(0xFF68217A),
    "C" to Color(0xFF555555),
    "HTML" to Color(0xFFE34F26),
    "CSS" to Color(0xFF1572B6),
    "PHP" to Color(0xFF777BB4),
    "Swift" to Color(0xFFF05138),
    "Go" to Color(0xFF00ADD8),
    "Rust" to Color(0xFFCE422B),
    "Ruby" to Color(0xFFCC342D),
    "Dart" to Color(0xFF0175C2),
    "SQL" to Color(0xFF336791),
    "Shell" to Color(0xFF4EAA25),
    "JSON" to Color(0xFF8BC34A),
    "XML" to Color(0xFFFF6600),
    "Markdown" to Color(0xFF083FA1),
    "سایر" to Color(0xFF64748B),
)

fun langColor(lang: String): Color = LangColors[lang] ?: Color(0xFF64748B)
