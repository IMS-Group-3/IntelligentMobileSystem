package com.example.ims

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.GridView


class MapGridView : View {
    private var Width: Int
    private var Height: Int

    constructor(context: Context, mapWidth: Int, mapHeight: Int) : super(context) {
        this.Width = mapWidth
        this.Height = mapHeight
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        this.Width = 100
        this.Height = 150
    }


    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private var gridWidth = 0
    private var gridHeight = 0
    private val markers = mutableListOf<GridMarker>()

    fun setGridSize(width: Int, height: Int) {
        gridWidth = Width
        gridHeight = Height
        invalidate()
    }

    fun addMarker(x: Int, y: Int, color: Int) {
        markers.add(GridMarker(x, y, color))
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        canvas?.let {
            // Calculate the size of each cell
            val cellWidth = width.toFloat() / gridWidth
            val cellHeight = height.toFloat() / gridHeight

            // Draw the horizontal lines
            for (y in 0 until gridHeight + 1) {
                val startY = y * cellHeight
                val endY = startY
                it.drawLine(0f, startY, width.toFloat(), endY, paint)
            }

            // Draw the vertical lines
            for (x in 0 until gridWidth + 1) {
                val startX = x * cellWidth
                val endX = startX
                it.drawLine(startX, 0f, endX, height.toFloat(), paint)
            }

            // Draw markers
            val markerPaint = Paint().apply {
                style = Paint.Style.FILL
            }
            val markerRadius = 10f // You can adjust this value as needed

            markers.forEach { marker ->
                markerPaint.color = marker.color
                val markerX = marker.x * cellWidth + cellWidth / 2
                val markerY = marker.y * cellHeight + cellHeight / 2
                it.drawCircle(markerX, markerY, markerRadius, markerPaint)
            }

        }
    }
}
