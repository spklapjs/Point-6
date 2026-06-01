package com.spklapjs.point_6.data.inference

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import com.example.point_6.domain.model.SPenSensorWindow

class SPenInferenceEngine(context: Context) {
    private var interpreter: Interpreter
    private var modelBuffer: MappedByteBuffer

    init {
        modelBuffer = loadModelFile(context, "spen_model.tflite")
        interpreter = Interpreter(modelBuffer)
    }

    private fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
        val fileDescriptor = context.assets.openFd(modelName)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
    }

    @Synchronized
    fun predict(window: SPenSensorWindow): Int {
        val input = Array(1) { Array(40) { FloatArray(2) } }
        var index = 0
        for (i in 0 until 40) {
            input[0][i][0] = window.spenDelta[index++]
            input[0][i][1] = window.spenDelta[index++]
        }

        val output = Array(1) { FloatArray(6) }

        interpreter.run(input, output)

        var maxIndex = 0
        var maxProb = output[0][0]
        for (i in 1 until 6) {
            if (output[0][i] > maxProb) {
                maxProb = output[0][i]
                maxIndex = i
            }
        }
        return maxIndex
    }

    fun close() {
        interpreter.close()
    }
}