package com.example.wardrobe.util

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

/**
 * 将外部 content:// 图片复制到 app 私有目录，返回可长期访问的 file:// Uri
 */
fun persistImageToAppStorage(context: Context, sourceUri: Uri): Uri {
    val cr = context.contentResolver

    // 推测扩展名
    val ext = when (cr.getType(sourceUri)) {
        "image/png" -> ".png"
        "image/webp" -> ".webp"
        "image/jpeg" -> ".jpg"
        else -> ".jpg"
    }

    // 目标文件（app 私有目录，不需要额外权限，卸载时自动清理）
    val imagesDir = File(context.filesDir, "images").apply { if (!exists()) mkdirs() }
    val file = File(imagesDir, "${UUID.randomUUID()}$ext")

    cr.openInputStream(sourceUri).use { input ->
        FileOutputStream(file).use { output ->
            if (input != null) input.copyTo(output)
        }
    }
    return Uri.fromFile(file)
}