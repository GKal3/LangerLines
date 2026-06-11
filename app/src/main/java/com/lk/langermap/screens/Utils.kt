package com.lk.langermap.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import java.io.File


object BitmapUtils {

    fun saveBitmapToCache(bitmap: Bitmap?, context: Context): Uri {
        if (bitmap == null) return Uri.EMPTY
        return try {
            val file = File(
                context.cacheDir,
                "edited_photo_${System.currentTimeMillis()}.jpg"
            )
            file.outputStream().use { out ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
            }
            Uri.fromFile(file)
        } catch (e: Exception) {
            Uri.EMPTY
        }
    }


    fun loadBitmapFromUri(context: Context, uri: Uri): Bitmap? {
        if (uri == Uri.EMPTY) return null
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // API 28+: usa ImageDecoder (restituisce sempre un bitmap
                // con config SOFTWARE, compatibile con Canvas)
                val source = android.graphics.ImageDecoder.createSource(
                    context.contentResolver, uri
                )
                android.graphics.ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                    decoder.allocator = android.graphics.ImageDecoder.ALLOCATOR_SOFTWARE
                }
            } else {
                // API < 28: fallback con ContentResolver InputStream
                @Suppress("DEPRECATION")
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    android.graphics.BitmapFactory.decodeStream(stream)
                }
            }
        } catch (e: Exception) {
            null
        }
    }
}