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
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
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


private const val MAX_PREVIEW_PX = 1280


// ── Solo due tool ora ─────────────────────────────────────────────────────────
enum class EditTool { CROP_ROTATE, MIRROR }


data class EditState(
    val cropRect: Rect = Rect(0f, 0f, 1f, 1f),
    val angleDeg: Float = 0f,
    val flipVertical: Boolean = false,
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

    var editState  by remember { mutableStateOf(EditState()) }
    var activeTool by remember { mutableStateOf(EditTool.CROP_ROTATE) }

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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(white)
            .statusBarsPadding()
    ) {
        // ── Top bar ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    tint = black
                )
            }
            Spacer(modifier = Modifier.weight(1f))
            Image(
                painter = painterResource(id = R.drawable.logo_lm),
                contentDescription = null,
                modifier = Modifier.size(width = 40.dp, height = 54.dp)
            )
        }

        Text(
            text = "Edit patient's photo",
            fontSize = 28.sp,
            fontFamily = robotoSemiBold,
            color = black,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
        )

        // ── Area tool ─────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (activeTool) {
                EditTool.CROP_ROTATE -> CropRotateTool(
                    bitmap         = previewBitmap,
                    editState      = editState,
                    onCropChanged  = { editState = editState.copy(cropRect = it) },
                    onAngleChanged = { editState = editState.copy(angleDeg = it) },
                    onReset        = { editState = editState.copy(
                        cropRect = Rect(0f, 0f, 1f, 1f),
                        angleDeg = 0f
                    )},
                    lavLight       = lavLight,
                    lavLightTrasl  = lavLightTrasl
                )
                EditTool.MIRROR -> MirrorTool(
                    bitmap        = cropPreviewBitmap(previewBitmap, editState.cropRect),
                    editState     = editState,
                    onFlipV       = { editState = editState.copy(flipVertical   = !editState.flipVertical) },
                    onFlipH       = { editState = editState.copy(flipHorizontal = !editState.flipHorizontal) },
                    onReset       = { editState = editState.copy(flipVertical = false, flipHorizontal = false) },
                    lavLight      = lavLight,
                    lavLightTrasl = lavLightTrasl
                )
            }
        }

        // ── Divider "Edit tools" ───────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            HorizontalDivider(color = grey)
            Text(
                text = "Edit tools",
                fontSize = 11.sp,
                fontFamily = robotoRegular,
                color = grey,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .offset(y = (-8).dp)
                    .background(white)
                    .padding(horizontal = 6.dp)
            )
        }

        // ── Tab tool ──────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolTabButton(
                label = "Crop & Rotate",
                icon  = {
                    Row {
                        Icon(Icons.Filled.Crop, null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(2.dp))
                        Icon(Icons.AutoMirrored.Filled.RotateRight, null, modifier = Modifier.size(18.dp))
                    }
                },
                selected      = activeTool == EditTool.CROP_ROTATE,
                activeColor   = lavLight,
                inactiveColor = lavLightTrasl,
                textColor     = black
            ) { activeTool = EditTool.CROP_ROTATE }

            ToolTabButton(
                label = "Mirror",
                icon  = { Icon(Icons.Filled.Flip, null, modifier = Modifier.size(18.dp)) },
                selected      = activeTool == EditTool.MIRROR,
                activeColor   = lavLight,
                inactiveColor = lavLightTrasl,
                textColor     = black
            ) { activeTool = EditTool.MIRROR }
        }

        // ── Reset globale ─────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp, top = 0.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(
                onClick = {
                    editState  = EditState()
                    activeTool = EditTool.CROP_ROTATE
                },
                shape  = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = black)
            ) {
                Icon(
                    painterResource(id = R.drawable.ic_reset),
                    null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text("Reset All", fontFamily = robotoRegular, fontSize = 14.sp)
            }
        }

        // ── Apply ─────────────────────────────────────────────────
        Button(
            onClick = {
                onApply(applyAllEdits(originalBitmap, editState))
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
                .width(160.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = grey,
                contentColor   = black
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Text("Apply", fontSize = 20.sp, fontFamily = robotoRegular)
        }
    }
}


// ─── Riduce bitmap per preview ────────────────────────────────────────────────
internal fun scaledForPreview(bitmap: Bitmap?): Bitmap? {
    bitmap ?: return null
    val maxSide = maxOf(bitmap.width, bitmap.height)
    if (maxSide <= MAX_PREVIEW_PX) return bitmap
    val scale = MAX_PREVIEW_PX.toFloat() / maxSide
    val w = (bitmap.width  * scale).toInt().coerceAtLeast(1)
    val h = (bitmap.height * scale).toInt().coerceAtLeast(1)
    return Bitmap.createScaledBitmap(bitmap, w, h, true)
}


// ─── Tab button ───────────────────────────────────────────────────────────────
@Composable
private fun RowScope.ToolTabButton(
    label: String,
    icon: @Composable () -> Unit,
    selected: Boolean,
    activeColor: Color,
    inactiveColor: Color,
    textColor: Color,
    onClick: () -> Unit
) {
    Button(
        onClick        = onClick,
        modifier       = Modifier.weight(1f).height(44.dp),
        colors         = ButtonDefaults.buttonColors(
            containerColor = if (selected) activeColor else inactiveColor,
            contentColor   = textColor
        ),
        shape          = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        icon()
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 14.sp, fontFamily = robotoRegular)
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// CROP + ROTATE TOOL  (tool unificato)
// ═══════════════════════════════════════════════════════════════════════════════
// Layout verticale:
//   [immagine con overlay crop]
//   [slider angolo]
//   [label angolo + reset]
@Composable
private fun CropRotateTool(
    bitmap: Bitmap?,
    editState: EditState,
    onCropChanged: (Rect) -> Unit,
    onAngleChanged: (Float) -> Unit,
    onReset: () -> Unit,
    lavLight: Color,
    lavLightTrasl: Color
) {
    var boxSize      by remember { mutableStateOf(IntSize.Zero) }
    val cropPx        = remember { mutableStateOf<Rect?>(null) }
    var activeHandle by remember { mutableStateOf<String?>(null) }
    val handlePx     = 40f

    // Inizializza cropPx dalle coordinate normalizzate
    if (cropPx.value == null && boxSize != IntSize.Zero) {
        val w = boxSize.width.toFloat()
        val h = boxSize.height.toFloat()
        cropPx.value = Rect(
            left   = editState.cropRect.left   * w,
            top    = editState.cropRect.top    * h,
            right  = editState.cropRect.right  * w,
            bottom = editState.cropRect.bottom * h
        )
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── Immagine + overlay crop ────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onSizeChanged { newSize ->
                    if (newSize != boxSize) {
                        boxSize = newSize
                        cropPx.value = Rect(
                            left   = editState.cropRect.left   * newSize.width,
                            top    = editState.cropRect.top    * newSize.height,
                            right  = editState.cropRect.right  * newSize.width,
                            bottom = editState.cropRect.bottom * newSize.height
                        )
                    }
                }
                .pointerInput(Unit) {
                    awaitEachGesture {
                        val down = awaitFirstDown(requireUnconsumed = false)
                        val rect = cropPx.value ?: return@awaitEachGesture

                        activeHandle = hitTestHandle(down.position, rect, handlePx)
                            ?: if (rect.contains(down.position)) "body" else null

                        if (activeHandle == null) return@awaitEachGesture
                        down.consume()

                        drag(pointerId = down.id) { change ->
                            change.consume()
                            val delta   = change.position - change.previousPosition
                            val dx      = delta.x
                            val dy      = delta.y
                            val cur     = cropPx.value ?: return@drag
                            val w       = boxSize.width.toFloat()
                            val h       = boxSize.height.toFloat()
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
                                    (cur.right + dx).coerceIn(cur.left + minSize, w),
                                    cur.bottom
                                )
                                "bl" -> Rect(
                                    (cur.left + dx).coerceIn(0f, cur.right - minSize),
                                    cur.top,
                                    cur.right,
                                    (cur.bottom + dy).coerceIn(cur.top + minSize, h)
                                )
                                "br" -> Rect(
                                    cur.left, cur.top,
                                    (cur.right  + dx).coerceIn(cur.left + minSize, w),
                                    (cur.bottom + dy).coerceIn(cur.top  + minSize, h)
                                )
                                "body" -> {
                                    val l = (cur.left + dx).coerceIn(0f, w - cur.width)
                                    val t = (cur.top  + dy).coerceIn(0f, h - cur.height)
                                    Rect(l, t, l + cur.width, t + cur.height)
                                }
                                else -> cur
                            }

                            cropPx.value = updated
                            onCropChanged(
                                Rect(
                                    updated.left   / w,
                                    updated.top    / h,
                                    updated.right  / w,
                                    updated.bottom / h
                                )
                            )
                        }
                        activeHandle = null
                    }
                }
        ) {
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap             = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = editState.angleDeg
                            scaleX    = if (editState.flipHorizontal) -1f else 1f
                            scaleY    = if (editState.flipVertical)   -1f else 1f
                        }
                )
            }

            // Overlay crop (disegnato sopra l'immagine ruotata)
            Canvas(modifier = Modifier.fillMaxSize()) {
                val rect = cropPx.value ?: Rect(0f, 0f, size.width, size.height)

                drawPath(
                    path = Path().apply {
                        op(
                            Path().apply { addRect(Rect(0f, 0f, size.width, size.height)) },
                            Path().apply { addRect(rect) },
                            PathOperation.Difference
                        )
                    },
                    color = Color.Black.copy(alpha = 0.45f)
                )
                drawRect(
                    color   = Color.White,
                    topLeft = Offset(rect.left, rect.top),
                    size    = Size(rect.width, rect.height),
                    style   = Stroke(width = 2.dp.toPx())
                )
                val tw = rect.width  / 3f
                val th = rect.height / 3f
                for (i in 1..2) {
                    drawLine(Color.White.copy(alpha = 0.4f),
                        Offset(rect.left + tw * i, rect.top),
                        Offset(rect.left + tw * i, rect.bottom), 1.dp.toPx())
                    drawLine(Color.White.copy(alpha = 0.4f),
                        Offset(rect.left,  rect.top + th * i),
                        Offset(rect.right, rect.top + th * i), 1.dp.toPx())
                }
                drawCropHandles(rect, handlePx * 0.55f)
            }
        }

        // ── Slider rotazione ──────────────────────────────────────
        Text(
            "Angle ${editState.angleDeg.toInt()}°",
            fontSize = 14.sp,
            fontFamily = robotoRegular,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )
        Slider(
            value         = editState.angleDeg,
            onValueChange = onAngleChanged,
            valueRange    = -180f..180f,
            modifier      = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors        = SliderDefaults.colors(
                thumbColor         = lavLight,
                activeTrackColor   = lavLight,
                inactiveTrackColor = lavLightTrasl
            )
        )

        // ── Reset locale ──────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            ResetButton {
                onReset()
                cropPx.value = null   // forza il re-init dell'overlay crop
            }
        }
    }
}


// ═══════════════════════════════════════════════════════════════════════════════
// MIRROR TOOL  (invariato)
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun MirrorTool(
    bitmap: Bitmap?,
    editState: EditState,
    onFlipV: () -> Unit,
    onFlipH: () -> Unit,
    onReset: () -> Unit,
    lavLight: Color,
    lavLightTrasl: Color
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().weight(1f),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap             = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            rotationZ = editState.angleDeg
                            scaleX    = if (editState.flipHorizontal) -1f else 1f
                            scaleY    = if (editState.flipVertical)   -1f else 1f
                        }
                )
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = onFlipV,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = if (editState.flipVertical) lavLight else lavLightTrasl,
                    contentColor   = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Filled.Flip, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(4.dp))
                Text("Flip vertical", fontFamily = robotoRegular, fontSize = 14.sp)
            }
            Button(
                onClick = onFlipH,
                colors  = ButtonDefaults.buttonColors(
                    containerColor = if (editState.flipHorizontal) lavLight else lavLightTrasl,
                    contentColor   = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Filled.Flip, null,
                    modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = 90f }
                )
                Spacer(Modifier.width(4.dp))
                Text("Flip horizontal", fontFamily = robotoRegular, fontSize = 14.sp)
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.End
        ) { ResetButton(onReset) }
    }
}


// ─── Helpers invariati ────────────────────────────────────────────────────────
private fun DrawScope.drawCropHandles(rect: Rect, r: Float) {
    listOf(
        Offset(rect.left,  rect.top),
        Offset(rect.right, rect.top),
        Offset(rect.left,  rect.bottom),
        Offset(rect.right, rect.bottom)
    ).forEach { c ->
        drawRect(Color.White,
            topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2f, r * 2f))
        drawRect(Color(0xFF1E88E5),
            topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2f, r * 2f),
            style = Stroke(width = 2.5.dp.toPx()))
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

private fun cropPreviewBitmap(bitmap: Bitmap?, cropRect: Rect): Bitmap? {
    bitmap ?: return null
    if (cropRect == Rect(0f, 0f, 1f, 1f)) return bitmap
    val x = (cropRect.left   * bitmap.width ).toInt().coerceIn(0, bitmap.width  - 1)
    val y = (cropRect.top    * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
    val w = ((cropRect.right  - cropRect.left) * bitmap.width ).toInt()
        .coerceAtLeast(1).coerceAtMost(bitmap.width  - x)
    val h = ((cropRect.bottom - cropRect.top ) * bitmap.height).toInt()
        .coerceAtLeast(1).coerceAtMost(bitmap.height - y)
    return Bitmap.createBitmap(bitmap, x, y, w, h)
}

@Composable
private fun ResetButton(onReset: () -> Unit) {
    OutlinedButton(
        onClick = onReset,
        shape   = RoundedCornerShape(20.dp),
        colors  = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Icon(painterResource(id = R.drawable.ic_reset), null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text("Reset", fontFamily = robotoRegular, fontSize = 14.sp)
    }
}

internal fun applyEdits(
    original: Bitmap?,
    cropRect: Rect,
    angleDeg: Float,
    flipH: Boolean,
    flipV: Boolean
): Bitmap? {
    original ?: return null
    var bmp = original.copy(Bitmap.Config.ARGB_8888, true)

    if (cropRect != Rect(0f, 0f, 1f, 1f)) {
        val x = (cropRect.left   * bmp.width ).toInt().coerceIn(0, bmp.width  - 1)
        val y = (cropRect.top    * bmp.height).toInt().coerceIn(0, bmp.height - 1)
        val w = ((cropRect.right  - cropRect.left) * bmp.width ).toInt()
            .coerceAtLeast(1).coerceAtMost(bmp.width  - x)
        val h = ((cropRect.bottom - cropRect.top ) * bmp.height).toInt()
            .coerceAtLeast(1).coerceAtMost(bmp.height - y)
        bmp = Bitmap.createBitmap(bmp, x, y, w, h)
    }

    if (angleDeg != 0f) {
        val srcW = bmp.width.toFloat()
        val srcH = bmp.height.toFloat()
        val rotMatrix = Matrix()
        rotMatrix.setRotate(angleDeg, srcW / 2f, srcH / 2f)
        val corners = floatArrayOf(0f, 0f, srcW, 0f, srcW, srcH, 0f, srcH)
        rotMatrix.mapPoints(corners)
        var minX = Float.MAX_VALUE;  var maxX = -Float.MAX_VALUE
        var minY = Float.MAX_VALUE;  var maxY = -Float.MAX_VALUE
        for (i in corners.indices step 2) {
            minX = minOf(minX, corners[i]);   maxX = maxOf(maxX, corners[i])
            minY = minOf(minY, corners[i+1]); maxY = maxOf(maxY, corners[i+1])
        }
        val newW = (maxX - minX).toInt().coerceAtLeast(1)
        val newH = (maxY - minY).toInt().coerceAtLeast(1)
        rotMatrix.postTranslate(-minX, -minY)
        val dst = Bitmap.createBitmap(newW, newH, Bitmap.Config.ARGB_8888)
        android.graphics.Canvas(dst).drawBitmap(bmp, rotMatrix, null)
        bmp = dst
    }

    if (flipH || flipV) {
        val m = Matrix().apply {
            postScale(
                if (flipH) -1f else 1f,
                if (flipV) -1f else 1f,
                bmp.width / 2f, bmp.height / 2f
            )
        }
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }

    return bmp
}

fun applyAllEdits(original: Bitmap?, state: EditState): Bitmap? =
    applyEdits(original, state.cropRect, state.angleDeg, state.flipHorizontal, state.flipVertical)

fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? = try {
    context.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it) }
} catch (e: Exception) { null }