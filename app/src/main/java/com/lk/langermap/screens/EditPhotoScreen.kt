package com.lk.langermap.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.drag
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.lk.langermap.R
import com.lk.langermap.ui.theme.robotoRegular
import com.lk.langermap.ui.theme.robotoSemiBold
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.abs

private const val MAX_PREVIEW_PX = 1280

data class EditState(
    val cropRect: Rect = Rect(0f, 0f, 1f, 1f),
    val angleDeg: Float = 0f,
    val rotate90Count: Int = 0,          // multiples of 90° CCW
    val flipHorizontal: Boolean = false
)

@Preview(showBackground = true)
@Composable
fun EditPhotoScreen(
    photoUri: Uri = Uri.EMPTY,
    onBack: () -> Unit = {},
    onApply: (Bitmap?) -> Unit = {}
) {
    val context = LocalContext.current

    var editState      by remember { mutableStateOf(EditState()) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap  by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(photoUri) {
        if (photoUri != Uri.EMPTY) {
            val full = withContext(Dispatchers.IO) { loadBitmapFromUri(context, photoUri) }
            originalBitmap = full
            previewBitmap  = withContext(Dispatchers.IO) { scaledForPreview(full) }
        }
    }

    val lavLight      = colorResource(id = R.color.lav_light)
    val lavLightTrasl = colorResource(id = R.color.lav_light_trasl)
    val grey          = colorResource(id = R.color.grey)
    val black         = colorResource(id = R.color.b)
    val white         = colorResource(id = R.color.w)
    val teal          = colorResource(id = R.color.teal)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
            .statusBarsPadding()
    ) {
        // ── HEADER ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    tint               = black
                )
            }
            Text(
                text       = "Edit patient's photo",
                fontSize   = 18.sp,
                fontFamily = robotoSemiBold,
                color      = black,
                modifier   = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            TextButton(onClick = { onApply(applyAllEdits(originalBitmap, editState)) }) {
                Text("Apply", color = teal, fontSize = 16.sp)
            }
        }

        // ── TOOL AREA ────────────────────────────────────────────
        Box(
            modifier        = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            CropRotateTool(
                bitmap         = previewBitmap,
                editState      = editState,
                onCropChanged  = { editState = editState.copy(cropRect = it) },
                onAngleChanged = { editState = editState.copy(angleDeg = it) },
                lavLight       = lavLight,
                lavLightTrasl  = lavLightTrasl
            )
        }

        // ── Divider "Edit tools" ──────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            HorizontalDivider(color = grey)
            Text(
                text       = "Edit tools",
                fontSize   = 11.sp,
                fontFamily = robotoRegular,
                color      = grey,
                modifier   = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
                    .background(white)
                    .padding(horizontal = 6.dp)
            )
        }

        // ── ICON BUTTONS: Flip H + Rotate 90° CCW ────────────────
        Row(
            modifier            = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally),
            verticalAlignment   = Alignment.CenterVertically
        ) {
            // Flip orizzontale
            IconButton(
                onClick  = { editState = editState.copy(flipHorizontal = !editState.flipHorizontal) },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (editState.flipHorizontal) lavLight else lavLightTrasl,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector        = Icons.Filled.Flip,
                    contentDescription = "Flip horizontal",
                    tint               = black,
                    modifier           = Modifier
                        .size(24.dp)
                        .graphicsLayer { rotationZ = 90f }   // Flip è verticale di default, ruotiamo l'icona
                )
            }

            // Ruota 90° antiorario
            IconButton(
                onClick  = {
                    editState = editState.copy(
                        rotate90Count = editState.rotate90Count + 1
                    )
                },
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = lavLightTrasl,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    painter            = painterResource(id = R.drawable.ic_rotate90),
                    contentDescription = "Rotate 90° CCW",
                    tint               = black,
                    modifier           = Modifier.size(24.dp)
                )
            }
        }

        // ── Undo + Reset ─────────────────────────────────────────
        Row(
            modifier              = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            OutlinedButton(
                onClick = {
                    editState = editState.copy(
                        cropRect = Rect(0f, 0f, 1f, 1f),
                        angleDeg = 0f
                    )
                },
                shape  = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = black)
            ) {
                Icon(painterResource(id = R.drawable.ic_undo), null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Undo", fontFamily = robotoRegular, fontSize = 14.sp)
            }

            OutlinedButton(
                onClick = { editState = EditState() },
                shape   = RoundedCornerShape(20.dp),
                colors  = ButtonDefaults.outlinedButtonColors(contentColor = black)
            ) {
                Icon(painterResource(id = R.drawable.ic_reset), null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Reset", fontFamily = robotoRegular, fontSize = 14.sp)
            }
        }
    }
}

// ─── Preview scaling ──────────────────────────────────────────────────────────
internal fun scaledForPreview(bitmap: Bitmap?): Bitmap? {
    bitmap ?: return null
    val maxSide = maxOf(bitmap.width, bitmap.height)
    if (maxSide <= MAX_PREVIEW_PX) return bitmap
    val scale = MAX_PREVIEW_PX.toFloat() / maxSide
    val w = (bitmap.width  * scale).toInt().coerceAtLeast(1)
    val h = (bitmap.height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, w, h, true)
}

// ─── CROP + ROTATE TOOL ───────────────────────────────────────────────────────
//
// Architettura corretta:
//   1. Box ESTERNO (frame dritto) con clipToBounds → taglia fisicamente la foto
//      che sporge quando è ruotata/scalata. L'utente vede solo ciò che è dentro.
//   2. La foto ruota + scala tramite graphicsLayer DENTRO il Box clippato.
//   3. Il crop overlay (Canvas) disegna il rettangolo di crop in coordinate
//      del Box esterno — quindi è sempre dritto e indipendente dalla rotazione.
//   4. cropRect normalizzato [0..1] esprime la frazione del frame dritto,
//      e applyEdits lo applica DOPO aver ruotato+scalato il bitmap.
//
@Composable
private fun CropRotateTool(
    bitmap: Bitmap?,
    editState: EditState,
    onCropChanged: (Rect) -> Unit,
    onAngleChanged: (Float) -> Unit,
    lavLight: Color,
    lavLightTrasl: Color
) {
    var frameSize    by remember { mutableStateOf(IntSize.Zero) }
    val cropPxState  = remember { mutableStateOf<Rect?>(null) }
    var activeHandle by remember { mutableStateOf<String?>(null) }
    val handlePx     = 40f

    // Ogni volta che le dimensioni del frame cambiano, reset del crop all'intero frame
    LaunchedEffect(frameSize) {
        if (frameSize == IntSize.Zero) return@LaunchedEffect
        val r = Rect(0f, 0f, frameSize.width.toFloat(), frameSize.height.toFloat())
        cropPxState.value = r
        onCropChanged(Rect(0f, 0f, 1f, 1f))
    }

    val angleRad  = Math.toRadians(abs(editState.angleDeg).toDouble())
    val autoScale = (
            kotlin.math.abs(kotlin.math.cos(angleRad)).toFloat() +
                    kotlin.math.abs(kotlin.math.sin(angleRad)).toFloat()
            )
    val totalAngle = editState.angleDeg - editState.rotate90Count * 90f

    Column(
        modifier            = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // ── Box clippato: la foto che sporge viene tagliata ───────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()                       // ← taglia fisicamente ciò che sporge
                .onSizeChanged { size -> frameSize = size }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down   = awaitFirstDown(requireUnconsumed = false)
                        val rect   = cropPxState.value ?: return@awaitEachGesture
                        val frameW = frameSize.width.toFloat()
                        val frameH = frameSize.height.toFloat()

                        activeHandle = hitTestHandle(down.position, rect, handlePx)
                            ?: if (rect.contains(down.position)) "body" else null

                        if (activeHandle == null) return@awaitEachGesture
                        down.consume()

                        drag(pointerId = down.id) { change ->
                            change.consume()
                            val delta   = change.position - change.previousPosition
                            val dx      = delta.x
                            val dy      = delta.y
                            val cur     = cropPxState.value ?: return@drag
                            val minSize = 80f

                            val updated: Rect = when (activeHandle) {
                                "tl" -> Rect(
                                    (cur.left + dx).coerceIn(0f, cur.right  - minSize),
                                    (cur.top  + dy).coerceIn(0f, cur.bottom - minSize),
                                    cur.right, cur.bottom
                                )
                                "tr" -> Rect(
                                    cur.left,
                                    (cur.top  + dy).coerceIn(0f, cur.bottom - minSize),
                                    (cur.right + dx).coerceIn(cur.left + minSize, frameW),
                                    cur.bottom
                                )
                                "bl" -> Rect(
                                    (cur.left + dx).coerceIn(0f, cur.right  - minSize),
                                    cur.top,
                                    cur.right,
                                    (cur.bottom + dy).coerceIn(cur.top + minSize, frameH)
                                )
                                "br" -> Rect(
                                    cur.left, cur.top,
                                    (cur.right  + dx).coerceIn(cur.left + minSize, frameW),
                                    (cur.bottom + dy).coerceIn(cur.top  + minSize, frameH)
                                )
                                "body" -> {
                                    val newLeft = (cur.left + dx).coerceIn(0f, frameW - cur.width)
                                    val newTop  = (cur.top  + dy).coerceIn(0f, frameH - cur.height)
                                    Rect(newLeft, newTop, newLeft + cur.width, newTop + cur.height)
                                }
                                else -> cur
                            }

                            cropPxState.value = updated
                            onCropChanged(
                                Rect(
                                    updated.left   / frameW,
                                    updated.top    / frameH,
                                    updated.right  / frameW,
                                    updated.bottom / frameH
                                )
                            )
                        }
                        activeHandle = null
                    }
                }
        ) {
            // Foto ruotata e scalata; clipToBounds del Box padre la ritaglia
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap             = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = totalAngle
                            scaleX    = autoScale * if (editState.flipHorizontal) -1f else 1f
                            scaleY    = autoScale
                        }
                )
            }

            // Overlay crop — Canvas con graphicsLayer per abilitare BlendMode.Clear
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
            ) {
                val crop = cropPxState.value
                    ?: Rect(0f, 0f, size.width, size.height)

                // Oscura tutto il frame
                drawRect(color = Color.Black.copy(alpha = 0.45f))
                // Buca trasparente sull'area di crop
                drawRect(
                    color     = Color.Transparent,
                    topLeft   = Offset(crop.left, crop.top),
                    size      = Size(crop.width, crop.height),
                    blendMode = BlendMode.Clear
                )
                // Bordo bianco
                drawRect(
                    color   = Color.White,
                    topLeft = Offset(crop.left, crop.top),
                    size    = Size(crop.width, crop.height),
                    style   = Stroke(width = 2.dp.toPx())
                )
                // Griglia 3×3
                val tw = crop.width  / 3f
                val th = crop.height / 3f
                for (i in 1..2) {
                    drawLine(
                        Color.White.copy(alpha = 0.4f),
                        Offset(crop.left + tw * i, crop.top),
                        Offset(crop.left + tw * i, crop.bottom),
                        1.dp.toPx()
                    )
                    drawLine(
                        Color.White.copy(alpha = 0.4f),
                        Offset(crop.left,  crop.top + th * i),
                        Offset(crop.right, crop.top + th * i),
                        1.dp.toPx()
                    )
                }
                drawCropHandles(crop, handlePx * 0.55f)
            }
        }

        // ── Slider rotazione ──────────────────────────────────────
        Text(
            "Angle ${editState.angleDeg.toInt()}°",
            fontSize   = 14.sp,
            fontFamily = robotoRegular,
            color      = Color.Black.copy(alpha = 0.6f),
            modifier   = Modifier.padding(top = 8.dp)
        )
        Slider(
            value         = editState.angleDeg,
            onValueChange = onAngleChanged,
            valueRange    = -45f..45f,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors        = SliderDefaults.colors(
                thumbColor         = lavLight,
                activeTrackColor   = lavLight,
                inactiveTrackColor = lavLightTrasl
            )
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────
private fun DrawScope.drawCropHandles(rect: Rect, r: Float) {
    listOf(
        Offset(rect.left,  rect.top),
        Offset(rect.right, rect.top),
        Offset(rect.left,  rect.bottom),
        Offset(rect.right, rect.bottom)
    ).forEach { c ->
        drawRect(Color.White,
            topLeft = Offset(c.x - r, c.y - r),
            size    = Size(r * 2f, r * 2f))
        drawRect(Color(0xFF1E88E5),
            topLeft = Offset(c.x - r, c.y - r),
            size    = Size(r * 2f, r * 2f),
            style   = Stroke(width = 2.5.dp.toPx()))
    }
}

private fun hitTestHandle(offset: Offset, rect: Rect, radius: Float): String? =
    mapOf(
        "tl" to Offset(rect.left,  rect.top),
        "tr" to Offset(rect.right, rect.top),
        "bl" to Offset(rect.left,  rect.bottom),
        "br" to Offset(rect.right, rect.bottom)
    ).entries.firstOrNull { (_, c) ->
        offset.x in (c.x - radius)..(c.x + radius) &&
                offset.y in (c.y - radius)..(c.y + radius)
    }?.key

// ─── Apply all edits to original bitmap ──────────────────────────────────────
internal fun applyEdits(
    original: Bitmap?,
    cropRect: Rect,
    angleDeg: Float,
    rotate90Count: Int,
    flipH: Boolean
): Bitmap? {
    original ?: return null

    val angleRad  = Math.toRadians(abs(angleDeg).toDouble())
    val cosA      = kotlin.math.abs(kotlin.math.cos(angleRad)).toFloat()
    val sinA      = kotlin.math.abs(kotlin.math.sin(angleRad)).toFloat()
    val autoScale = cosA + sinA
    val totalAngle = angleDeg - rotate90Count * 90f

    // ── 1. Calcola il "frame" virtuale (come ContentScale.Fit) ──────────
    //    Scegliamo una dimensione di frame coerente con le proporzioni del bitmap.
    //    Usiamo le dimensioni del bitmap come riferimento (non serve un frame fisico).
    val bmpW = original.width.toFloat()
    val bmpH = original.height.toFloat()

    // Il frame virtuale ha le stesse proporzioni del bitmap originale.
    // "Fit" significa che il bitmap riempie il frame senza tagliare nulla.
    // Quindi frame == bitmap (scala 1:1) — è il caso più semplice.
    // autoScale poi ingrandisce il bitmap ruotato per riempire il frame.

    // ── 2. Crea un canvas della dimensione del frame (= bitmap originale) ─
    //    e disegna il bitmap ruotato+scalato centrato.
    val frameW = bmpW.toInt()
    val frameH = bmpH.toInt()

    val result = Bitmap.createBitmap(frameW, frameH, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(result)

    val matrix = Matrix()
    // Prima scala con autoScale attorno al centro del frame
    matrix.postScale(
        autoScale * if (flipH) -1f else 1f,
        autoScale,
        bmpW / 2f,
        bmpH / 2f
    )
    // Poi ruota attorno al centro del frame
    matrix.postRotate(totalAngle, bmpW / 2f, bmpH / 2f)

    canvas.drawBitmap(original, matrix, null)

    // ── 3. Crop in coordinate-frame ──────────────────────────────────────
    //    cropRect [0..1] è relativo al frame → moltiplichiamo per frameW/H
    val x = (cropRect.left   * frameW).toInt().coerceIn(0, frameW - 1)
    val y = (cropRect.top    * frameH).toInt().coerceIn(0, frameH - 1)
    val w = (cropRect.width  * frameW).toInt()
        .coerceAtLeast(1).coerceAtMost(frameW - x)
    val h = (cropRect.height * frameH).toInt()
        .coerceAtLeast(1).coerceAtMost(frameH - y)

    return Bitmap.createBitmap(result, x, y, w, h)
}

fun applyAllEdits(original: Bitmap?, state: EditState): Bitmap? =
    applyEdits(
        original      = original,
        cropRect      = state.cropRect,
        angleDeg      = state.angleDeg,
        rotate90Count = state.rotate90Count,
        flipH         = state.flipHorizontal
    )

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? = try {
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
} catch (e: Exception) { null }