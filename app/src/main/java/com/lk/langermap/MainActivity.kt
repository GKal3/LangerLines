package com.lk.langermap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.lk.langermap.screens.HomeScreen
import com.lk.langermap.screens.SexScreen
import com.lk.langermap.screens.nav
import com.lk.langermap.ui.theme.LangerMapTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LangerMapTheme {
                SexScreen()
                nav()

            }
        }
    }
}