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

// ─── Tool enum ────────────────────────────────────────────────────────────────
enum class EditTool { CROP, ROTATE, MIRROR }

// ─── Stato globale accumulato tra i tool ──────────────────────────────────────
data class EditState(
    val cropRect: Rect = Rect(0f, 0f, 1f, 1f),
    val angleDeg: Float = 0f,
    val flipVertical: Boolean = false,
    val flipHorizontal: Boolean = false
)

// ─── Screen principale ────────────────────────────────────────────────────────
@Preview(showBackground = true)
@Composable
fun EditPhotoScreen(
    photoUri: Uri = Uri.EMPTY,
    onBack: () -> Unit = {},
    onApply: (Bitmap?) -> Unit = {}
) {
    val context = LocalContext.current

    var editState      by remember { mutableStateOf(EditState()) }
    var activeTool     by remember { mutableStateOf(EditTool.CROP) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }

    // Carica bitmap originale
    LaunchedEffect(photoUri) {
        if (photoUri != Uri.EMPTY) {
            withContext(Dispatchers.IO) {
                originalBitmap = loadBitmapFromUri(context, photoUri)
            }
        }
    }

    // Bitmap che accumula le modifiche confermate
    var accumulatedBitmap by remember { mutableStateOf<Bitmap?>(null) }

    LaunchedEffect(originalBitmap) {
        if (originalBitmap != null && accumulatedBitmap == null) {
            accumulatedBitmap = originalBitmap
        }
    }

    // 2. Quando cambi tool, applica le modifiche correnti all'accumulato
    LaunchedEffect(activeTool) {
        val src = accumulatedBitmap ?: return@LaunchedEffect
        withContext(Dispatchers.IO) {
            accumulatedBitmap = applyAllEdits(src, editState)
            editState = EditState() // resetta lo stato per il nuovo tool
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
        // Top bar
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

        // Area tool
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (activeTool) {
                EditTool.CROP -> CropTool(
                    bitmap        = accumulatedBitmap,
                    editState     = editState,
                    onCropChanged = { editState = editState.copy(cropRect = it) },
                    onReset       = { editState = editState.copy(cropRect = Rect(0f, 0f, 1f, 1f)) }
                )
                EditTool.ROTATE -> RotateTool(
                    bitmap         = accumulatedBitmap,
                    editState      = editState,
                    onAngleChanged = { editState = editState.copy(angleDeg = it) },
                    onReset        = { editState = editState.copy(angleDeg = 0f) },
                    lavLight       = lavLight,
                    lavLightTrasl  = lavLightTrasl
                )
                EditTool.MIRROR -> MirrorTool(
                    bitmap        = accumulatedBitmap,
                    editState     = editState,
                    onFlipV       = { editState = editState.copy(flipVertical   = !editState.flipVertical) },
                    onFlipH       = { editState = editState.copy(flipHorizontal = !editState.flipHorizontal) },
                    onReset       = { editState = editState.copy(flipVertical = false, flipHorizontal = false) },
                    lavLight      = lavLight,
                    lavLightTrasl = lavLightTrasl
                )
            }
        }

        // Divider "Edit tools"
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

        // Tab tool
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolTabButton(
                label = "Crop",
                icon  = { Icon(Icons.Filled.Crop, null, modifier = Modifier.size(18.dp)) },
                selected = activeTool == EditTool.CROP,
                activeColor = lavLight, inactiveColor = lavLightTrasl, textColor = black
            ) { activeTool = EditTool.CROP }

            ToolTabButton(
                label = "Rotate",
                icon  = { Icon(Icons.AutoMirrored.Filled.RotateRight, null, modifier = Modifier.size(18.dp)) },
                selected = activeTool == EditTool.ROTATE,
                activeColor = lavLight, inactiveColor = lavLightTrasl, textColor = black
            ) { activeTool = EditTool.ROTATE }

            ToolTabButton(
                label = "Mirror",
                icon  = { Icon(Icons.Filled.Flip, null, modifier = Modifier.size(18.dp)) },
                selected = activeTool == EditTool.MIRROR,
                activeColor = lavLight, inactiveColor = lavLightTrasl, textColor = black
            ) { activeTool = EditTool.MIRROR }
        }

        // Apply
        Button(
            onClick = {
                onApply(applyAllEdits(accumulatedBitmap, editState))
            },
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(bottom = 24.dp)
                .width(160.dp)
                .height(48.dp),
            colors = ButtonDefaults.buttonColors(containerColor = grey, contentColor = black),
            shape  = RoundedCornerShape(24.dp)
        ) {
            Text("Apply", fontSize = 20.sp, fontFamily = robotoRegular)
        }
    }
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
        onClick = onClick,
        modifier = Modifier.weight(1f).height(44.dp),
        colors   = ButtonDefaults.buttonColors(
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
// CROP TOOL
// Fix: awaitEachGesture + awaitFirstDown + drag() al posto del loop manuale
// con awaitPointerEventScope + PointerEventPass.Initial che causava il blocco.
// drag(pointerId) traccia esattamente quel dito fino al rilascio e fornisce
// positionChange() già calcolato, eliminando il prevPos manuale.
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun CropTool(
    bitmap: Bitmap?,
    editState: EditState,
    onCropChanged: (Rect) -> Unit,
    onReset: () -> Unit
) {
    var boxSize      by remember { mutableStateOf(IntSize.Zero) }
    val cropPx        = remember { mutableStateOf<Rect?>(null) }
    var activeHandle by remember { mutableStateOf<String?>(null) }
    val handlePx     = 40f   // raggio touch handle in px

    // Inizializza cropPx quando boxSize è disponibile e cropPx è null
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
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                // Ricalcola cropPx solo se le dimensioni cambiano davvero
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
                // ── FIX PRINCIPALE ────────────────────────────────────────────
                // awaitEachGesture crea una coroutine fresca per ogni gesto,
                // evitando loop annidati che perdevano/duplicavano gli eventi.
                // drag(pointerId) traccia esattamente il dito che ha fatto down
                // e fornisce positionChange() → niente più prevPos manuale.
                // ─────────────────────────────────────────────────────────────
                .pointerInput(Unit) {
                    awaitEachGesture {
                        // Aspetta il touch-down
                        val down = awaitFirstDown(requireUnconsumed = false)

                        val rect = cropPx.value ?: return@awaitEachGesture

                        // Determina quale handle (o body) è stato toccato
                        activeHandle = hitTestHandle(down.position, rect, handlePx)
                            ?: if (rect.contains(down.position)) "body" else null

                        // Se il dito è fuori dal rect e fuori dagli handle → ignora
                        if (activeHandle == null) return@awaitEachGesture
                        down.consume()

                        // Segui il dito finché non viene sollevato
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

                        // Pulizia al rilascio del dito
                        activeHandle = null
                    }
                }
        ) {
            // Immagine
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap             = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale       = ContentScale.Fit,
                    modifier           = Modifier.fillMaxSize()
                )
            }

            // Overlay crop — Canvas figlio, NON intercetta eventi
            Canvas(modifier = Modifier.fillMaxSize()) {
                val rect = cropPx.value ?: Rect(0f, 0f, size.width, size.height)

                // Scurisci esterno
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

                // Bordo bianco
                drawRect(
                    color   = Color.White,
                    topLeft = Offset(rect.left, rect.top),
                    size    = Size(rect.width, rect.height),
                    style   = Stroke(width = 2.dp.toPx())
                )

                // Griglia 3×3
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.End
        ) {
            ResetButton {
                onReset()
                cropPx.value = null   // forza ricalcolo nel prossimo frame
            }
        }
    }
}

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

// ═══════════════════════════════════════════════════════════════════════════════
// ROTATE TOOL
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun RotateTool(
    bitmap: Bitmap?,
    editState: EditState,
    onAngleChanged: (Float) -> Unit,
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
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer { rotationZ = editState.angleDeg }
                )
            }
        }
        Text(
            "Angle ${editState.angleDeg.toInt()}°",
            fontSize = 14.sp, fontFamily = robotoRegular,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )
        Slider(
            value = editState.angleDeg,
            onValueChange = onAngleChanged,
            valueRange = -180f..180f,
            modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
            colors = SliderDefaults.colors(
                thumbColor = lavLight,
                activeTrackColor = lavLight,
                inactiveTrackColor = lavLightTrasl
            )
        )
        Row(
            modifier = Modifier.fillMaxWidth().padding(end = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.End
        ) { ResetButton(onReset) }
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// MIRROR TOOL
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
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier
                        .fillMaxSize()
                        .graphicsLayer {
                            scaleX = if (editState.flipHorizontal) -1f else 1f
                            scaleY = if (editState.flipVertical)   -1f else 1f
                        }
                )
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
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
                Icon(Icons.Filled.Flip, null,
                    modifier = Modifier.size(18.dp).graphicsLayer { rotationZ = 90f })
                Spacer(Modifier.width(4.dp))
                Text("Flip horizontal", fontFamily = robotoRegular, fontSize = 14.sp)
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(end = 16.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.End
        ) { ResetButton(onReset) }
    }
}

// ─── Reset button ─────────────────────────────────────────────────────────────
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

// ═══════════════════════════════════════════════════════════════════════════════
// BITMAP PROCESSING
// ═══════════════════════════════════════════════════════════════════════════════
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
        val m = Matrix().apply { postRotate(angleDeg) }
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
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