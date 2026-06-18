package com.lk.langermap.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.lk.langermap.R
import com.lk.langermap.ui.theme.*
import java.io.File
import androidx.core.content.FileProvider
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput

@Preview
@Composable
fun OutputScreen(
    photoUri: String = "",
    onBack: () -> Unit = {},
    onNavigateToBackHome: () -> Unit = {}
) {
    val context = LocalContext.current
    var selectedFormat by remember { mutableStateOf("PNG") }
    val formats = listOf("PNG", "JPEG", "PDF")

    val black = colorResource(id = R.color.b)
    val teal  = colorResource(id = R.color.teal)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .pointerInput(Unit) { detectTapGestures { } },
        horizontalAlignment = Alignment.CenterHorizontally
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
                text = "Here's your result!",
                fontSize = 18.sp,
                fontFamily = robotoSemiBold,
                color = black,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
        }

        // ── Subtitle ─────────────────────────────────────────────
        Text(
            text = "Save or share the final image",
            fontSize = 13.sp,
            color = Color.Gray,
            modifier = Modifier
                .align(Alignment.Start)
                .padding(start = 8.dp, bottom = 12.dp)
                .padding(horizontal = 16.dp)
        )

        // ── Anteprima immagine ────────────────────────────────────
        if (photoUri.isNotEmpty()) {
            AsyncImage(
                model = Uri.parse(photoUri),
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 180.dp, max = 400.dp)
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(
                        width = 1.dp,
                        color = colorResource(id = R.color.lav_light),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp)
                    .background(colorResource(id = R.color.lav_light_trasl).copy(alpha = 0.2f))
                    .border(
                        width = 1.dp,
                        color = colorResource(id = R.color.lav_light),
                        shape = RoundedCornerShape(16.dp)
                    )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // ── Divider download options ──────────────────────────────
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
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

        // ── Selezione formato ─────────────────────────────────────
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

        Spacer(modifier = Modifier.weight(1f))

        // ── Save to gallery ───────────────────────────────────────
        Button(
            onClick = {
                saveToGallery(context, photoUri, selectedFormat)
                onNavigateToBackHome()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.teal_light),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_download_arrow),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Save to gallery",
                fontSize = 18.sp,
                fontFamily = robotoRegular,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // ── Share image ───────────────────────────────────────────
        Button(
            onClick = {
                shareImage(context, photoUri)
                onNavigateToBackHome()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .padding(horizontal = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.teal_light),
                contentColor = Color.Black
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_share),
                contentDescription = null,
                modifier = Modifier.size(20.dp),
                tint = Color.Black
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Share image",
                fontSize = 18.sp,
                fontFamily = robotoRegular,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(59.dp))
    }
}

// ── SAVE TO GALLERY ───────────────────────────────────────────────────────────
private fun saveToGallery(context: Context, photoUri: String, format: String) {
    val uri = Uri.parse(photoUri)
    val mimeType = when (format) {
        "JPEG" -> "image/jpeg"
        "PDF"  -> "application/pdf"
        else   -> "image/png"
    }
    val values = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, "LangerMap_${System.currentTimeMillis()}")
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
    }
    val resolver = context.contentResolver
    val destUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
    destUri?.let {
        resolver.openInputStream(uri)?.use { input ->
            resolver.openOutputStream(it)?.use { output ->
                input.copyTo(output)
            }
        }
    }
}

// ── SHARE IMAGE ───────────────────────────────────────────────────────────────
private fun shareImage(context: Context, photoUri: String) {
    try {
        val originalUri = Uri.parse(photoUri)
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
    } catch (e: Exception) {
        e.printStackTrace()
    }
}