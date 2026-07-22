package com.codeshare.app.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseOutBack
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.codeshare.app.ui.AppViewModel
import com.codeshare.app.ui.Routes
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(vm: AppViewModel, nav: NavHostController) {
    val scale = remember { Animatable(0.3f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        scale.animateTo(1f, tween(700, easing = EaseOutBack))
    }
    LaunchedEffect(Unit) {
        alpha.animateTo(1f, tween(900))
        delay(1400)
        val dest = if (FirebaseAuth.getInstance().currentUser != null) Routes.HOME else Routes.AUTH
        nav.navigate(dest) { popUpTo(Routes.SPLASH) { inclusive = true } }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color(0xFF1E1B4B), Color(0xFF4F46E5), Color(0xFF6366F1))
            )
        ),
        contentAlignment = Alignment.Center,
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "</>",
                fontSize = 72.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.scale(scale.value),
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "کدشیر",
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight.Black,
                modifier = Modifier.alpha(alpha.value),
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "اشتراک‌گذاری حرفه‌ای کد",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.alpha(alpha.value),
            )
        }
    }
}
