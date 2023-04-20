package com.example.ims

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import android.widget.Toast
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt


class MapGridView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private var Width: Int = 100
    private var Height: Int = 150
    private val markers = mutableListOf<GridMarker>()
    private val matrix = Matrix()
    private var scaleFactor = 1f
    private val scaleDetector: ScaleGestureDetector
    private val touchSlop: Int
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var offsetX = 0f
    private var offsetY = 0f
    private var cellWidth  = 0f
    private var cellHeight = 0f

    init {
        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop


            scaleDetector = ScaleGestureDetector(context, object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
            override fun onScale(detector: ScaleGestureDetector): Boolean {
                scaleFactor *= detector.scaleFactor
                scaleFactor = max(0.1f, min(scaleFactor, 5.0f))
                invalidate()
                return true
            }
        })
    }
    // initiallizes the width and height of the view.
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // Calculate cellWidth and cellHeight based on the view's width and height
        cellWidth = width.toFloat() / Width
        cellHeight = height.toFloat() / Height
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val markerX = width / 2f
        val markerY = height / 2f

        // Updates the matrix with the scale factor
        matrix.reset()
        matrix.postScale(scaleFactor, scaleFactor, markerX, markerY)

        // Centers the last marker on the map
    /*    if (markers.isNotEmpty()) {
            val marker = markers.last()
            val cellWidth = width.toFloat() / gridWidth
            val cellHeight = height.toFloat() / gridHeight

            val x = marker.x * cellWidth + cellWidth / 2
            val y = marker.y * cellHeight + cellHeight / 2
            matrix.postTranslate(width / 2f - x * scaleFactor, height / 2f - y * scaleFactor)
        }*/

        if (canvas != null) {
            canvas.save()
            canvas.concat(matrix)
        }

        canvas?.let {

            // Sets markers and lines paint style
            val markerPaint = Paint().apply {
                style = Paint.Style.FILL
            }
            val linePaint = Paint().apply {
                style = Paint.Style.STROKE
            }
            var markerRadius = 5f

            markers.forEachIndexed { index, marker ->

                val markerX = marker.x * cellWidth + cellWidth / 2
                val markerY = marker.y * cellHeight + cellHeight / 2
                // Draws markers
                if (index == markers.size - 1){
                    markerPaint.color = Color.BLACK
                    markerRadius = 15f
                    it.drawCircle(markerX + offsetX, markerY + offsetY, markerRadius, markerPaint)
                    markerRadius = 5f
                } else {
                    markerPaint.color = marker.color
                    it.drawCircle(markerX + offsetX, markerY + offsetY, markerRadius, markerPaint)
                }

                // Draws line between markers
                if (index > 0) {
                    val prevMarker = markers[index - 1]
                    val prevMarkerX = prevMarker.x * cellWidth + cellWidth / 2
                    val prevMarkerY = prevMarker.y * cellHeight + cellHeight / 2

                    linePaint.color = marker.color
                    linePaint.strokeWidth = markerRadius * 2
                    it.drawLine(prevMarkerX + offsetX, prevMarkerY + offsetY, markerX + offsetX, markerY + offsetY, linePaint)
                }
            }

            // Adds collision event markers to the map
            markers.forEachIndexed { index, marker ->
                val markerX = marker.x * cellWidth + cellWidth / 2
                val markerY = marker.y * cellHeight + cellHeight / 2

                // Draws blue circle on the map when collisionEvent is true
                if (marker.collisionEvent) {
                    markerPaint.color = Color.BLUE
                    val innerMarkerRadius = markerRadius * 4
                    it.drawCircle(markerX + offsetX, markerY + offsetY, innerMarkerRadius, markerPaint)
                }
            }

        }
        if (canvas != null) {
            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.action) {
            // Pointer touches screen
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y

                val touchX = event.x
                val touchY = event.y

                // Calculate the size of each cell
                val cellWidth = width.toFloat() / Width
                val cellHeight = height.toFloat() / Height

                // Loop through the markers and check if the touch event is inside the marker
                markers.forEach { marker ->
                    if (marker.collisionEvent) {
                        val markerX = marker.x * cellWidth + cellWidth / 2
                        val markerY = marker.y * cellHeight + cellHeight / 2
                        val markerRadius = 5f * 4

                        if (isTouchInsideMarker(
                                touchX,
                                touchY,
                                markerX,
                                markerY,
                                markerRadius
                            )
                        ) {
                            // Replace the block below with navigation or popup dialog with the Image received from the backend team.
                            Toast.makeText(
                                context,
                                "Collision avoided at (${marker.x}, ${marker.y})",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
            // Centers the map on the current location of the pointer
            MotionEvent.ACTION_MOVE -> {

                    offsetX += event.x - lastTouchX
                    offsetY += event.y - lastTouchY

                    invalidate()

                    lastTouchX = event.x
                    lastTouchY = event.y
            }
        }
        return true
    }

    // Checks if the pointer is inside the marker radius
    private fun isTouchInsideMarker(touchX: Float, touchY: Float, markerX: Float, markerY: Float, markerRadius: Float): Boolean {
        val dx = touchX - markerX
        val dy = touchY - markerY
        val distance = sqrt(dx * dx + dy * dy)
        return distance <= markerRadius
    }

    // Adds marker to map
    fun addMarker(marker: GridMarker) {
        this.markers.add(marker)
        val markerX = marker.x * cellWidth + cellWidth / 2
        val markerY = marker.y * cellHeight + cellHeight / 2

        matrix.reset()
        matrix.postTranslate(width / 2f - markerX, height / 2f - markerY)

        invalidate()
    }
}
