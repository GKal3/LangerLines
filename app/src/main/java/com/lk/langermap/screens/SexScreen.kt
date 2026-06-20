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
import androidx.compose.runtime.DisposableEffect

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun SexScreen(
    initialSex: String? = null,
    onNavigateToRegion: (String) -> Unit = {}
) {
    var selectedSex by remember { mutableStateOf(initialSex) }
    var expanded by remember { mutableStateOf(false) }

    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_STOP) {
                expanded = false
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            expanded = false
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    val options = listOf("Male", "Female")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.teal_light))
            .statusBarsPadding()
    ) {

        // ── HEADER ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.35f)
                .align(Alignment.TopCenter)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = R.drawable.logo_lm),
                    contentDescription = null,
                    modifier = Modifier.size(width = 49.dp, height = 69.dp)
                )
                Text(
                    text = stringResource(id = R.string.app_name),
                    fontSize = 10.sp,
                    fontFamily = montserratSemiBold,
                    color = colorResource(id = R.color.teal_dark)
                )
            }

            Text(
                text = stringResource(id = R.string.choose_sex),
                fontSize = 28.sp,
                fontFamily = robotoSemiBold,
                color = colorResource(id = R.color.b),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp)
            )
        }

        // ── WHITE CARD ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.70f)
                .align(Alignment.BottomCenter)
                .topShadow(
                    color = colorResource(id = R.color.b).copy(alpha = 0.15f),
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

            // ── DRAG HANDLE ───────────────────────────────────────────────
            Box(
                modifier = Modifier
                    .width(199.dp)
                    .height(4.dp)
                    .background(
                        color = colorResource(id = R.color.lav_light),
                        shape = RoundedCornerShape(2.dp)
                    )
            )

            Spacer(modifier = Modifier.height(137.dp))

            // ── DROPDOWN ───────────────────────────────────────────────
            ExposedDropdownMenuBox(
                expanded = expanded,
                onExpandedChange = { expanded = !expanded },
                modifier = Modifier.fillMaxWidth(0.7f)
            ) {
                OutlinedTextField(
                    value = selectedSex ?: "Select gender",
                    onValueChange = {},
                    readOnly = true,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontFamily = robotoRegular,
                        fontSize = 20.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
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
                        .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                        .fillMaxWidth()
                )

                ExposedDropdownMenu(
                    expanded = expanded,
                    onDismissRequest = { expanded = false }
                ) {
                    options.forEachIndexed { index, option ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = option,
                                    fontFamily = robotoRegular,
                                    fontSize = 20.sp,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            },
                            onClick = {
                                selectedSex = option
                                expanded = false
                            }
                        )
                        if (index < options.size - 1) {
                            HorizontalDivider(
                                color = colorResource(id = R.color.l_grey_trasl),
                                thickness = 2.dp
                            )
                        }
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
                    fontSize = 20.sp,
                    fontFamily = robotoRegular
                )
            }

            Spacer(modifier = Modifier.height(59.dp))
        }
    }
}