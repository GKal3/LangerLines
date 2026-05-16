package com.lk.langermap.screens

import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import java.io.File

/**
 * Salva un Bitmap nella cache dell'app e restituisce il suo URI.
 * Usato da nav.kt dopo onApply per passare l'immagine editata all'OverlayScreen.
 *
 * @param bitmap  il Bitmap da salvare (può essere null → restituisce Uri.EMPTY)
 * @param context il Context dell'Activity/Application
 */
fun saveBitmapToCache(bitmap: Bitmap?, context: Context): Uri {
    if (bitmap == null) return Uri.EMPTY
    return try {
        val file = File(context.cacheDir, "edited_photo_${System.currentTimeMillis()}.jpg")
        file.outputStream().use { out ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, out)
        }
        Uri.fromFile(file)
    } catch (e: Exception) {
        Uri.EMPTY
    }
}