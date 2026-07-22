package com.codeshare.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.codeshare.app.ui.AppNavHost
import com.codeshare.app.ui.AppViewModel
import com.codeshare.app.ui.theme.CodeShareTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: AppViewModel = viewModel()
            val darkPref by vm.darkTheme.collectAsState()
            val dark = darkPref ?: isSystemInDarkTheme()
            CodeShareTheme(darkTheme = dark) {
                AppNavHost(vm)
            }
        }
    }
}
