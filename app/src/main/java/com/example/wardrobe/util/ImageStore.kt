package com.example.wardrobe.util

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * Copy an external content:// image to the app's private directory and return a long-term accessible file:// Uri
 */
fun persistImageToAppStorage(context: Context, sourceUri: Uri): Uri {
    val cr = context.contentResolver

    // Guess the file extension
    val ext = when (cr.getType(sourceUri)) {
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        "image/jpeg" -> ".jpg"
        else -> ".jpg"
    }

    // Target file (app's private directory, no extra permissions needed, automatically cleaned up on uninstall)
    val imagesDir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
    val file = File(imagesDir, "${UUID.randomUUID()}$ext")

    cr.openInputStream(sourceUri).use { input ->
        FileOutputStream(file).use { output ->
            if (input != null) input.copyTo(output)
        }
    }
    return Uri.fromFile(file)
}