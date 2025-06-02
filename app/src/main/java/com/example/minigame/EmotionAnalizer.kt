package com.example.minigame

import android.content.Context
import android.graphics.*
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

object EmotionAnalyzer {
    private lateinit var interpreter: Interpreter
    private val labels = listOf("angry", "happy", "neutral", "sad")

    fun init(context: Context) {
        val assetFileDescriptor = context.assets.openFd("emotion_model.tflite")
        val fileInputStream = assetFileDescriptor.createInputStream()
        val fileChannel = fileInputStream.channel
        val startOffset = assetFileDescriptor.startOffset
        val declaredLength = assetFileDescriptor.declaredLength
        val modelBuffer = fileChannel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        interpreter = Interpreter(modelBuffer)
    }

    fun predictEmotion(bitmap: Bitmap): String {
        val input = preprocess(bitmap)
        val output = Array(1) { FloatArray(labels.size) }
        interpreter.run(input, output)
        val index = output[0].indices.maxByOrNull { output[0][it] } ?: 0
        return labels[index]
    }

    private fun preprocess(bitmap: Bitmap): ByteBuffer {
        val resized = Bitmap.createScaledBitmap(bitmap, 48, 48, true)
        val grayBitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(grayBitmap)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        canvas.drawBitmap(resized, 0f, 0f, paint)

        val buffer = ByteBuffer.allocateDirect(1 * 48 * 48 * 1 * 4)
        buffer.order(ByteOrder.nativeOrder())

        for (y in 0 until 48) {
            for (x in 0 until 48) {
                val pixel = grayBitmap.getPixel(x, y)
                val normalized = Color.red(pixel) / 255.0f
                buffer.putFloat(normalized)
            }
        }
        return buffer
    }
}
