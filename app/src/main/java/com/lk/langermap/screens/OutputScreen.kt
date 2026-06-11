package com.lk.langermap.screens

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .statusBarsPadding()
            .padding(horizontal = 16.dp)
        // ✅ FIX: blocca i touch events dal passare alla schermata sottostante
        .pointerInput(Unit) { detectTapGestures { } },
    horizontalAlignment = Alignment.CenterHorizontally
) { 

        // Top bar
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

        // Titolo
        Text(
            text = "Here's your result !",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "Save or share the final image",
            fontSize = 14.sp,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp, bottom = 16.dp)
        )

        // Anteprima immagine
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .clip(RoundedCornerShape(16.dp))
                .border(
                    width = 1.dp,
                    color = colorResource(id = R.color.lav_light),
                    shape = RoundedCornerShape(16.dp)
                )
                .background(colorResource(id = R.color.lav_light_trasl).copy(alpha = 0.2f))
        ) {
            if (photoUri.isNotEmpty()) {
                AsyncImage(
                    model = Uri.parse(photoUri),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Divider download options
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = colorResource(id = R.color.lav_light))
            Text(
                text = "Download options",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = colorResource(id = R.color.lav_light))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Selezione formato
        Row(
            modifier = Modifier.fillMaxWidth(),
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

        // Save to gallery
        Button(
            onClick = {
                saveToGallery(context, photoUri, selectedFormat)
                onNavigateToBackHome()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.teal_light)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_download_arrow),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
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

        // Share image
        Button(
            onClick = {
                shareImage(context, photoUri)
                onNavigateToBackHome()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(id = R.color.teal_light)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_share),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Share image",
                fontSize = 18.sp,
                fontFamily = robotoRegular,
                color = Color.Black
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun saveToGallery(context: Context, photoUri: String, format: String) {
    // salva l'immagine nella galleria
    val uri = Uri.parse(photoUri)
    val mimeType = when (format) {
        "JPEG" -> "image/jpeg"
        "PDF" -> "application/pdf"
        else -> "image/png"
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
//
// FIX 1 — FileProvider invece di Uri.fromFile / file://
//   I file in cache (file://) non sono leggibili dalle altre app per motivi di
//   sicurezza (FileUriExposedException su API 24+). FileProvider genera un URI
//   content:// con permesso di lettura temporaneo riconosciuto dal sistema.
//
// FIX 2 — FLAG_ACTIVITY_NEW_TASK
//   startActivity() chiamato da un Context non-Activity richiede questo flag,
//   altrimenti il sistema lancia un'eccezione silenziosa e il chooser non appare.
//
// FIX 3 — chooser creato con Intent.createChooser + FLAG_ACTIVITY_NEW_TASK
//   Il chooser stesso è un'Activity separata; deve ereditare il flag.
//
private fun shareImage(context: Context, photoUri: String) {
    try {
        val originalUri = Uri.parse(photoUri)

        // Converti file:// → content:// tramite FileProvider
        // (se è già un content:// lo usiamo direttamente)
        val shareUri: Uri = if (originalUri.scheme == "file") {
            val file = File(originalUri.path ?: return)
            FileProvider.getUriForFile(
                context,
                // Deve corrispondere all'authority dichiarata nel Manifest:
                // <provider android:authorities="${applicationId}.provider" .../>
                "${context.packageName}.provider",
                file
            )
        } else {
            // content:// già compatibile
            originalUri
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_STREAM, shareUri)
            // Permesso di lettura esplicito per le app che ricevono l'intent
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            // Necessario quando startActivity() viene chiamato fuori da un'Activity
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