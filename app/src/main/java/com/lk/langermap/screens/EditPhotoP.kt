package com.lk.langermap.screens

import android.graphics.Bitmap
import android.graphics.Matrix
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material.icons.filled.RotateRight
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
import androidx.compose.ui.layout.onGloballyPositioned
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

    LaunchedEffect(photoUri) {
        if (photoUri != Uri.EMPTY) {
            withContext(Dispatchers.IO) {
                originalBitmap = loadBitmapFromUri(context, photoUri)
            }
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
        // ── Top bar ──────────────────────────────────────────────────────────
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

        // ── Titolo ───────────────────────────────────────────────────────────
        // FIX: padding(horizontal, bottom) non valido → usare start/end/bottom separati
        Text(
            text = "Edit patient's photo",
            fontSize = 28.sp,
            fontFamily = robotoSemiBold,
            color = black,
            modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 8.dp)
        )

        // ── Area immagine + tool attivo ──────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            when (activeTool) {
                EditTool.CROP -> CropTool(
                    bitmap        = originalBitmap,
                    editState     = editState,
                    onCropChanged = { editState = editState.copy(cropRect = it) },
                    onReset       = { editState = editState.copy(cropRect = Rect(0f, 0f, 1f, 1f)) }
                )
                EditTool.ROTATE -> RotateTool(
                    bitmap         = originalBitmap,
                    editState      = editState,
                    onAngleChanged = { editState = editState.copy(angleDeg = it) },
                    onReset        = { editState = editState.copy(angleDeg = 0f) },
                    lavLight       = lavLight,
                    lavLightTrasl  = lavLightTrasl
                )
                EditTool.MIRROR -> MirrorTool(
                    bitmap        = originalBitmap,
                    editState     = editState,
                    onFlipV       = { editState = editState.copy(flipVertical   = !editState.flipVertical) },
                    onFlipH       = { editState = editState.copy(flipHorizontal = !editState.flipHorizontal) },
                    onReset       = { editState = editState.copy(flipVertical = false, flipHorizontal = false) },
                    lavLight      = lavLight,
                    lavLightTrasl = lavLightTrasl
                )
            }
        }

        // ── Edit tools divider ───────────────────────────────────────────────
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

        // ── Tab bottoni tool ─────────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            ToolTabButton(
                label         = "Crop",
                icon          = { Icon(Icons.Filled.Crop, null, modifier = Modifier.size(18.dp)) },
                selected      = activeTool == EditTool.CROP,
                activeColor   = lavLight,
                inactiveColor = lavLightTrasl,
                textColor     = black
            ) { activeTool = EditTool.CROP }

            ToolTabButton(
                label         = "Rotate",
                icon          = { Icon(Icons.Filled.RotateRight, null, modifier = Modifier.size(18.dp)) },
                selected      = activeTool == EditTool.ROTATE,
                activeColor   = lavLight,
                inactiveColor = lavLightTrasl,
                textColor     = black
            ) { activeTool = EditTool.ROTATE }

            ToolTabButton(
                label         = "Mirror",
                icon          = { Icon(Icons.Filled.Flip, null, modifier = Modifier.size(18.dp)) },
                selected      = activeTool == EditTool.MIRROR,
                activeColor   = lavLight,
                inactiveColor = lavLightTrasl,
                textColor     = black
            ) { activeTool = EditTool.MIRROR }
        }

        // ── Apply button ─────────────────────────────────────────────────────
        Button(
            onClick = { onApply(applyAllEdits(originalBitmap, editState)) },
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

// ─── Tab button helper ────────────────────────────────────────────────────────
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
        modifier = Modifier
            .weight(1f)
            .height(44.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = if (selected) activeColor else inactiveColor,
            contentColor   = textColor
        ),
        shape = RoundedCornerShape(12.dp),
        contentPadding = PaddingValues(horizontal = 8.dp)
    ) {
        icon()
        Spacer(modifier = Modifier.width(4.dp))
        Text(label, fontSize = 14.sp, fontFamily = robotoRegular)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// CROP TOOL
// ═══════════════════════════════════════════════════════════════════════════════
@Composable
private fun CropTool(
    bitmap: Bitmap?,
    editState: EditState,
    onCropChanged: (Rect) -> Unit,
    onReset: () -> Unit
) {
    var imageSize      by remember { mutableStateOf(IntSize.Zero) }
    var cropPx         by remember { mutableStateOf<Rect?>(null) }
    var draggingHandle by remember { mutableStateOf<String?>(null) }
    val handleRadius   = 24f

    LaunchedEffect(imageSize) {
        if (imageSize != IntSize.Zero) {
            val w = imageSize.width.toFloat()
            val h = imageSize.height.toFloat()
            cropPx = Rect(
                left   = editState.cropRect.left   * w,
                top    = editState.cropRect.top    * h,
                right  = editState.cropRect.right  * w,
                bottom = editState.cropRect.bottom * h
            )
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .onGloballyPositioned { coords -> imageSize = coords.size }
                .pointerInput(imageSize) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            val rect = cropPx ?: return@detectDragGestures
                            draggingHandle = hitTestHandle(offset, rect, handleRadius)
                                ?: if (rect.contains(offset)) "body" else null
                        },
                        onDrag = { change, dragAmount ->
                            change.consume()
                            val currentRect = cropPx ?: return@detectDragGestures
                            val w           = imageSize.width.toFloat()
                            val h           = imageSize.height.toFloat()
                            val dx          = dragAmount.x
                            val dy          = dragAmount.y
                            val minSize     = 60f

                            val newRect: Rect = when (draggingHandle) {
                                "tl" -> Rect(
                                    left   = (currentRect.left + dx).coerceIn(0f, currentRect.right - minSize),
                                    top    = (currentRect.top  + dy).coerceIn(0f, currentRect.bottom - minSize),
                                    right  = currentRect.right,
                                    bottom = currentRect.bottom
                                )
                                "tr" -> Rect(
                                    left   = currentRect.left,
                                    top    = (currentRect.top  + dy).coerceIn(0f, currentRect.bottom - minSize),
                                    right  = (currentRect.right + dx).coerceIn(currentRect.left + minSize, w),
                                    bottom = currentRect.bottom
                                )
                                "bl" -> Rect(
                                    left   = (currentRect.left + dx).coerceIn(0f, currentRect.right - minSize),
                                    top    = currentRect.top,
                                    right  = currentRect.right,
                                    bottom = (currentRect.bottom + dy).coerceIn(currentRect.top + minSize, h)
                                )
                                "br" -> Rect(
                                    left   = currentRect.left,
                                    top    = currentRect.top,
                                    right  = (currentRect.right  + dx).coerceIn(currentRect.left + minSize, w),
                                    bottom = (currentRect.bottom + dy).coerceIn(currentRect.top + minSize, h)
                                )
                                "body" -> {
                                    val newL = (currentRect.left + dx).coerceIn(0f, w - currentRect.width)
                                    val newT = (currentRect.top  + dy).coerceIn(0f, h - currentRect.height)
                                    Rect(newL, newT, newL + currentRect.width, newT + currentRect.height)
                                }
                                else -> currentRect
                            }
                            cropPx = newRect
                            onCropChanged(
                                Rect(
                                    left   = newRect.left   / w,
                                    top    = newRect.top    / h,
                                    right  = newRect.right  / w,
                                    bottom = newRect.bottom / h
                                )
                            )
                        },
                        onDragEnd = { draggingHandle = null }
                    )
                }
        ) {
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap.asImageBitmap(),
                    contentDescription = null,
                    contentScale = ContentScale.Fit,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Canvas(modifier = Modifier.fillMaxSize()) {
                val rect = cropPx ?: Rect(0f, 0f, size.width, size.height)

                val diff = Path().apply {
                    op(
                        Path().apply { addRect(Rect(0f, 0f, size.width, size.height)) },
                        Path().apply { addRect(rect) },
                        PathOperation.Difference
                    )
                }
                drawPath(diff, Color.Black.copy(alpha = 0.4f))

                drawRect(
                    color   = Color(0xFF2196F3),
                    topLeft = Offset(rect.left, rect.top),
                    size    = Size(rect.width, rect.height),
                    style   = Stroke(width = 2.dp.toPx())
                )

                drawCropHandles(rect, handleRadius * 0.55f)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, top = 4.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.End
        ) { ResetButton(onReset) }
    }
}

private fun DrawScope.drawCropHandles(rect: Rect, r: Float) {
    listOf(
        Offset(rect.left,  rect.top),
        Offset(rect.right, rect.top),
        Offset(rect.left,  rect.bottom),
        Offset(rect.right, rect.bottom)
    ).forEach { c ->
        drawRect(Color.Black, topLeft = Offset(c.x - r, c.y - r), size = Size(r * 2, r * 2))
    }
}

private fun hitTestHandle(offset: Offset, rect: Rect, radius: Float): String? =
    mapOf(
        "tl" to Offset(rect.left,  rect.top),
        "tr" to Offset(rect.right, rect.top),
        "bl" to Offset(rect.left,  rect.bottom),
        "br" to Offset(rect.right, rect.bottom)
    ).entries.firstOrNull { (_, c) -> (offset - c).getDistance() <= radius }?.key

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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
            text = "Angle ${editState.angleDeg.toInt()}°",
            fontSize = 14.sp,
            fontFamily = robotoRegular,
            color = Color.Black.copy(alpha = 0.6f),
            modifier = Modifier.padding(top = 8.dp)
        )

        Slider(
            value = editState.angleDeg,
            onValueChange = { onAngleChanged(it) },
            valueRange = -180f..180f,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            colors = SliderDefaults.colors(
                thumbColor         = lavLight,
                activeTrackColor   = lavLight,
                inactiveTrackColor = lavLightTrasl
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = 16.dp, bottom = 4.dp),
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
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
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
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = onFlipV,
                colors = ButtonDefaults.buttonColors(
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
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (editState.flipHorizontal) lavLight else lavLightTrasl,
                    contentColor   = Color.Black
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    Icons.Filled.Flip, null,
                    modifier = Modifier
                        .size(18.dp)
                        .graphicsLayer { rotationZ = 90f }
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

// ─── Reset button condiviso ───────────────────────────────────────────────────
@Composable
private fun ResetButton(onReset: () -> Unit) {
    OutlinedButton(
        onClick = onReset,
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_reset),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
        )
        Spacer(Modifier.width(4.dp))
        Text("Reset", fontFamily = robotoRegular, fontSize = 14.sp)
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// APPLY — produce Bitmap finale con crop → rotate → flip
// ═══════════════════════════════════════════════════════════════════════════════
fun applyAllEdits(original: Bitmap?, state: EditState): Bitmap? {
    original ?: return null
    var bmp = original.copy(Bitmap.Config.ARGB_8888, true)

    val cr = state.cropRect
    if (cr != Rect(0f, 0f, 1f, 1f)) {
        val x = (cr.left   * bmp.width).toInt().coerceIn(0, bmp.width - 1)
        val y = (cr.top    * bmp.height).toInt().coerceIn(0, bmp.height - 1)
        val w = ((cr.right  - cr.left) * bmp.width).toInt()
            .coerceAtLeast(1).coerceAtMost(bmp.width - x)
        val h = ((cr.bottom - cr.top)  * bmp.height).toInt()
            .coerceAtLeast(1).coerceAtMost(bmp.height - y)
        bmp = Bitmap.createBitmap(bmp, x, y, w, h)
    }

    if (state.angleDeg != 0f) {
        val m = Matrix().apply { postRotate(state.angleDeg) }
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }

    if (state.flipHorizontal || state.flipVertical) {
        val m = Matrix().apply {
            postScale(
                if (state.flipHorizontal) -1f else 1f,
                if (state.flipVertical)   -1f else 1f,
                bmp.width / 2f,
                bmp.height / 2f
            )
        }
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.width, bmp.height, m, true)
    }

    return bmp
}

// ─── Utility: carica Bitmap da URI ───────────────────────────────────────────
fun loadBitmapFromUri(context: android.content.Context, uri: Uri): Bitmap? = try {
    context.contentResolver.openInputStream(uri)?.use { stream ->
        android.graphics.BitmapFactory.decodeStream(stream)
    }
} catch (e: Exception) { null }