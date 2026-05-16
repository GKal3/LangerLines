package com.lk.langermap.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lk.langermap.R
import com.lk.langermap.ui.theme.montserratSemiBold
import com.lk.langermap.ui.theme.robotoRegular
import com.lk.langermap.ui.theme.robotoSemiBold
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.Dp
import com.lk.langermap.ui.theme.nextButtonColors
import com.lk.langermap.ui.theme.nextButtonShape
import com.lk.langermap.ui.theme.regionButtonColors
import com.lk.langermap.ui.theme.regionButtonShape
import androidx.compose.runtime.setValue
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults

fun Modifier.topShadow(
    color: Color = Color.Black.copy(alpha = 0.15f),
    blur: Dp = 12.dp,
    offsetY: Dp = (-4).dp
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint().apply {
            asFrameworkPaint().apply {
                isAntiAlias = true
                this.color = android.graphics.Color.TRANSPARENT
                setShadowLayer(
                    blur.toPx(),
                    0f,
                    offsetY.toPx(),
                    color.toArgb()
                )
            }
        }
        canvas.drawRoundRect(
            left = 0f,
            top = 0f,
            right = size.width,
            bottom = size.height,
            radiusX = 32.dp.toPx(),
            radiusY = 32.dp.toPx(),
            paint = paint
        )
    }
}
@Composable
fun RegionScreen(sex: String, onNavigateToUpload: (Int, String) -> Unit = { _, _ -> }) {

    // Mappa stringa → drawable delle linee di Langer
    val regionDrawableMap = mapOf(
        R.string.forehead  to R.drawable.langer_forehead,
        R.string.cheek     to R.drawable.langer_cheek,
        R.string.chest     to R.drawable.langer_female_chest,
        R.string.abdomen   to R.drawable.langer_female_abdomen_front,
        R.string.arm       to R.drawable.langer_female_arm,
        R.string.leg       to R.drawable.langer_female_leg
    )

    val regionNames = mapOf(
        R.string.forehead to stringResource(R.string.forehead),
        R.string.cheek to stringResource(R.string.cheek),
        R.string.chest to stringResource(R.string.chest),
        R.string.abdomen to stringResource(R.string.abdomen),
        R.string.arm to stringResource(R.string.arm),
        R.string.leg to stringResource(R.string.leg)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.w))
            .statusBarsPadding()
    ) {

        // Gradient sfondo metà schermo
        Image(
            painter = painterResource(id = R.drawable.gradient1),
            contentDescription = null,
            contentScale = ContentScale.FillBounds,
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.5f)
                .align(Alignment.TopCenter)
        )

        // Header
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.25f)
                .statusBarsPadding()
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
                text = stringResource(id = R.string.choose_region),
                fontSize = 36.sp,
                fontFamily = robotoSemiBold,
                color = colorResource(id = R.color.b),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp)
            )

            Text(
                text = stringResource(id = R.string.choose_region_subtitle),
                fontSize = 20.sp,
                fontFamily = robotoRegular,
                color = colorResource(id = R.color.b),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 6.dp)
            )
        }

        // Card bianca in basso
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

            Spacer(modifier = Modifier.height(24.dp))

            // Griglia 2 colonne
            val regions = listOf(
                R.string.forehead,
                R.string.cheek,
                R.string.chest,
                R.string.abdomen,
                R.string.arm,
                R.string.leg
            )
            var selectedRegion by remember { mutableStateOf<Int?>(null) }
            regions.chunked(2).forEach { rowItems ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(40.dp)
                ) {
                    rowItems.forEach { stringRes ->
                        Button(
                            onClick = { selectedRegion = stringRes },
                            colors = regionButtonColors(isSelected = selectedRegion == stringRes),
                            shape = regionButtonShape,     // ← da ButtonStyles.kt
                            modifier = Modifier
                                .weight(1f)
                                .height(90.dp)
                                .padding(vertical = 8.dp)
                        ) {
                            Text(
                                text = stringResource(id = stringRes),
                                fontSize = 24.sp,
                                fontFamily = robotoRegular,
                                color = colorResource(id = R.color.b)
                            )
                        }
                    }
                    // se la riga ha un solo elemento, riempi l'altro lato
                    if (rowItems.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
            // Checkbox POV — appaiono solo se arm o leg sono selezionati
            val showPov = selectedRegion == R.string.arm || selectedRegion == R.string.leg
            var selectedPov by remember { mutableStateOf("front") } // front di default

            if (showPov) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp),
                    horizontalAlignment = Alignment.Start
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPov == "front",
                            onClick = { selectedPov = "front" },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colorResource(id = R.color.lav_light)
                            )
                        )
                        Text(
                            text = "From front POV",
                            fontFamily = robotoRegular,
                            fontSize = 14.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(
                            selected = selectedPov == "back",
                            onClick = { selectedPov = "back" },
                            colors = RadioButtonDefaults.colors(
                                selectedColor = colorResource(id = R.color.lav_light)
                            )
                        )
                        Text(
                            text = "From back POV",
                            fontFamily = robotoRegular,
                            fontSize = 14.sp
                        )
                    }
                }
            }

            // Bottone Next
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    selectedRegion?.let { region ->
                        val drawable = regionDrawableMap[region] ?: 0
                        val regionName = regionNames[region] ?: ""
                        onNavigateToUpload(drawable, regionName)
                    }
                },
                enabled = selectedRegion != null, // ← disabilitato finché nessuna regione è selezionata
                modifier = Modifier
                    .width(117.dp)
                    .height(50.dp),
                colors = nextButtonColors(), // ← da ButtonStyles.kt
                shape = nextButtonShape,     // ← da ButtonStyles.kt
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

@Preview
@Composable
fun RegionScreenPreview() {
    RegionScreen("male")
}