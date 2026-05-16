package com.lk.langermap.screens

import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lk.langermap.R
import com.lk.langermap.ui.theme.robotoRegular
import kotlin.math.roundToInt

@Preview
@Composable
fun OverlayScreen(
    photoUri: String = "",                        // URI stringa della foto caricata
    overlayRes: Int = R.drawable.langer_forehead, // drawable linee Langer dalla regione selezionata
    onBack: () -> Unit = {},
    onFinish: () -> Unit = {}
) {
    // ── STATO TRASFORMAZIONI OVERLAY ─────────────────────────────
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var scale   by remember { mutableFloatStateOf(1f) }
    var rotation by remember { mutableFloatStateOf(0f) }
    var opacity by remember { mutableFloatStateOf(0.75f) }
    var selectedColor by remember { mutableStateOf(Color.Black) }

    // ── STORICO PER UNDO ─────────────────────────────────────────
    data class Snapshot(
        val ox: Float, val oy: Float, val sc: Float,
        val rot: Float, val op: Float, val col: Color
    )
    val history = remember { ArrayDeque<Snapshot>() }

    fun saveHistory() {
        history.addLast(Snapshot(offsetX, offsetY, scale, rotation, opacity, selectedColor))
        if (history.size > 20) history.removeFirst()
    }

    fun undo() {
        if (history.isNotEmpty()) {
            val p = history.removeLast()
            offsetX = p.ox; offsetY = p.oy; scale = p.sc
            rotation = p.rot; opacity = p.op; selectedColor = p.col
        }
    }

    val colorOptions = listOf(Color.Black, Color.White, Color.Red, Color.Green, Color.Blue)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── TOP BAR ───────────────────────────────────────────────
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

        // ── TITOLO ────────────────────────────────────────────────
        Text(
            text = "Overlay Langer's lines",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Correctly adapt the lines to the image",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // ── CANVAS: FOTO + OVERLAY ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.lav_light_trasl),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(
                    colorResource(id = R.color.lav_light_trasl).copy(alpha = 0.1f)
                )
        ) {
            // LAYER 1 — Foto (fissa, non trasformabile)
            if (photoUri.isNotEmpty()) {
                AsyncImage(
                    model = Uri.parse(photoUri),
                    contentDescription = "Foto paziente",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            // LAYER 2 — Linee di Langer sopra la foto
            // Gestures: 1 dito = drag, 2 dita = pinch zoom + rotazione
            Image(
                painter = painterResource(id = overlayRes),
                contentDescription = "Linee di Langer",
                contentScale = ContentScale.Fit,
                colorFilter = ColorFilter.tint(selectedColor.copy(alpha = opacity)),
                modifier = Modifier
                    .fillMaxSize()
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        rotationZ = rotation
                    )
                    .pointerInput(Unit) {
                        detectTransformGestures { _, pan, zoom, rotationDelta ->
                            saveHistory()
                            offsetX += pan.x
                            offsetY += pan.y
                            scale = (scale * zoom).coerceIn(0.3f, 5f)
                            rotation += rotationDelta
                        }
                    }
            )

            // Icona fullscreen in basso a destra
            Icon(
                painter = painterResource(id = R.drawable.ic_full_screen),
                contentDescription = null,
                tint = colorResource(id = R.color.lav_light_trasl),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(8.dp)
                    .size(28.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── DIVIDER ───────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = colorResource(id = R.color.lav_light_trasl))
            Text(
                text = "more overlay settings",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = colorResource(id = R.color.lav_light_trasl))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── PANNELLO SETTINGS ─────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = colorResource(id = R.color.lav_light_trasl).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Rotation slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_rotation),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Rotation", fontSize = 14.sp, fontFamily = robotoRegular)
                Slider(
                    value = rotation,
                    onValueChange = { saveHistory(); rotation = it },
                    valueRange = -180f..180f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = colorResource(id = R.color.lav_light_trasl),
                        activeTrackColor = colorResource(id = R.color.lav_light_trasl)
                    )
                )
            }

            // Opacity slider
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_opacity),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Opacity", fontSize = 14.sp, fontFamily = robotoRegular)
                Slider(
                    value = opacity,
                    onValueChange = { saveHistory(); opacity = it },
                    valueRange = 0f..1f,
                    modifier = Modifier.weight(1f),
                    colors = SliderDefaults.colors(
                        thumbColor = colorResource(id = R.color.lav_light_trasl),
                        activeTrackColor = colorResource(id = R.color.lav_light_trasl)
                    )
                )
            }

            // Color picker
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_colorwheel),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Color", fontSize = 14.sp, fontFamily = robotoRegular)
                Spacer(modifier = Modifier.width(12.dp))
                colorOptions.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .padding(2.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (selectedColor == color) 2.dp else 0.dp,
                                color = Color.DarkGray,
                                shape = CircleShape
                            )
                            .clickable { saveHistory(); selectedColor = color }
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── UNDO / RESET ──────────────────────────────────────────
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { undo() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_undo), contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Undo", color = Color.DarkGray)
            }
            Button(
                onClick = {
                    history.clear()
                    offsetX = 0f; offsetY = 0f
                    scale = 1f; rotation = 0f
                    opacity = 0.75f; selectedColor = Color.Black
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(painter = painterResource(id = R.drawable.ic_reset), contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── FINISH ────────────────────────────────────────────────
        Button(
            onClick = onFinish,
            modifier = Modifier
                .width(160.dp)
                .height(50.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.teal_light)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text(
                text = "Finish",
                fontSize = 20.sp,
                color = Color.White,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}