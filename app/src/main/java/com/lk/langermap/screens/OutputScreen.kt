package com.lk.langermap.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lk.langermap.R
import com.lk.langermap.ui.theme.*
import java.io.File
import androidx.core.content.FileProvider
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import androidx.core.net.toUri

@Preview
@Composable
fun OutputScreen(
    photoUri: String = "",
    onBack: () -> Unit = {},
    onNavigateToBackHome: () -> Unit = {}
) {
    val context = LocalContext.current
    val density = LocalDensity.current
    var selectedFormat by remember { mutableStateOf("PNG") }
    val formats = listOf("PNG", "JPEG", "PDF")

    val black = colorResource(id = R.color.b)

    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var photoIntrinsicSize by remember { mutableStateOf<IntSize?>(null) }

    val photoRect by remember(boxSize, photoIntrinsicSize) {
        derivedStateOf {
            val intrinsic = photoIntrinsicSize
            if (boxSize.width == 0 || boxSize.height == 0 || intrinsic == null) {
                null
            } else {
                val wBox = boxSize.width.toFloat()
                val hBox = boxSize.height.toFloat()
                val wBmp = intrinsic.width.toFloat()
                val hBmp = intrinsic.height.toFloat()

                val fitScale = minOf(wBox / wBmp, hBox / hBmp)
                val fittedW = wBmp * fitScale
                val fittedH = hBmp * fitScale
                val left = (wBox - fittedW) / 2f
                val top  = (hBox - fittedH) / 2f

                OutputPhotoRect(left, top, fittedW, fittedH)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .pointerInput(Unit) { detectTapGestures { } },
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        // ── HEADER ────────────────────────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {

            IconButton(
                onClick = onBack,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_back_arrow),
                    contentDescription = "Back",
                    tint = black
                )
            }

            Text(
                text = "Here's your result!",
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center,
                fontSize = 20.sp,
                fontFamily = robotoSemiBold,
                color = black
            )
        }

        // ── SUBTITLE ──────────────────────────────────────────────
        Row (
            modifier = Modifier
                .padding(bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text       = "Save or share the final image",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                fontSize   = 18.sp,
                fontFamily = robotoSemiBold,
                modifier   = Modifier
                    .fillMaxWidth()
            )
        }

        LaunchedEffect(photoUri) {
            photoIntrinsicSize = null
        }

        // ── IMAGE PREVIEW ───────────────────────────────────────────────
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .onSizeChanged { boxSize = it },
            contentAlignment = Alignment.Center
        ) {
            if (photoUri.isNotEmpty()) {
                AsyncImage(
                    model = photoUri.toUri(),
                    contentDescription = null,
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

                photoRect?.let { rect ->
                    val leftDp   = with(density) { rect.left.toDp() }
                    val topDp    = with(density) { rect.top.toDp() }
                    val widthDp  = with(density) { rect.width.toDp() }
                    val heightDp = with(density) { rect.height.toDp() }

                    Box(
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .offset(x = leftDp, y = topDp)
                            .size(widthDp, heightDp)
                            .border(
                                width = 1.dp,
                                color = colorResource(id = R.color.lav_light)
                            )
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(colorResource(id = R.color.lav_light_trasl).copy(alpha = 0.2f))
                        .border(
                            width = 1.dp,
                            color = colorResource(id = R.color.lav_light)
                        )
                )
            }
        }

        // ── DIVIDER ───────────────────────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = colorResource(id = R.color.lav_light))
            Text(
                text = "download options",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = colorResource(id = R.color.lav_light))
        }

        Spacer(modifier = Modifier.height(10.dp))

        // ── FORMAT ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            formats.forEach { format ->
                Button(
                    onClick = { selectedFormat = format },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedFormat == format)
                            colorResource(id = R.color.lav_light)
                        else
                            colorResource(id = R.color.lav_light_trasl)
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = format,
                        fontFamily = robotoRegular,
                        fontSize = 16.sp,
                        color = colorResource(id = R.color.b)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── SAVE AND SHARE ───────────────────────────────────────────────
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = {
                    if (selectedFormat == "PDF") {
                        savePdfToFile(context, photoUri)
                    } else {
                        saveToGallery(context, photoUri, selectedFormat)
                    }
                    onNavigateToBackHome()
                },
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                colors = nextButtonColors(),
                shape = nextButtonShape
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_download_arrow),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (selectedFormat == "PDF") "Save to file" else "Save to gallery",
                    fontSize = 15.sp,
                    fontFamily = robotoRegular,
                    color = Color.Black
                )
            }

            Button(
                onClick = {
                    shareImage(context, photoUri, onShared = { onNavigateToBackHome() })
                },
                modifier = Modifier
                    .width(130.dp)
                    .fillMaxHeight(),
                colors = nextButtonColors(),
                shape = nextButtonShape
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_share),
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                    tint = Color.Black
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Share",
                    fontSize = 15.sp,
                    fontFamily = robotoRegular,
                    color = Color.Black
                )
            }
        }

        Spacer(modifier = Modifier.height(59.dp))
    }
}

private data class OutputPhotoRect(
    val left: Float,
    val top: Float,
    val width: Float,
    val height: Float
)

// ── SAVE PNG / JPEG TO GALLERY ────────────────────────────────────────────────
private fun saveToGallery(context: Context, photoUri: String, format: String) {
    val uri = photoUri.toUri()
    val inputStream = context.contentResolver.openInputStream(uri) ?: return
    val original = BitmapFactory.decodeStream(inputStream) ?: return

    val (mimeType, compressFormat, extension) = when (format) {
        "JPEG" -> Triple("image/jpeg", Bitmap.CompressFormat.JPEG, "jpg")
        else   -> Triple("image/png",  Bitmap.CompressFormat.PNG,  "png")
    }

    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "LangerMap_${System.currentTimeMillis()}.$extension")
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    val resolver = context.contentResolver
    val destUri  = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return

    resolver.openOutputStream(destUri)?.use { out ->
        original.compress(compressFormat, 95, out)
    }

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
        values.clear()
        values.put(MediaStore.Images.Media.IS_PENDING, 0)
        resolver.update(destUri, values, null, null)
    }
}

// ── SAVE PDF TO FILE ─────────────────────────────
private fun savePdfToFile(context: Context, photoUri: String) {
    try {
        val uri = photoUri.toUri()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return
        val bitmap = BitmapFactory.decodeStream(inputStream) ?: return

        val pdfDoc = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = pdfDoc.startPage(pageInfo)
        page.canvas.drawBitmap(bitmap, 0f, 0f, Paint())
        pdfDoc.finishPage(page)

        val tempFile = File(context.cacheDir, "LangerMap_${System.currentTimeMillis()}.pdf")
        tempFile.outputStream().use { pdfDoc.writeTo(it) }
        pdfDoc.close()

        val shareUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.provider",
            tempFile
        )

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "application/pdf"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(Intent.createChooser(intent, "Save PDF").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

// ── SHARE IMAGE ───────────────────────────────────────────────────────────────
private fun shareImage(context: Context, photoUri: String, onShared: () -> Unit) {
    try {
        val originalUri = photoUri.toUri()
        val shareUri: Uri = if (originalUri.scheme == "file") {
            val file = File(originalUri.path ?: return)
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
        } else {
            originalUri
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val chooser = Intent.createChooser(shareIntent, "Share image").apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        context.startActivity(chooser)
        onShared()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}