package com.example.ims

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.GridView
import android.widget.Toast
import kotlin.math.sqrt


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

    // Paint val for the grid
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

    fun addMarkers(markers: List<GridMarker>) {
        this.markers.addAll(markers)
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

            // markers and lines paint style
            val markerPaint = Paint().apply {
                style = Paint.Style.FILL
            }
            val linePaint = Paint().apply {
                style = Paint.Style.STROKE
            }
            val markerRadius = 5f

            markers.forEachIndexed { index, marker ->
                // Draw marker
                markerPaint.color = marker.color
                val markerX = marker.x * cellWidth + cellWidth / 2
                val markerY = marker.y * cellHeight + cellHeight / 2
                it.drawCircle(markerX, markerY, markerRadius, markerPaint)

                // Draw line between markers
                if (index > 0) {
                    val prevMarker = markers[index - 1]
                    val prevMarkerX = prevMarker.x * cellWidth + cellWidth / 2
                    val prevMarkerY = prevMarker.y * cellHeight + cellHeight / 2

                    linePaint.color = marker.color
                    linePaint.strokeWidth = markerRadius * 2
                    it.drawLine(prevMarkerX, prevMarkerY, markerX, markerY, linePaint)
                }
            }

            // Adding collision event markers to the grid
            markers.forEachIndexed { index, marker ->
                val markerX = marker.x * cellWidth + cellWidth / 2
                val markerY = marker.y * cellHeight + cellHeight / 2

                // Draw blue circle on top of red marker when collisionEvent is true
                if (marker.collisionEvent) {
                    markerPaint.color = Color.BLUE
                    val innerMarkerRadius = markerRadius * 4
                    it.drawCircle(markerX, markerY, innerMarkerRadius, markerPaint)
                }
            }

        }
    }

    // Checking if the click is inside the marker radius
    private fun isTouchInsideMarker(touchX: Float, touchY: Float, markerX: Float, markerY: Float, markerRadius: Float): Boolean {
        val dx = touchX - markerX
        val dy = touchY - markerY
        val distance = sqrt(dx * dx + dy * dy)
        return distance <= markerRadius
    }
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            val touchX = event.x
            val touchY = event.y

            // Calculate the size of each cell
            val cellWidth = width.toFloat() / gridWidth
            val cellHeight = height.toFloat() / gridHeight

            // Loop through the markers and check if the touch event is inside the marker
            markers.forEach { marker ->
                if (marker.collisionEvent) {
                    val markerX = marker.x * cellWidth + cellWidth / 2
                    val markerY = marker.y * cellHeight + cellHeight / 2
                    val markerRadius = 5f * 4  // 5f is the base markerRadius and 4 is the scaling factor

                    if (isTouchInsideMarker(touchX, touchY, markerX, markerY, markerRadius)) {
                        // Handle the click event for the blue marker
                        Toast.makeText(context, "Blue marker at (${marker.x}, ${marker.y}) clicked", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        return super.onTouchEvent(event)
    }

}
