package com.example.gps_tracker

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class LineView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    private val linePaint = Paint()

    // List of points defining the line
    private var points: List<Pair<Float, Float>> = emptyList()

    init {
        linePaint.color = Color.BLACK
        linePaint.strokeWidth = 5f
    }

    // Set the points for the line
    fun setPoints(points: List<Pair<Float, Float>>) {
        this.points = points
        invalidate() // Redraw the view
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        for (i in 0 until points.size - 1) {
            val startPoint = points[i]
            val endPoint = points[i + 1]
            canvas.drawLine(
                startPoint.first,
                startPoint.second,
                endPoint.first,
                endPoint.second,
                linePaint
            )
        }
    }
}