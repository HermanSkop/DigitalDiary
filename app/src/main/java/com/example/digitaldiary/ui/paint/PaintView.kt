package com.example.digitaldiary.ui.paint

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PaintView(context: Context, attrs: AttributeSet) : View(context, attrs){
    private val paths = mutableListOf<Path>()
    private val paint = Paint().apply {
        style = Paint.Style.STROKE
        strokeWidth = 10f
        color = Color.CYAN
        strokeJoin = Paint.Join.ROUND
        strokeCap = Paint.Cap.ROUND
    }
    var bitmap: Bitmap? = null
        set(value) {
            field = value
            invalidate()
        }
    override fun onDraw(canvas: Canvas) {
        canvas.let {
            bitmap?.let { bitmap ->
                val rect = Rect(0, 0, canvas.width, canvas.height)
                it.drawBitmap(bitmap, null, rect, Paint())
            }
            paths.forEach { path ->
                it.drawPath(path, paint)
            }
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.action){
            MotionEvent.ACTION_DOWN -> {
                Path().apply {
                    moveTo(event.x, event.y)
                    lineTo(event.x, event.y)
                }.also {
                    paths.add(it)
                }
            }
            MotionEvent.ACTION_MOVE -> {
                paths.last().lineTo(event.x, event.y)
            }
        }
        invalidate()
        return true
    }

fun getDrawing(): Bitmap {
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    this.bitmap?.let {
        val rect = Rect(0, 0, width, height)
        canvas.drawBitmap(it, null, rect, null)
    }

    paths.forEach { path ->
        canvas.drawPath(path, paint)
    }

    return bitmap
}}