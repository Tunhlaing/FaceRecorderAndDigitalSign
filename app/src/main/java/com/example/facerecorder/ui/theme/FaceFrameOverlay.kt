package com.example.facerecorder.ui.theme

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View

class FaceFrameOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {

    var frameColor: Int = Color.YELLOW
        set(value) {
            field = value
            invalidate() // Redraw the view when the color changes
        }

    // Paint object for drawing the frame
    private val paint = Paint().apply {
        color = frameColor
        style = Paint.Style.STROKE
        strokeWidth = 8f
    }

    // Define the bounds for the frame (you can adjust these values to customize the size)
    var rectBounds: RectF? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the oval frame if rectBounds is set
        if (rectBounds == null) {
            // Define a default oval frame in the center of the overlay
            val left = width * 0.2f
            val top = height * 0.2f
            val right = width * 0.8f
            val bottom = height * 0.8f
            rectBounds = RectF(left, top, right, bottom)
        }

        rectBounds?.let {
            paint.color = frameColor
            canvas.drawOval(it, paint)
        }
    }

    // Adjusted method to make it easier for the face to fit in the frame
    fun isFaceWithinFrame(faceBounds: RectF, marginPercentage: Float = 0.3f): Boolean {
        rectBounds?.let { frame ->
            // Add a margin around the frame to allow part of the face to be outside
            val marginX = frame.width() * marginPercentage
            val marginY = frame.height() * marginPercentage

            // Create a relaxed frame that is slightly larger
            val relaxedFrame = RectF(
                frame.left - marginX,
                frame.top - marginY,
                frame.right + marginX,
                frame.bottom + marginY
            )

            // Check if the relaxed frame contains the face bounds
            return relaxedFrame.contains(faceBounds)
        }
        return false
    }
}

