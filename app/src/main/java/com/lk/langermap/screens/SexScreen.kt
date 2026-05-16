package com.lk.langermap.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lk.langermap.R
import com.lk.langermap.ui.theme.*
import androidx.compose.material3.ExperimentalMaterial3Api
import com.lk.langermap.screens.RegionScreen

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SexScreen(onNavigateToRegion: (String) -> Unit = {}) {

    var expanded by remember { mutableStateOf(false) }
    var selectedSex by remember { mutableStateOf<String?>(null) }
    val options = listOf("Male", "Female")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.w))
            .statusBarsPadding()
    ) {

        // ── Gradient background (top half) ──────────────────────────────────
        Image(
            painter = painterResource(id = R.drawable.gradient1),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.TopCenter)
        )

        // ── Header (logo + titolo) ───────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)          // altezza header fissa al 35 %
                .align(Alignment.TopCenter)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo_lm),
                    contentDescription = null,
                    modifier = Modifier.size(width = 59.dp, height = 79.dp)
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontSize = 14.sp,
                    fontFamily = montserratSemiBold
                )
            }

            Text(
                text = stringResource(id = R.string.choose_sex),
                fontSize = 36.sp,
                fontFamily = robotoSemiBold,
                color = colorResource(id = R.color.b),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp)
            )
        }

        // ── Card bianca (bottom sheet statico) ──────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.70f)
                .align(Alignment.BottomCenter)
                .topShadow(
                    color = Color.Black.copy(alpha = 0.15f),
                    blur = 12.dp,
                    offsetY = (-6).dp
                )
                .background(
                    color = colorResource(id = R.color.w),
                    shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp)
                )
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Drag handle
            Box(
                modifier = Modifier
                    .width(199.dp)
                    .height(4.dp)
                    .background(
                        color = colorResource(id = R.color.lav_light),
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Dropdown
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = selectedSex ?: "Select gender",
                    onValueChange = {},
                    readOnly = true,
                    textStyle = androidx.compose.ui.text.TextStyle(  // ← font sul testo selezionato
                        fontFamily = robotoRegular,
                        fontSize = 24.sp
                    ),
                    trailingIcon = {
                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = colorResource(id = R.color.lav_light_trasl),
                        focusedContainerColor  = colorResource(id = R.color.lav_light_trasl),
                        unfocusedBorderColor   = Color.Transparent,
                        focusedBorderColor     = Color.Transparent
                    ),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontFamily = robotoRegular,
                                    fontSize = 24.sp   // ← aggiunto
                                )
                            },
                            onClick = {
                                selectedSex = option
                                expanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    selectedSex?.let { onNavigateToRegion(it) }
                },
                enabled = selectedSex != null,
                modifier = Modifier
                    .width(117.dp)
                    .height(50.dp),
                colors = nextButtonColors(),
                shape = nextButtonShape
            ) {
                Text(
                    text = stringResource(id = R.string.btn_next),
                    fontSize = 24.sp,
                    fontFamily = robotoRegular
                )
            }

            Spacer(modifier = Modifier.height(59.dp))
        }
    }
}