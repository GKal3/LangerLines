package com.lk.langermap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lk.langermap.screens.Navigation
import com.lk.langermap.ui.theme.LangerMapTheme
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LangerMapTheme {
                Navigation(viewModel = viewModel)
            }
        }
    }
}