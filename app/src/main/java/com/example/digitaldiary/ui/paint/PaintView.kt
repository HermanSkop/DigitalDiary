package com.example.digitaldiary.ui.paint

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
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
    override fun onDraw(canvas: Canvas) {
        canvas.let {
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
}