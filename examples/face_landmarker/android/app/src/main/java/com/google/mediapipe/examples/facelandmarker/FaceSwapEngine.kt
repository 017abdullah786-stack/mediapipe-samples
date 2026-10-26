
package com.google.mediapipe.examples.facelandmarker

import android.content.Context
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import java.io.File

/**
 * LiveFaceFX Stage 3
 *
 * Loads and inspects the face-swap ONNX model.
 * Camera-frame processing and face blending will be connected next.
 */
class FaceSwapEngine(context: Context) : AutoCloseable {

    private val environment = OrtEnvironment.getEnvironment()
    private val modelDirectory =
        File(context.filesDir, "livefacefx/models")

    private var swapSession: OrtSession? = null

    data class ModelInfo(
        val inputNames: List<String>,
        val outputNames: List<String>
    )

    fun loadSwapModel(): ModelInfo {
        val modelFile = File(modelDirectory, "inswapper_128.onnx")

        check(modelFile.isFile && modelFile.length() > 0L) {
            "Face-swap model is missing: ${modelFile.absolutePath}"
        }

        val options = OrtSession.SessionOptions()

        try {
            val newSession = environment.createSession(
                modelFile.absolutePath,
                options
            )

            if (newSession.inputInfo.isEmpty() ||
                newSession.outputInfo.isEmpty()
            ) {
                newSession.close()
                error("The ONNX model has no usable inputs or outputs.")
            }

            swapSession?.close()
            swapSession = newSession

            return ModelInfo(
                inputNames = newSession.inputInfo.keys.toList(),
                outputNames = newSession.outputInfo.keys.toList()
            )
        } finally {
            options.close()
        }
    }

    fun isModelLoaded(): Boolean = swapSession != null

    override fun close() {
        swapSession?.close()
        swapSession = null
    }
}
