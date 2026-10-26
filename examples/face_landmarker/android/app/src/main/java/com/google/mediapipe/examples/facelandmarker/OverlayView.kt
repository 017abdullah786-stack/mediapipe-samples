
package com.google.mediapipe.examples.facelandmarker

import android.content.Context
import android.util.AttributeSet
import android.view.View
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

    fun clear() {
        results = null
        invalidate()
    }

    override fun draw(canvas: android.graphics.Canvas) {
        // Draw only the normal View background; no moustache or landmarks.
        super.draw(canvas)
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
