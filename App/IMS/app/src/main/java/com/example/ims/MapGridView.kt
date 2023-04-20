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
import android.widget.GridView
import android.widget.Toast
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
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
    private val matrix = Matrix()
    private val inverseMatrix = Matrix()
    private var scaleFactor = 1f
    private val scaleDetector: ScaleGestureDetector
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private val touchSlop: Int


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

    fun setGridSize(width: Int, height: Int) {
        gridWidth = Width
        gridHeight = Height
        invalidate()
    }

    fun addMarker(marker: GridMarker) {
        this.markers.add(marker)

        val cellWidth = width.toFloat() / gridWidth
        val cellHeight = height.toFloat() / gridHeight

        val markerX = marker.x * cellWidth + cellWidth / 2
        val markerY = marker.y * cellHeight + cellHeight / 2

        matrix.reset()
        matrix.postTranslate(width / 2f - markerX, height / 2f - markerY)

        invalidate()
    }
    fun addMarkers(markers: List<GridMarker>) {
        this.markers.addAll(markers)
        invalidate()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        // Update the matrix with the scale factor
        val markerX = width / 2f
        val markerY = height / 2f

        matrix.reset()
        matrix.postScale(scaleFactor, scaleFactor, markerX, markerY)
        if (markers.isNotEmpty()) {
            val marker = markers.last()
            val cellWidth = width.toFloat() / gridWidth
            val cellHeight = height.toFloat() / gridHeight

            val x = marker.x * cellWidth + cellWidth / 2
            val y = marker.y * cellHeight + cellHeight / 2
            matrix.postTranslate(width / 2f - x * scaleFactor, height / 2f - y * scaleFactor)
        }

        if (canvas != null) {
            canvas.save()
        }
        if (canvas != null) {
            canvas.concat(matrix)
        }

        val cellWidth = width.toFloat() / gridWidth
        val cellHeight = height.toFloat() / gridHeight

        canvas?.let {

            // markers and lines paint style
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
                // Draw marker
                if (index == markers.size - 1){
                    markerPaint.color = Color.BLACK
                    markerRadius = 15f
                    it.drawCircle(markerX, markerY, markerRadius, markerPaint)
                    markerRadius = 5f
                } else {
                    markerPaint.color = marker.color
                    it.drawCircle(markerX, markerY, markerRadius, markerPaint)
                }

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
        if (canvas != null) {
            canvas.restore()
        }
    }

    // Checking if the click is inside the marker radius
    private fun isTouchInsideMarker(touchX: Float, touchY: Float, markerX: Float, markerY: Float, markerRadius: Float): Boolean {
        val dx = touchX - markerX
        val dy = touchY - markerY
        val distance = sqrt(dx * dx + dy * dy)
        return distance <= markerRadius
    }
    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y

                val touchPoint = floatArrayOf(event.x, event.y)
                inverseMatrix.set(matrix)
                inverseMatrix.invert(inverseMatrix)
                inverseMatrix.mapPoints(touchPoint)

                val touchX = touchPoint[0]
                val touchY = touchPoint[1]

                // Calculate the size of each cell
                val cellWidth = width.toFloat() / gridWidth
                val cellHeight = height.toFloat() / gridHeight

                // Loop through the markers and check if the touch event is inside the marker
                markers.forEach { marker ->
                    if (marker.collisionEvent) {
                        val markerX = marker.x * cellWidth + cellWidth / 2
                        val markerY = marker.y * cellHeight + cellHeight / 2
                        val markerRadius =
                            5f * 4  // 5f is the base markerRadius and 4 is the scaling factor

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
            MotionEvent.ACTION_MOVE -> {
                if (!scaleDetector.isInProgress) {
                    val dx = event.x - lastTouchX
                    val dy = event.y - lastTouchY

                    if (abs(dx) >= touchSlop || abs(dy) >= touchSlop) {
                        matrix.postTranslate(dx, dy)
                        invalidate()
                        lastTouchX = event.x
                        lastTouchY = event.y
                    }
                }
            }
        }
        return true
    }

}
