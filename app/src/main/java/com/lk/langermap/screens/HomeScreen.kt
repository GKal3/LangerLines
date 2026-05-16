package com.lk.langermap.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.lk.langermap.R
import kotlinx.coroutines.delay

@Composable
fun HomeScreen(onNavigateToRegion: () -> Unit = {}) {

    LaunchedEffect(Unit) {
        delay(2000)
        onNavigateToRegion()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.teal_light)),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_home),
            contentDescription = null,
            modifier = Modifier
                .width(295.dp)
                .height(393.dp)
        )
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    HomeScreen()
}