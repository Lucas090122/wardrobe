package com.example.wardrobe.clothingClassification

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.io.FileInputStream
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.widget.Toast
import com.example.wardrobe.R
import java.io.File
import java.io.FileOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder

/*class TFLiteHelper(context: Context, modelName: String) {

    var interpreter: Interpreter

    init {
        interpreter = Interpreter(loadModelFile(context, modelName))
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun close() {
        interpreter.close()
    }
}*/

fun loadModelFileFromRaw(context: Context, resourceId: Int): MappedByteBuffer {
    val inputStream = context.resources.openRawResource(resourceId)
    val tempFile = File.createTempFile("temp_model", ".tflite", context.cacheDir)
    tempFile.deleteOnExit()

    val outputStream = FileOutputStream(tempFile)
    inputStream.copyTo(outputStream)

    val fileInputStream = tempFile.inputStream()
    val fileChannel = fileInputStream.channel
    val mappedByteBuffer = fileChannel.map(FileChannel.MapMode.READ_ONLY, 0, fileChannel.size())
    fileChannel.close()
    return mappedByteBuffer
}

fun preprocessImage(bitmap: Bitmap): ByteBuffer {
    val imgData = ByteBuffer.allocateDirect(1 * 28 * 28 * 4) // float32 = 4 bytes
    imgData.order(ByteOrder.nativeOrder())

    val resized = Bitmap.createScaledBitmap(bitmap, 28, 28, true)

    for (y in 0 until 28) {
        for (x in 0 until 28) {
            val pixel = resized.getPixel(x, y)
            val r = Color.red(pixel)
            val g = Color.green(pixel)
            val b = Color.blue(pixel)
            val gray = (r + g + b) / 3.0f
            val normalized = gray / 255.0f
            imgData.putFloat(normalized)
        }
    }
    imgData.rewind()
    return imgData
}

fun predict(interpreter: Interpreter, input: ByteBuffer): Int {
    val output = Array(1) { FloatArray(10) } // 10 classes
    interpreter.run(input, output)

    // Find index with highest probability
    return output[0].indices.maxByOrNull { output[0][it] } ?: -1
}

val labels = arrayOf(
    "T-shirt/top", "Trouser", "Pullover", "Dress", "Coat",
    "Sandal", "Shirt", "Sneaker", "Bag", "Ankle boot"
)

fun uriToBitmap(context: Context, uri: Uri): Bitmap {
    return MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
}

fun classifyClothing(context: Context, imageUri: Uri) {
    val bitmap = uriToBitmap(context, imageUri)
    val input = preprocessImage(bitmap)

    val tflite = Interpreter(loadModelFileFromRaw(context, R.raw.fashion_mnist_model))
    val predictedIndex = predict(tflite, input)
    val predictedLabel = labels[predictedIndex]

    tflite.close()

    Toast.makeText(context, "Predicted: $predictedLabel", Toast.LENGTH_LONG).show()
}
