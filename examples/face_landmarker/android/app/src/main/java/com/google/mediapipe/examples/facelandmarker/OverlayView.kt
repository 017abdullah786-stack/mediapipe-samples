
package com.google.mediapipe.examples.facelandmarker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.facelandmarker.FaceLandmarkerResult
import kotlin.math.max
import kotlin.math.min

class OverlayView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {

    private var results: FaceLandmarkerResult? = null
    private var imageWidth = 1
    private var imageHeight = 1
    private var scaleFactor = 1f

    private val mustachePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.rgb(48, 30, 25)
        style = Paint.Style.FILL
    }

    fun clear() {
        results = null
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        val faces = results?.faceLandmarks() ?: return
        if (faces.isEmpty()) return

        val scaledWidth = imageWidth * scaleFactor
        val scaledHeight = imageHeight * scaleFactor
        val offsetX = (width - scaledWidth) / 2f
        val offsetY = (height - scaledHeight) / 2f

        faces.forEach { landmarks ->
            drawMustache(canvas, landmarks, offsetX, offsetY)
        }
    }

    private fun drawMustache(
        canvas: Canvas,
        landmarks: List<NormalizedLandmark>,
        offsetX: Float,
        offsetY: Float
    ) {
        if (landmarks.size < 292) return

        fun x(index: Int): Float =
            landmarks[index].x() * imageWidth * scaleFactor + offsetX

        fun y(index: Int): Float =
            landmarks[index].y() * imageHeight * scaleFactor + offsetY

        // Mouth corners and the area beneath the nose
        val leftX = x(61)
        val rightX = x(291)
        val mouthTop = y(13)
        val noseBottom = y(2)

        val mouthWidth = rightX - leftX
        if (mouthWidth <= 0f) return

        val centerX = (leftX + rightX) / 2f
        val centerY = (noseBottom + mouthTop) / 2f
        val halfWidth = mouthWidth * 0.47f
        val height = mouthWidth * 0.14f

        // Two soft-edged sections make a simple moustache.
        canvas.drawOval(
            centerX - halfWidth,
            centerY - height * 0.25f,
            centerX,
            centerY + height * 0.65f,
            mustachePaint
        )

        canvas.drawOval(
            centerX,
            centerY - height * 0.25f,
            centerX + halfWidth,
            centerY + height * 0.65f,
            mustachePaint
        )
    }

    fun setResults(
        faceLandmarkerResults: FaceLandmarkerResult,
        imageHeight: Int,
        imageWidth: Int,
        runningMode: RunningMode = RunningMode.IMAGE
    ) {
        results = faceLandmarkerResults
        this.imageHeight = imageHeight
        this.imageWidth = imageWidth

        if (imageWidth <= 0 || imageHeight <= 0) {
            scaleFactor = 1f
        } else {
            scaleFactor = when (runningMode) {
                RunningMode.IMAGE,
                RunningMode.VIDEO -> min(
                    width.toFloat() / imageWidth,
                    height.toFloat() / imageHeight
                )

                RunningMode.LIVE_STREAM -> max(
                    width.toFloat() / imageWidth,
                    height.toFloat() / imageHeight
                )
            }
        }

        invalidate()
    }
}
