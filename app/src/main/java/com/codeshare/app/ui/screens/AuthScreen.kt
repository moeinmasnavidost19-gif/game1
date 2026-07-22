package com.codeshare.app.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.codeshare.app.ui.AppViewModel
import com.codeshare.app.ui.Routes
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

private enum class AuthMode { LOGIN, REGISTER, FORGOT }

@Composable
fun AuthScreen(vm: AppViewModel, nav: NavHostController) {
    val context = LocalContext.current
    val busy by vm.busy.collectAsState()
    val user by vm.currentUser.collectAsState()

    var mode by remember { mutableStateOf(AuthMode.LOGIN) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var name by remember { mutableStateOf("") }
    var showPass by remember { mutableStateOf(false) }

    // پس از ورود موفق → صفحه اصلی
    LaunchedEffect(user) {
        if (user != null) {
            nav.navigate(Routes.HOME) { popUpTo(Routes.AUTH) { inclusive = true } }
        }
    }

    val googleLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        runCatching {
            val account = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                .getResult(ApiException::class.java)
            account.idToken?.let { vm.signInGoogle(it) }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize().background(
            Brush.verticalGradient(
                listOf(Color(0xFF1E1B4B), Color(0xFF4F46E5))
            )
        ),
    ) {
        Column(
            modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(48.dp))
            Text("</>", fontSize = 48.sp, fontWeight = FontWeight.Black, color = Color.White)
            Text(
                "کدشیر",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White, fontWeight = FontWeight.Black,
            )
            Spacer(Modifier.height(32.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        when (mode) {
                            AuthMode.LOGIN -> "خوش برگشتی! 👋"
                            AuthMode.REGISTER -> "ساخت حساب جدید ✨"
                            AuthMode.FORGOT -> "بازیابی رمز عبور 🔑"
                        },
                        style = MaterialTheme.typography.titleLarge,
                    )
                    Spacer(Modifier.height(20.dp))

                    if (mode == AuthMode.REGISTER) {
                        OutlinedTextField(
                            value = name, onValueChange = { name = it },
                            label = { Text("نام نمایشی") },
                            leadingIcon = { Icon(Icons.Filled.Person, null) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                        )
                        Spacer(Modifier.height(12.dp))
                    }

                    OutlinedTextField(
                        value = email, onValueChange = { email = it.trim() },
                        label = { Text("ایمیل") },
                        leadingIcon = { Icon(Icons.Filled.Email, null) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                    )

                    if (mode != AuthMode.FORGOT) {
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(
                            value = password, onValueChange = { password = it },
                            label = { Text("رمز عبور") },
                            leadingIcon = { Icon(Icons.Filled.Lock, null) },
                            trailingIcon = {
                                IconButton(onClick = { showPass = !showPass }) {
                                    Icon(
                                        if (showPass) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                                        null,
                                    )
                                }
                            },
                            singleLine = true,
                            visualTransformation = if (showPass) VisualTransformation.None
                                else PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp),
                        )
                    }

                    if (mode == AuthMode.LOGIN) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
                            TextButton(onClick = { mode = AuthMode.FORGOT }) {
                                Text("رمز را فراموش کردی؟", style = MaterialTheme.typography.labelMedium)
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    Button(
                        onClick = {
                            when (mode) {
                                AuthMode.LOGIN -> vm.signIn(email, password)
                                AuthMode.REGISTER -> vm.signUp(email, password, name.ifBlank { "کاربر" })
                                AuthMode.FORGOT -> { vm.resetPassword(email); mode = AuthMode.LOGIN }
                            }
                        },
                        enabled = !busy && email.isNotBlank() &&
                            (mode == AuthMode.FORGOT || password.length >= 6),
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                    ) {
                        if (busy) CircularProgressIndicator(
                            modifier = Modifier.width(22.dp).height(22.dp),
                            color = Color.White, strokeWidth = 2.dp,
                        )
                        else Text(
                            when (mode) {
                                AuthMode.LOGIN -> "ورود"
                                AuthMode.REGISTER -> "ثبت‌نام"
                                AuthMode.FORGOT -> "ارسال ایمیل بازیابی"
                            },
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }

                    if (mode != AuthMode.FORGOT) {
                        Spacer(Modifier.height(16.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            HorizontalDivider(Modifier.weight(1f))
                            Text(
                                "  یا  ",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                            HorizontalDivider(Modifier.weight(1f))
                        }
                        Spacer(Modifier.height(16.dp))
                        OutlinedButton(
                            onClick = {
                                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                                    .requestIdToken(webClientId(context))
                                    .requestEmail()
                                    .build()
                                val client = GoogleSignIn.getClient(context as Activity, gso)
                                client.signOut()
                                googleLauncher.launch(client.signInIntent)
                            },
                            enabled = !busy,
                            modifier = Modifier.fillMaxWidth().height(52.dp),
                            shape = RoundedCornerShape(14.dp),
                        ) {
                            Text("G", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color(0xFF4285F4))
                            Spacer(Modifier.width(10.dp))
                            Text("ورود با گوگل")
                        }
                    }

                    Spacer(Modifier.height(12.dp))
                    TextButton(onClick = {
                        mode = if (mode == AuthMode.LOGIN) AuthMode.REGISTER else AuthMode.LOGIN
                    }) {
                        Text(
                            when (mode) {
                                AuthMode.LOGIN -> "حساب نداری؟ ثبت‌نام کن"
                                else -> "حساب داری؟ وارد شو"
                            }
                        )
                    }
                }
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

/** خواندن default_web_client_id تولیدشده توسط google-services */
private fun webClientId(context: android.content.Context): String {
    val resId = context.resources.getIdentifier(
        "default_web_client_id", "string", context.packageName
    )
    return if (resId != 0) context.getString(resId) else ""
}
