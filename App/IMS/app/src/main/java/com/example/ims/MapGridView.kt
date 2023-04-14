package com.example.ims

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.GridView


class MapGridView : View {
    private var mapWidth: Int
    private var mapHeight: Int

    constructor(context: Context, mapWidth: Int, mapHeight: Int) : super(context) {
        this.mapWidth = mapWidth
        this.mapHeight = mapHeight
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        // Initialize your view with default values or read the values from the attrs.
        this.mapWidth = 100
        this.mapHeight = 100
    }


    private val paint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private var gridWidth = 0
    private var gridHeight = 0

    fun setGridSize(width: Int, height: Int) {
        gridWidth = mapWidth
        gridHeight = mapHeight
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
        }
    }
}
