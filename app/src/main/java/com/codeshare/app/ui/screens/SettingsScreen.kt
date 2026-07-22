package com.codeshare.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.codeshare.app.ui.AppViewModel
import com.codeshare.app.ui.components.CodeThemes
import com.codeshare.app.ui.components.CodeViewer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavHostController) {
    val darkPref by vm.darkTheme.collectAsState()
    val codeTheme by vm.codeTheme.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("تنظیمات ⚙️") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            // ─── تم اپ ───
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("تم اپلیکیشن", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = darkPref == false,
                            onClick = { vm.setDarkTheme(false) },
                            label = { Text("روشن") },
                            leadingIcon = { Icon(Icons.Filled.LightMode, null) },
                        )
                        FilterChip(
                            selected = darkPref == true,
                            onClick = { vm.setDarkTheme(true) },
                            label = { Text("تاریک") },
                            leadingIcon = { Icon(Icons.Filled.DarkMode, null) },
                        )
                        FilterChip(
                            selected = darkPref == null,
                            onClick = { vm.setDarkTheme(null) },
                            label = { Text("سیستم") },
                            leadingIcon = { Icon(Icons.Filled.PhoneAndroid, null) },
                        )
                    }
                }
            }

            // ─── تم نمایش کد ───
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp)) {
                    Text("تم نمایش کد", style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CodeThemes.all.forEach { t ->
                            FilterChip(
                                selected = codeTheme == t.name,
                                onClick = { vm.setCodeTheme(t.name) },
                                label = { Text(t.name) },
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    // پیش‌نمایش زنده
                    CodeViewer(
                        code = "fun greet(name: String) {\n    // خوش آمدی!\n    println(\"سلام $name\")\n}",
                        language = "Kotlin",
                        theme = CodeThemes.byName(codeTheme),
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }

            // ─── درباره ───
            Card(shape = RoundedCornerShape(16.dp)) {
                Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("</>", style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.primary)
                    Text("کدشیر", style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold)
                    Text("نسخه ۱.۰.۰", style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(Modifier.height(4.dp))
                    Text("اشتراک‌گذاری حرفه‌ای کد 💜",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}
