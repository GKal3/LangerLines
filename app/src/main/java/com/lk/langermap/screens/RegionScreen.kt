package com.lk.langermap.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.nativeCanvas

fun Modifier.topShadow(
    color: Color = Color.Black.copy(alpha = 0.15f),
    blur: Dp = 12.dp,
    offsetY: Dp = (-4).dp
) = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            isAntiAlias = true
            this.color = android.graphics.Color.TRANSPARENT
            setShadowLayer(
                blur.toPx(),
                0f,
                offsetY.toPx(),
                color.toArgb()
            )
        }
        canvas.nativeCanvas.drawRoundRect(
            0f,
            0f,
            size.width,
            size.height,
            32.dp.toPx(),
            32.dp.toPx(),
            paint
        )
    }
}
@Composable
fun RegionScreen(
    sex: String,
    initialRegion: Int? = null,
    initialPov: String = "front",
    onNavigateToUpload: (Int, String, Int, String) -> Unit = { _, _, _, _ -> },
    onBack: () -> Unit = {}
) {
    var selectedPov by remember { mutableStateOf(initialPov) }
    var selectedRegion by remember { mutableStateOf(initialRegion) }
    val regionDrawableMap = if (sex.lowercase() == "male") {
        mapOf(
            R.string.forehead to R.drawable.langer_forehead,
            R.string.cheek    to R.drawable.langer_cheek,
            R.string.chest    to R.drawable.langer_male_chest,
            R.string.abdomen  to R.drawable.langer_male_abdomen,
            R.string.back  to R.drawable.langer_male_back,
            R.string.arm      to if (selectedPov == "front") R.drawable.langer_male_arm_front else R.drawable.langer_male_arm_back,
            R.string.leg      to if (selectedPov == "front") R.drawable.langer_male_leg_front else R.drawable.langer_male_leg_back
        )
    } else {
        mapOf(
            R.string.forehead to R.drawable.langer_forehead,
            R.string.cheek    to R.drawable.langer_cheek,
            R.string.chest    to R.drawable.langer_female_chest,
            R.string.abdomen  to R.drawable.langer_female_abdomen,
            R.string.back  to R.drawable.langer_female_back,
            R.string.arm      to if (selectedPov == "front") R.drawable.langer_female_arm_front else R.drawable.langer_female_arm_back,
            R.string.leg      to if (selectedPov == "front") R.drawable.langer_female_leg_front else R.drawable.langer_female_leg_back
        )
    }

    val regionNames = mapOf(
        R.string.forehead to stringResource(R.string.forehead),
        R.string.cheek to stringResource(R.string.cheek),
        R.string.chest to stringResource(R.string.chest),
        R.string.abdomen to stringResource(R.string.abdomen),
        R.string.arm to stringResource(R.string.arm),
        R.string.leg to stringResource(R.string.leg),
        R.string.back to stringResource(R.string.back)
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(id = R.color.w))
            .statusBarsPadding()
    ) {

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
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back_arrow),
                        contentDescription = "Back"
                    )
                }
                Image(
                    painter = painterResource(id = R.drawable.logo_lm),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }

            Text(
                text = stringResource(id = R.string.choose_region),
                fontSize = 28.sp,
                fontFamily = robotoSemiBold,
                color = colorResource(id = R.color.b),
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 20.dp)
            )

            Text(
                text = stringResource(id = R.string.choose_region_subtitle),
                fontSize = 14.sp,
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
            val scrollState = androidx.compose.foundation.rememberScrollState()

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(scrollState),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val regions = listOf(
                    R.string.forehead,
                    R.string.cheek,
                    R.string.chest,
                    R.string.abdomen,
                    R.string.arm,
                    R.string.leg,
                    R.string.back
                )

                regions.chunked(2).forEach { rowItems ->
                    if (rowItems.size == 1) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Button(
                                onClick = { selectedRegion = rowItems[0] },
                                colors = regionButtonColors(isSelected = selectedRegion == rowItems[0]),
                                shape = regionButtonShape,
                                modifier = Modifier
                                    .fillMaxWidth(0.5f)
                                    .height(90.dp),
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = stringResource(id = rowItems[0]),
                                    fontSize = 20.sp,
                                    fontFamily = robotoRegular,
                                    color = colorResource(id = R.color.b),
                                    maxLines = 1,
                                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                )
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(40.dp)
                        ) {
                            rowItems.forEach { stringRes ->
                                Button(
                                    onClick = { selectedRegion = stringRes },
                                    colors = regionButtonColors(isSelected = selectedRegion == stringRes),
                                    shape = regionButtonShape,
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(90.dp)
                                        .padding(vertical = 8.dp)
                                ) {
                                    Text(
                                        text = stringResource(id = stringRes),
                                        fontSize = 20.sp,
                                        fontFamily = robotoRegular,
                                        color = colorResource(id = R.color.b),
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                val showPov = selectedRegion == R.string.arm || selectedRegion == R.string.leg
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

                Spacer(modifier = Modifier.height(24.dp))

                // Bottone Next dentro lo scroll
                Button(
                    onClick = {
                        selectedRegion?.let { region ->
                            val drawable = regionDrawableMap[region] ?: 0
                            val regionName = regionNames[region] ?: ""
                            onNavigateToUpload(drawable, regionName, selectedRegion!!, selectedPov)
                        }
                    },
                    enabled = selectedRegion != null,
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
}

@Preview
@Composable
fun RegionScreenPreview() {
    RegionScreen("male")
}