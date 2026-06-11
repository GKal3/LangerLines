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
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@Preview
@Composable
fun OverlayScreen(
    photoUri: String = "",
    overlayRes: Int = R.drawable.langer_forehead,
    initialOffsetX: Float = 0f,
    initialOffsetY: Float = 0f,
    initialScale: Float = 1f,
    initialRotation: Float = 0f,
    initialOpacity: Float = 0.75f,
    initialColor: Color = Color.Black,
    onStateChanged: (Float, Float, Float, Float, Float, Color) -> Unit = { _, _, _, _, _, _ -> },
    onBack: () -> Unit = {},
    onFinish: (String) -> Unit = {}
) {
    var offsetX       by remember { mutableFloatStateOf(initialOffsetX) }
    var offsetY       by remember { mutableFloatStateOf(initialOffsetY) }
    var scale         by remember { mutableFloatStateOf(initialScale) }
    var rotation      by remember { mutableFloatStateOf(initialRotation) }
    var opacity       by remember { mutableFloatStateOf(initialOpacity) }
    var selectedColor by remember { mutableStateOf(initialColor) }
    var showFullscreen by remember { mutableStateOf(false) }

    // ── DIMENSIONI REALI DELLA BOX (pixel fisici) ─────────────────
    var boxWidthPx  by remember { mutableIntStateOf(0) }
    var boxHeightPx by remember { mutableIntStateOf(0) }

    val context        = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // ── COMPOSIZIONE BITMAP ──────────────────────────────────────
    fun composeBitmap(onReady: (Uri) -> Unit) {
        val bw: Int = boxWidthPx
        val bh: Int = boxHeightPx
        if (bw == 0 || bh == 0) return

        val capturedScale    : Float = scale
        val capturedRotation : Float = rotation
        val capturedOffsetX  : Float = offsetX
        val capturedOffsetY  : Float = offsetY
        val capturedOpacity  : Float = opacity
        val capturedColor    : Color = selectedColor

        coroutineScope.launch(Dispatchers.IO) {

            val photoBitmap = BitmapUtils.loadBitmapFromUri(context, Uri.parse(photoUri))
                ?: return@launch

            val overlayBitmap = android.graphics.BitmapFactory.decodeResource(
                context.resources, overlayRes
            ) ?: return@launch

            val wBmp: Float = photoBitmap.width.toFloat()
            val hBmp: Float = photoBitmap.height.toFloat()
            val wBox: Float = bw.toFloat()
            val hBox: Float = bh.toFloat()
            val wOv : Float = overlayBitmap.width.toFloat()
            val hOv : Float = overlayBitmap.height.toFloat()

            val cropScale: Float = maxOf(wBox / wBmp, hBox / hBmp)
            val fitScaleBox: Float = minOf(wBox / wOv, hBox / hOv)
            val fitScaleBmp: Float = fitScaleBox / cropScale

            val cxBmp: Float = wBmp / 2f
            val cyBmp: Float = hBmp / 2f

            val outW: Int = photoBitmap.width
            val outH: Int = photoBitmap.height
            val result = android.graphics.Bitmap.createBitmap(
                outW, outH, android.graphics.Bitmap.Config.ARGB_8888
            )
            val canvas = android.graphics.Canvas(result)

            canvas.drawBitmap(photoBitmap, 0f, 0f, null)

            val paint = android.graphics.Paint().apply {
                alpha = (capturedOpacity * 255).toInt()
                colorFilter = android.graphics.PorterDuffColorFilter(
                    capturedColor.toArgb(),
                    android.graphics.PorterDuff.Mode.SRC_IN
                )
            }

            val matrix = android.graphics.Matrix()
            matrix.setScale(fitScaleBmp, fitScaleBmp)

            val scaledOvW: Float = wOv * fitScaleBmp
            val scaledOvH: Float = hOv * fitScaleBmp
            matrix.postTranslate(
                (wBmp - scaledOvW) / 2f,
                (hBmp - scaledOvH) / 2f
            )

            matrix.postTranslate(-cxBmp, -cyBmp)
            matrix.postScale(capturedScale, capturedScale)
            matrix.postRotate(capturedRotation)
            matrix.postTranslate(cxBmp, cyBmp)

            matrix.postTranslate(
                capturedOffsetX / cropScale,
                capturedOffsetY / cropScale
            )

            canvas.drawBitmap(overlayBitmap, matrix, paint)

            val savedUri: Uri = BitmapUtils.saveBitmapToCache(result, context)
            withContext(Dispatchers.Main) {
                onReady(savedUri)
            }
        }
    }

    // ── STORICO PER UNDO ─────────────────────────────────────────
    data class Snapshot(
        val ox: Float, val oy: Float, val sc: Float,
        val rot: Float, val op: Float, val col: Color
    )
    val history = remember { ArrayDeque<Snapshot>() }

    fun saveHistory() {
        history.addLast(
            Snapshot(offsetX, offsetY, scale, rotation, opacity, selectedColor)
        )
        if (history.size > 20) history.removeFirst()
        onStateChanged(offsetX, offsetY, scale, rotation, opacity, selectedColor)
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
                .onSizeChanged { size ->
                    boxWidthPx  = size.width
                    boxHeightPx = size.height
                }
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
                    .clickable { showFullscreen = true }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ── DIVIDER ───────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = colorResource(id = R.color.lav_light_trasl)
            )
            Text(
                text = "more overlay settings",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(
                modifier = Modifier.weight(1f),
                color = colorResource(id = R.color.lav_light_trasl)
            )
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
                Icon(
                    painter = painterResource(id = R.drawable.ic_undo),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Undo", color = Color.DarkGray)
            }
            Button(
                onClick = {
                    history.clear()
                    offsetX = 0f; offsetY = 0f
                    scale = 1f; rotation = 0f
                    opacity = 0.75f; selectedColor = Color.Black
                    onStateChanged(0f, 0f, 1f, 0f, 0.75f, Color.Black)
                },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = Color.LightGray),
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reset),
                    contentDescription = null
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", color = Color.DarkGray)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ── FINISH ────────────────────────────────────────────────
        Button(
            onClick = {
                composeBitmap { composedUri ->
                    onFinish(composedUri.toString())
                }
            },
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

        if (showFullscreen) {
            var fsWidthPx  by remember { mutableIntStateOf(0) }
            var fsHeightPx by remember { mutableIntStateOf(0) }

            Dialog(
                onDismissRequest = { showFullscreen = false },
                properties = DialogProperties(
                    usePlatformDefaultWidth = false,
                    dismissOnBackPress = true,
                    dismissOnClickOutside = true
                )
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black)
                        .clickable { showFullscreen = false }
                        .onSizeChanged { size ->
                            fsWidthPx  = size.width
                            fsHeightPx = size.height
                        }
                ) {
                    // LAYER 1 — Foto fullscreen
                    if (photoUri.isNotEmpty()) {
                        AsyncImage(
                            model = Uri.parse(photoUri),
                            contentDescription = "Fullscreen foto paziente",
                            // Usiamo Fit come nella box originale (non Crop)
                            // così la foto e l'overlay occupano lo stesso spazio visivo
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                    }

                    // LAYER 2 — Overlay con trasformazioni riscalate
                    // Calcola il fattore di scala tra la fullscreen e la box originale.
                    // Usiamo min() per mantenere le proporzioni (stesso di ContentScale.Fit).
                    val uniformScale = if (boxWidthPx > 0 && boxHeightPx > 0 &&
                                          fsWidthPx > 0 && fsHeightPx > 0) {
                        minOf(
                            fsWidthPx.toFloat()  / boxWidthPx.toFloat(),
                            fsHeightPx.toFloat() / boxHeightPx.toFloat()
                        )
                    } else 1f

                    // offsetX/offsetY vengono scalati proporzionalmente alla nuova dimensione.
                    // scale e rotation rimangono invariati: graphicsLayer li applica
                    // rispetto al centro del composable, indipendentemente dalle dimensioni.
                    Image(
                        painter = painterResource(id = overlayRes),
                        contentDescription = "Linee di Langer fullscreen",
                        contentScale = ContentScale.Fit,
                        colorFilter = ColorFilter.tint(selectedColor.copy(alpha = opacity)),
                        modifier = Modifier
                            .fillMaxSize()
                            .offset {
                                IntOffset(
                                    (offsetX * uniformScale).roundToInt(),
                                    (offsetY * uniformScale).roundToInt()
                                )
                            }
                            .graphicsLayer(
                                scaleX    = scale,
                                scaleY    = scale,
                                rotationZ = rotation
                            )
                    )

                    // Icona chiudi fullscreen
                    Icon(
                        painter = painterResource(id = R.drawable.ic_full_screen),
                        contentDescription = "Chiudi fullscreen",
                        tint = Color.White,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .size(32.dp)
                            .clickable { showFullscreen = false }
                    )
                }
            }
        }
    }
}