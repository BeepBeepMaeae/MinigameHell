package com.example.minigame

import android.content.Context
import android.graphics.*
import android.os.Build
import androidx.annotation.RequiresApi
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder

object EmotionAnalyzer {
    private var interpreter: Interpreter? = null
    private val labels = listOf("angry", "happy", "neutral", "sad")

    @RequiresApi(Build.VERSION_CODES.CUPCAKE)
    fun init(context: Context) {
        if (interpreter != null) return  // 이미 초기화되어 있으면 무시
        val fd = context.assets.openFd("emotion_model.tflite")
        val inputStream = fd.createInputStream()
        val fileChannel = inputStream.channel
        val startOffset = fd.startOffset
        val declaredLength = fd.declaredLength
        val buffer = fileChannel.map(java.nio.channels.FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
        interpreter = Interpreter(buffer)
    }

    fun isInitialized(): Boolean = interpreter != null

    fun predict(bitmap: Bitmap): String {
        val localInterpreter = interpreter
        requireNotNull(localInterpreter) { "EmotionAnalyzer must be initialized by calling init(context) before use." }

        val resized = Bitmap.createScaledBitmap(bitmap, 48, 48, true)
        val gray = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(gray)
        val paint = Paint().apply {
            colorFilter = ColorMatrixColorFilter(ColorMatrix().apply { setSaturation(0f) })
        }
        canvas.drawBitmap(resized, 0f, 0f, paint)

        val buffer = ByteBuffer.allocateDirect(1 * 48 * 48 * 1 * 4).order(ByteOrder.nativeOrder())
        for (y in 0 until 48) {
            for (x in 0 until 48) {
                val pixel = gray.getPixel(x, y)
                buffer.putFloat(Color.red(pixel) / 255.0f)
            }
        }

        val output = Array(1) { FloatArray(labels.size) }
        localInterpreter.run(buffer, output)
        val index = output[0].indices.maxByOrNull { output[0][it] } ?: 0
        return labels[index]
    }
}
