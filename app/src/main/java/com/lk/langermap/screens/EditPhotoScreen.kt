package com.lk.langermap.screens

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
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
    val rotate90Count: Int = 0,
    val flipHorizontal: Boolean = false
)

/**
 * Trasforma il rettangolo di crop (normalizzato 0..1) per tenerlo coerente
 * con una rotazione di 90° in senso antiorario del frame (la stessa rotazione
 * applicata dal pulsante "Rotate90": totalAngle += -90°).
 */
private fun rotateCropRect90(r: Rect): Rect = Rect(
    left   = r.top,
    top    = 1f - r.right,
    right  = r.bottom,
    bottom = 1f - r.left
)

@Preview(showBackground = true)
@Composable
fun EditPhotoScreen(
    photoUri: Uri = Uri.EMPTY,
    initialEditState: EditState = EditState(),          // ← NUOVO: stato iniziale salvato
    onEditStateChanged: (EditState) -> Unit = {},       // ← NUOVO: callback per persistere
    onBack: () -> Unit = {},
    onApply: (Bitmap?) -> Unit = {}
) {
    val context = LocalContext.current

    // Inizializza con lo stato salvato (es. quando si torna indietro da overlay)
    var editState      by remember { mutableStateOf(initialEditState) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var previewBitmap  by remember { mutableStateOf<Bitmap?>(null) }

    // Helper per aggiornare editState e notificare il viewModel in un colpo solo
    fun updateEdit(new: EditState) {
        editState = new
        onEditStateChanged(new)
    }

    LaunchedEffect(photoUri) {
        if (photoUri != Uri.EMPTY) {
            val full = withContext(Dispatchers.IO) { loadBitmapFromUri(context, photoUri) }
            originalBitmap = full
            previewBitmap  = withContext(Dispatchers.IO) { scaledForPreview(full) }
            // NON resettiamo editState qui: lo stato salvato (initialEditState)
            // viene già usato come valore iniziale di remember{}
        }
    }

    val lavLight      = colorResource(id = R.color.lav_light)
    val lavLightTrasl = colorResource(id = R.color.lav_light_trasl)
    val black         = colorResource(id = R.color.b)
    val white         = colorResource(id = R.color.w)
    val teal          = colorResource(id = R.color.teal)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF2C2C2C))
            .statusBarsPadding()
    ) {
        // ── HEADER ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .background(white)
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

        Spacer(modifier = Modifier.height(10.dp))

        // ── TOOLBAR: Flip + Rotate90 (sx) — Reset (dx) ────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment     = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(
                    onClick  = { updateEdit(editState.copy(flipHorizontal = !editState.flipHorizontal)) },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (editState.flipHorizontal) lavLight else lavLightTrasl,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector        = Icons.Default.Flip,
                        contentDescription = "Flip",
                        modifier           = Modifier
                            .size(24.dp)
                            .graphicsLayer { rotationZ = 90f }
                    )
                }
                IconButton(
                    onClick  = {
                        updateEdit(editState.copy(
                            rotate90Count = editState.rotate90Count + 1,
                            cropRect      = rotateCropRect90(editState.cropRect)
                        ))
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(lavLightTrasl, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        painter            = painterResource(R.drawable.ic_rotate90),
                        contentDescription = "Rotate",
                        modifier           = Modifier.size(24.dp)
                    )
                }
            }

            OutlinedButton(
                onClick = { updateEdit(EditState()) },
                shape  = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor   = black,
                    containerColor = white
                )
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_reset),
                    contentDescription = null,
                    modifier           = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Reset", fontFamily = robotoRegular, fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── TOOL AREA ────────────────────────────────────────────
        Box(
            modifier         = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            CropRotateTool(
                bitmap         = previewBitmap,
                editState      = editState,
                onCropChanged  = { newCrop -> updateEdit(editState.copy(cropRect = newCrop)) },
                onAngleChanged = { updateEdit(editState.copy(angleDeg = it)) }
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
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

// ─── Rettangolo della foto in coordinate del Box (ContentScale.Fit) ───────────
private fun fitRect(boxW: Float, boxH: Float, imgW: Float, imgH: Float): Rect {
    if (boxW <= 0f || boxH <= 0f || imgW <= 0f || imgH <= 0f) return Rect(0f, 0f, boxW, boxH)
    val scale = minOf(boxW / imgW, boxH / imgH)
    val w     = imgW * scale
    val h     = imgH * scale
    val left  = (boxW - w) / 2f
    val top   = (boxH - h) / 2f
    return Rect(left, top, left + w, top + h)
}

// ─── CROP + ROTATE TOOL ───────────────────────────────────────────────────────
@Composable
private fun CropRotateTool(
    bitmap: Bitmap?,
    editState: EditState,
    onCropChanged: (Rect) -> Unit,
    onAngleChanged: (Float) -> Unit
) {
    val lavLightColor = colorResource(id = R.color.lav_light)
    val lavLightTrasl = colorResource(id = R.color.lav_light_trasl)

    var boxSize     by remember { mutableStateOf(IntSize.Zero) }
    var imageBounds by remember { mutableStateOf(Rect.Zero) }
    val cropPxState  = remember { mutableStateOf<Rect?>(null) }
    var activeHandle by remember { mutableStateOf<String?>(null) }
    val handlePx     = 40f

    LaunchedEffect(boxSize, bitmap, editState.rotate90Count, editState.cropRect) {
        if (boxSize == IntSize.Zero) return@LaunchedEffect
        val bmpW = bitmap?.width?.toFloat()  ?: 1f
        val bmpH = bitmap?.height?.toFloat() ?: 1f

        val (effectiveW, effectiveH) = if (editState.rotate90Count % 2 == 0) {
            bmpW to bmpH
        } else {
            bmpH to bmpW
        }

        val bounds = fitRect(
            boxSize.width.toFloat(),
            boxSize.height.toFloat(),
            effectiveW,
            effectiveH
        )
        imageBounds = bounds

        val r = editState.cropRect
        cropPxState.value = Rect(
            r.left   * bounds.width,
            r.top    * bounds.height,
            r.right  * bounds.width,
            r.bottom * bounds.height
        )
    }

    val angleRad   = Math.toRadians(abs(editState.angleDeg).toDouble())
    val autoScale  = (
            kotlin.math.abs(kotlin.math.cos(angleRad)).toFloat() +
                    kotlin.math.abs(kotlin.math.sin(angleRad)).toFloat()
            )
    val totalAngle = editState.angleDeg + editState.rotate90Count * -90f

    val bmpW = bitmap?.width?.toFloat()  ?: 1f
    val bmpH = bitmap?.height?.toFloat() ?: 1f
    val isTransposed = editState.rotate90Count % 2 != 0
    val rotationFitScale = if (isTransposed) {
        maxOf(bmpW, bmpH) / minOf(bmpW, bmpH)
    } else {
        1f
    }

    Column(
        modifier            = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .clipToBounds()
                .onSizeChanged { size -> boxSize = size }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down   = awaitFirstDown(requireUnconsumed = false)
                        val crop   = cropPxState.value ?: return@awaitEachGesture
                        val bounds = imageBounds
                        if (bounds == Rect.Zero) return@awaitEachGesture

                        val localPos = down.position - Offset(bounds.left, bounds.top)

                        activeHandle = hitTestHandle(localPos, crop, handlePx)
                            ?: if (crop.contains(localPos)) "body" else null

                        if (activeHandle == null) return@awaitEachGesture
                        down.consume()

                        drag(pointerId = down.id) { change ->
                            change.consume()
                            val delta   = change.position - change.previousPosition
                            val dx      = delta.x
                            val dy      = delta.y
                            val cur     = cropPxState.value ?: return@drag
                            val minSize = 80f
                            val maxW    = bounds.width
                            val maxH    = bounds.height

                            val updated: Rect = when (activeHandle) {
                                "tl" -> Rect(
                                    (cur.left + dx).coerceIn(0f, cur.right  - minSize),
                                    (cur.top  + dy).coerceIn(0f, cur.bottom - minSize),
                                    cur.right, cur.bottom
                                )
                                "tr" -> Rect(
                                    cur.left,
                                    (cur.top  + dy).coerceIn(0f, cur.bottom - minSize),
                                    (cur.right + dx).coerceIn(cur.left + minSize, maxW),
                                    cur.bottom
                                )
                                "bl" -> Rect(
                                    (cur.left + dx).coerceIn(0f, cur.right  - minSize),
                                    cur.top,
                                    cur.right,
                                    (cur.bottom + dy).coerceIn(cur.top + minSize, maxH)
                                )
                                "br" -> Rect(
                                    cur.left, cur.top,
                                    (cur.right  + dx).coerceIn(cur.left + minSize, maxW),
                                    (cur.bottom + dy).coerceIn(cur.top  + minSize, maxH)
                                )
                                "body" -> {
                                    val newLeft = (cur.left + dx).coerceIn(0f, maxW - cur.width)
                                    val newTop  = (cur.top  + dy).coerceIn(0f, maxH - cur.height)
                                    Rect(newLeft, newTop, newLeft + cur.width, newTop + cur.height)
                                }
                                else -> cur
                            }

                            cropPxState.value = updated
                            onCropChanged(
                                Rect(
                                    updated.left   / maxW,
                                    updated.top    / maxH,
                                    updated.right  / maxW,
                                    updated.bottom / maxH
                                )
                            )
                        }
                        activeHandle = null
                    }
                }
        ) {
            val bounds = imageBounds
            if (bounds != Rect.Zero) {
                val density = LocalDensity.current
                val offsetX = with(density) { bounds.left.toDp() }
                val offsetY = with(density) { bounds.top.toDp() }
                val imgW    = with(density) { bounds.width.toDp() }
                val imgH    = with(density) { bounds.height.toDp() }

                Box(
                    modifier = Modifier
                        .absoluteOffset(x = offsetX, y = offsetY)
                        .size(width = imgW, height = imgH)
                        .clipToBounds()
                ) {
                    if (bitmap != null) {
                        Image(
                            bitmap             = bitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier           = Modifier
                                .fillMaxSize()
                                .graphicsLayer {
                                    rotationZ = totalAngle
                                    scaleX    = autoScale * rotationFitScale * if (editState.flipHorizontal) -1f else 1f
                                    scaleY    = autoScale * rotationFitScale
                                }
                        )
                    }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen }
                    ) {
                        val crop = cropPxState.value
                            ?: Rect(0f, 0f, size.width, size.height)

                        drawRect(color = Color.Black.copy(alpha = 0.45f))
                        drawRect(
                            color     = Color.Transparent,
                            topLeft   = Offset(crop.left, crop.top),
                            size      = Size(crop.width, crop.height),
                            blendMode = BlendMode.Clear
                        )
                        drawRect(
                            color   = Color.White,
                            topLeft = Offset(crop.left, crop.top),
                            size    = Size(crop.width, crop.height),
                            style   = Stroke(width = 2.dp.toPx())
                        )
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
                        drawCropHandles(crop)
                    }
                }
            }
        }

        Text(
            text       = "Angle ${editState.angleDeg.toInt()}°",
            fontSize   = 16.sp,
            fontFamily = robotoRegular,
            color      = lavLightColor,
            modifier   = Modifier.padding(top = 8.dp)
        )
        Slider(
            value         = editState.angleDeg,
            onValueChange = { newAngle -> onAngleChanged(newAngle) },
            valueRange    = -45f..45f,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = SliderDefaults.colors(
                thumbColor         = lavLightColor,
                activeTrackColor   = lavLightColor,
                inactiveTrackColor = lavLightTrasl
            )
        )
    }
}

// ─── Helpers ──────────────────────────────────────────────────────────────────

private fun DrawScope.drawCropHandles(rect: Rect) {
    val l = 22.dp.toPx()
    val s =  4.dp.toPx()
    val o =  3.dp.toPx()

    drawLine(Color.White, Offset(rect.left + o, rect.top + o),    Offset(rect.left + l, rect.top + o),    s)
    drawLine(Color.White, Offset(rect.left + o, rect.top + o),    Offset(rect.left + o, rect.top + l),    s)
    drawLine(Color.White, Offset(rect.right - l, rect.top + o),   Offset(rect.right - o, rect.top + o),   s)
    drawLine(Color.White, Offset(rect.right - o, rect.top + o),   Offset(rect.right - o, rect.top + l),   s)
    drawLine(Color.White, Offset(rect.left + o, rect.bottom - o), Offset(rect.left + l, rect.bottom - o), s)
    drawLine(Color.White, Offset(rect.left + o, rect.bottom - l), Offset(rect.left + o, rect.bottom - o), s)
    drawLine(Color.White, Offset(rect.right - l, rect.bottom - o), Offset(rect.right - o, rect.bottom - o), s)
    drawLine(Color.White, Offset(rect.right - o, rect.bottom - l), Offset(rect.right - o, rect.bottom - o), s)
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

    val angleRad   = Math.toRadians(abs(angleDeg).toDouble())
    val cosA       = kotlin.math.abs(kotlin.math.cos(angleRad)).toFloat()
    val sinA       = kotlin.math.abs(kotlin.math.sin(angleRad)).toFloat()
    val autoScale  = cosA + sinA
    val totalAngle = angleDeg - rotate90Count * 90f

    val bmpW = original.width.toFloat()
    val bmpH = original.height.toFloat()

    val isTransposed = rotate90Count % 2 != 0
    val frameW = if (isTransposed) bmpH.toInt() else bmpW.toInt()
    val frameH = if (isTransposed) bmpW.toInt() else bmpH.toInt()

    val result = Bitmap.createBitmap(frameW, frameH, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(result)

    val matrix = Matrix()
    matrix.postTranslate((frameW - bmpW) / 2f, (frameH - bmpH) / 2f)
    matrix.postScale(
        autoScale * if (flipH) -1f else 1f,
        autoScale,
        frameW / 2f,
        frameH / 2f
    )
    matrix.postRotate(totalAngle, frameW / 2f, frameH / 2f)
    canvas.drawBitmap(original, matrix, null)

    val x = (cropRect.left   * frameW).toInt().coerceIn(0, frameW - 1)
    val y = (cropRect.top    * frameH).toInt().coerceIn(0, frameH - 1)
    val w = (cropRect.width  * frameW).toInt().coerceAtLeast(1).coerceAtMost(frameW - x)
    val h = (cropRect.height * frameH).toInt().coerceAtLeast(1).coerceAtMost(frameH - y)

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