package com.example.facerecorder.ui.theme

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SignatureView(context: Context?, attrs: AttributeSet?) :
    View(context, attrs) {
    private var paint: Paint? = null
    private var path: Path? = null
    var signatureBitmap: Bitmap? = null
        private set
    private var canvas: Canvas? = null

    init {
        init()
    }

    private fun init() {
        paint = Paint()
        paint!!.color = Color.BLACK
        paint!!.isAntiAlias = true
        paint!!.strokeWidth = 5f
        paint!!.style = Paint.Style.STROKE
        path = Path()
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        signatureBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        canvas = Canvas(signatureBitmap!!)
        signatureBitmap!!.eraseColor(Color.WHITE)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawBitmap(signatureBitmap!!, 0f, 0f, null)
        canvas.drawPath(path!!, paint!!)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                path!!.moveTo(x, y)
                return true
            }

            MotionEvent.ACTION_MOVE -> path!!.lineTo(x, y)
            MotionEvent.ACTION_UP -> {
                canvas!!.drawPath(path!!, paint!!)
                path!!.reset()
            }
        }
        invalidate()
        return true
    }

    fun clear() {
        path!!.reset()
        signatureBitmap!!.eraseColor(Color.WHITE)
        invalidate()
    }
}