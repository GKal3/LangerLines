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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lk.langermap.R
import com.lk.langermap.ui.theme.robotoRegular
import com.lk.langermap.ui.theme.robotoSemiBold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt
import androidx.core.graphics.createBitmap
import androidx.core.net.toUri

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
    var offsetX by remember { mutableFloatStateOf(initialOffsetX) }
    var offsetY by remember { mutableFloatStateOf(initialOffsetY) }
    var scale by remember { mutableFloatStateOf(initialScale) }
    var rotation by remember { mutableFloatStateOf(initialRotation) }
    var opacity by remember { mutableFloatStateOf(initialOpacity) }
    var selectedColor by remember { mutableStateOf(initialColor) }

    var boxWidthPx  by remember { mutableIntStateOf(0) }
    var boxHeightPx by remember { mutableIntStateOf(0) }

    var photoIntrinsicSize by remember { mutableStateOf<IntSize?>(null) }

    val context        = LocalContext.current
    val density        = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val photoRect by remember(boxWidthPx, boxHeightPx, photoIntrinsicSize) {
        derivedStateOf {
            val intrinsic = photoIntrinsicSize
            if (boxWidthPx == 0 || boxHeightPx == 0 || intrinsic == null) {
                null
            } else {
                val wBox = boxWidthPx.toFloat()
                val hBox = boxHeightPx.toFloat()
                val wBmp = intrinsic.width.toFloat()
                val hBmp = intrinsic.height.toFloat()

                val fitScale = minOf(wBox / wBmp, hBox / hBmp)

                val fittedW = wBmp * fitScale
                val fittedH = hBmp * fitScale
                val left = (wBox - fittedW) / 2f
                val top  = (hBox - fittedH) / 2f

                PhotoRect(left, top, fittedW, fittedH, fitScale)
            }
        }
    }

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
            val photoBitmap = BitmapUtils.loadBitmapFromUri(context, photoUri.toUri())
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

            val fitScaleBmpToBox: Float = minOf(wBox / wBmp, hBox / hBmp)

            val fitScaleOvToBox: Float = minOf(wBox / wOv, hBox / hOv)

            val fitScaleOvToBmp: Float = fitScaleOvToBox / fitScaleBmpToBox

            val cxBmp: Float = wBmp / 2f
            val cyBmp: Float = hBmp / 2f

            val result = createBitmap(photoBitmap.width, photoBitmap.height)
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
            matrix.setScale(fitScaleOvToBmp, fitScaleOvToBmp)
            val scaledOvW = wOv * fitScaleOvToBmp
            val scaledOvH = hOv * fitScaleOvToBmp
            matrix.postTranslate((wBmp - scaledOvW) / 2f, (hBmp - scaledOvH) / 2f)
            matrix.postTranslate(-cxBmp, -cyBmp)
            matrix.postScale(capturedScale, capturedScale)
            matrix.postRotate(capturedRotation)
            matrix.postTranslate(cxBmp, cyBmp)

            matrix.postTranslate(capturedOffsetX / fitScaleBmpToBox, capturedOffsetY / fitScaleBmpToBox)

            canvas.drawBitmap(overlayBitmap, matrix, paint)

            val savedUri = BitmapUtils.saveBitmapToCache(result, context)
            withContext(Dispatchers.Main) { onReady(savedUri) }
        }
    }

    data class Snapshot(
        val ox: Float, val oy: Float, val sc: Float,
        val rot: Float, val op: Float, val col: Color
    )
    val history = remember { ArrayDeque<Snapshot>() }

    fun saveHistory() {
        history.addLast(Snapshot(offsetX, offsetY, scale, rotation, opacity, selectedColor))
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
    val black = colorResource(id = R.color.b)
    val white = colorResource(id = R.color.w)
    val teal  = colorResource(id = R.color.teal)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
            .statusBarsPadding()
    ) {

        // ── HEADER ────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    tint = black
                )
            }
            Text(
                text = "Overlay Langer's lines",
                textAlign = TextAlign.Center,
                fontSize = 18.sp,
                fontFamily = robotoSemiBold,
                color = black,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            TextButton(
                onClick = {
                    composeBitmap { composedUri ->
                        onFinish(composedUri.toString())
                    }
                }
            ) {
                Text("Finish", color = teal, fontSize = 16.sp)
            }
        }

        // ── SUBTITLE ──────────────────────────────────────────────
        Row (
            modifier = Modifier
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = "Correctly adapt the lines to the image",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize   = 18.sp,
                fontFamily = robotoSemiBold,
                modifier   = Modifier
                    .fillMaxWidth()
            )
        }

        // ── CANVAS ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .onSizeChanged { size ->
                    boxWidthPx  = size.width
                    boxHeightPx = size.height
                }
        ) {
            if (photoUri.isNotEmpty()) {
                AsyncImage(
                    model = Uri.parse(photoUri),
                    contentDescription = "Foto paziente",
                    contentScale = ContentScale.Fit,
                    onSuccess = { state ->
                        val drawable = state.result.drawable
                        photoIntrinsicSize = IntSize(
                            drawable.intrinsicWidth,
                            drawable.intrinsicHeight
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            photoRect?.let { rect ->
                val leftDp   = with(density) { rect.left.toDp() }
                val topDp    = with(density) { rect.top.toDp() }
                val widthDp  = with(density) { rect.width.toDp() }
                val heightDp = with(density) { rect.height.toDp() }

                Box(
                    modifier = Modifier
                        .offset(x = leftDp, y = topDp)
                        .size(widthDp, heightDp)
                        .border(
                            width = 1.dp,
                            color = colorResource(id = R.color.lav_light_trasl)
                        )
                )
            }

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
        }

        // ── DIVIDER ───────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = colorResource(id = R.color.lav_light))
            Text(
                text = "more overlay settings",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = colorResource(id = R.color.lav_light))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── SETTINGS ───────────────────────────────────────────────
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .background(
                    color = colorResource(id = R.color.lav_light_trasl).copy(alpha = 0.3f),
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ── ROTATION SLIDER ───────────────────────────────────────────────
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

            // ── OPACITY SLIDER ───────────────────────────────────────────────
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

            // ── COLOR PICK ───────────────────────────────────────────────
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
                Spacer(modifier = Modifier.width(55.dp))
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
        Spacer(modifier = Modifier.height(10.dp))

        // ── UNDO / RESET ─────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { undo() },
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.lav_light_trasl).copy(alpha = 0.55f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_undo),
                    contentDescription = null,
                    tint = black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Undo", color = black)
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(id = R.color.lav_light_trasl).copy(alpha = 0.55f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_reset),
                    contentDescription = null,
                    tint = black,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text("Reset", color = black)
            }
        }

        Spacer(modifier = Modifier.height(59.dp))
    }
}

private data class PhotoRect(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float,
    val fitScale: Float
)