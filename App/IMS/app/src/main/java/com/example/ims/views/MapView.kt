package com.example.ims.views

import android.content.Context
import android.content.Intent
import android.graphics.*
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.view.ViewConfiguration
import androidx.core.content.ContextCompat
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt
import android.graphics.drawable.BitmapDrawable
import com.example.ims.R
import com.example.ims.activities.ImagePopUpActivity
import com.example.ims.data.LocationMarker
import com.example.ims.services.ImageApi
import kotlin.math.ceil

class MapView(context: Context, attrs: AttributeSet?) : View(context, attrs) {
    var onCollisionListener: OnCollisionListener? = null
    private var canvasWidth: Int = 10000
    private var canvasHeight: Int = 15000
    private var viewWidth: Int = 0
    private var viewHeight: Int = 0
    private val markers = mutableListOf<LocationMarker>()
    private val markerPaint = Paint()
    private val linePaint = Paint()
    private var markerRadius = 5f
    private val markerColor = Color.BLUE
    private val matrix = Matrix()
    private var scaleFactor = 1f
    private val scaleDetector: ScaleGestureDetector
    private val touchSlop: Int
    private var lastTouchX = 0f
    private var lastTouchY = 0f
    private var offsetX = 0f
    private var offsetY = 0f
    private var cellWidth = 0f
    private var cellHeight = 0f
    private var isMarkerCentered = true
    private val iconWarning: Drawable
    private val iconMowerRight: Drawable
    private val iconMowerLeft: Drawable
    private val backgroundBitmap: Bitmap
    private val imageApi = ImageApi()

    interface OnCollisionListener {
        fun onCollision(imageId:Int)
    }
    init {
        val configuration = ViewConfiguration.get(context)
        touchSlop = configuration.scaledTouchSlop

        // Initialize marker and line paint style
        markerPaint.style = Paint.Style.FILL
        linePaint.style = Paint.Style.STROKE

        iconWarning = ContextCompat.getDrawable(context, R.drawable.baseline_warning_24)!!
        iconMowerRight = ContextCompat.getDrawable(context, R.drawable.robotmower_right)!!
        iconMowerLeft = ContextCompat.getDrawable(context, R.drawable.robotmower_left)!!

        val backgroundDrawable = ContextCompat.getDrawable(context, R.drawable.grass_ims)
        backgroundBitmap = (backgroundDrawable as BitmapDrawable).bitmap

        // Dummy marker to display the mower icon at start of fragment
        markers.add(LocationMarker(5000, 5000, false))

        scaleDetector = ScaleGestureDetector(
            context,
            object : ScaleGestureDetector.SimpleOnScaleGestureListener() {
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

        viewWidth = w
        viewHeight = h

        // Calculate cellWidth and cellHeight based on the view's width and height
        cellWidth = width.toFloat() / canvasWidth
        cellHeight = height.toFloat() / canvasHeight
    }


    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        val markerX = width / 2f
        val markerY = height / 2f

        // Updates the matrix with the scale factor
        matrix.reset()
        matrix.postScale(scaleFactor, scaleFactor, markerX, markerY)

        // Centers the last marker on the map
        if (isMarkerCentered && markers.isNotEmpty()) {
            centerMap()
        }

        canvas?.save()
        canvas?.concat(matrix)

        // Draws the tiled background
        val bgWidth = backgroundBitmap.width
        val bgHeight = backgroundBitmap.height

        // Calculates the number of columns and rows of background images needed
        val numCols = ceil((width / scaleFactor / bgWidth.toFloat()) * 2).toInt()
        val numRows = ceil((height / scaleFactor / bgHeight.toFloat()) * 2).toInt()

        val offsetXInt = (-offsetX).toInt() % bgWidth
        val offsetYInt = (-offsetY).toInt() % bgHeight

        // Loops through the columns and rows and draws additional backgrounds
        for (i in -numCols..numCols) {
            for (j in -numRows..numRows) {
                canvas?.drawBitmap(
                    backgroundBitmap,
                    (i * bgWidth - offsetXInt).toFloat(),
                    (j * bgHeight - offsetYInt).toFloat(),
                    null
                )
            }
        }

        canvas?.let {
            markers.forEachIndexed { index, marker ->

                    drawLineBetweenMarkers(index, offsetX, offsetY, marker, it)

                    if (index == markers.size - 1) {
                        drawMower(index, offsetX, offsetY, marker, it)
                    } else {
                        drawMarker(marker, offsetX, offsetY, it)
                    }
            }


            // Adds collision event markers to the map
            markers.forEachIndexed { index, marker ->
                // Get the center coordinates of the latest marker
                val (markerCenterX, markerCenterY) = getMarkerCenterCoordinates(marker)

                // Draws warning icon on the map when collisionEvent is true
                if (marker.collisionEvent) {

                    val iconWidth = iconWarning.intrinsicWidth
                    val iconHeight = iconWarning.intrinsicHeight
                    val halfIconWidth = iconWidth / 2
                    val halfIconHeight = iconHeight / 2

                    val left = (markerCenterX + offsetX - halfIconWidth).toInt()
                    val top = (markerCenterY + offsetY - halfIconHeight).toInt()
                    val right = (markerCenterX + offsetX + halfIconWidth).toInt()
                    val bottom = (markerCenterY + offsetY + halfIconHeight).toInt()

                    iconWarning.setBounds(left, top, right, bottom)
                    iconWarning.draw(it)
                }
            }

        }
        if (canvas != null) {
            canvas.restore()
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)
        isMarkerCentered = false
        when (event.action) {
            // Pointer touches screen
            MotionEvent.ACTION_DOWN -> {
                lastTouchX = event.x
                lastTouchY = event.y

                // transform the touch points coordinates
                val transformedTouchPoint = transformTouchCoordinates(event.x, event.y)
                val touchX = transformedTouchPoint.x
                val touchY = transformedTouchPoint.y

                // Calculate the size of each cell
                val cellWidth = width.toFloat() / canvasWidth
                val cellHeight = height.toFloat() / canvasHeight

                // Loop through the markers and check if the touch event is inside the marker
                markers.forEach { marker ->
                    if (marker.collisionEvent) {
                        val markerCenterX = marker.x * cellWidth + cellWidth / 2 + offsetX
                        val markerCenterY = marker.y * cellHeight + cellHeight / 2 + offsetY

                        val markerRadius = 5f * 4

                        if (isTouchInsideMarker(
                                touchX,
                                touchY,
                                markerCenterX,
                                markerCenterY,
                                markerRadius
                            )
                        ) {
                            // Exchange with the ID of the mapMarker where collisionEvent == true
                            val imageId = 11
                            startImagePopUpActivity(imageId)
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

    private fun startImagePopUpActivity(imageId: Int) {
        val intent = Intent(context, ImagePopUpActivity::class.java)

        imageApi.getImageByteArrayById(imageId) { result ->
            if (result.isSuccess) {

                // Sets imageView in the dialogbox with the bitmap result
                val imageByteArray = result.getOrNull()

                intent.putExtra("bitmap", imageByteArray)
                context.startActivity(intent)
            } else if (result.isFailure) {
                val exception = result.exceptionOrNull()
            }
        }
    }

    private fun drawLineBetweenMarkers(index: Int, offsetX: Float, offsetY: Float, marker: LocationMarker, it: Canvas) {
        if (index > 0) {
            val prevMarker = markers[index - 1]
            val (prevMarkerX, prevMarkerY) = getMarkerCenterCoordinates(prevMarker)
            val (markerCenterX, markerCenterY) = getMarkerCenterCoordinates(marker)

            linePaint.color = markerColor
            linePaint.strokeWidth = markerRadius * 2
            it.drawLine(
                prevMarkerX + offsetX,
                prevMarkerY + offsetY,
                markerCenterX + offsetX,
                markerCenterY + offsetY,
                linePaint
            )
        }
    }
    private fun drawMower(index: Int, offsetX: Float, offsetY: Float, marker: LocationMarker, it: Canvas) {
        val (markerCenterX, markerCenterY) = getMarkerCenterCoordinates(marker)

        val iconWidth = iconWarning.intrinsicWidth
        val iconHeight = iconWarning.intrinsicHeight
        val halfIconWidth = iconWidth
        val halfIconHeight = iconHeight

        val left = (markerCenterX + offsetX - halfIconWidth).toInt()
        val top = (markerCenterY + offsetY - halfIconHeight).toInt()
        val right = (markerCenterX + offsetX + halfIconWidth).toInt()
        val bottom = (markerCenterY + offsetY + halfIconHeight).toInt()

        // Draws the mower facing the direction of the movement
        val iconMower = if (index > 0 && markers[index - 1].x >= marker.x) {
            iconMowerLeft
        } else {
            iconMowerRight
        }

        iconMower.setBounds(left, top, right, bottom)
        iconMower.draw(it)
    }

    private fun drawMarker(marker: LocationMarker, offsetX: Float, offsetY: Float, it: Canvas) {
        val (markerCenterX, markerCenterY) = getMarkerCenterCoordinates(marker)

        markerPaint.color = markerColor
        it.drawCircle(
            markerCenterX + offsetX,
            markerCenterY + offsetY,
            markerRadius,
            markerPaint
        )
    }

    // Transforms coordinates from the touch points to be in the same coordinate system as the markers
    private fun transformTouchCoordinates(touchX: Float, touchY: Float): PointF {
        val invertedMatrix = Matrix()
        matrix.invert(invertedMatrix)

        val touchCoordinates = floatArrayOf(touchX, touchY)
        invertedMatrix.mapPoints(touchCoordinates)

        return PointF(touchCoordinates[0], touchCoordinates[1])
    }

    // Checks if the pointer is inside the marker radius
    private fun isTouchInsideMarker(
        touchX: Float,
        touchY: Float,
        markerX: Float,
        markerY: Float,
        markerRadius: Float
    ): Boolean {
        val dx = touchX - markerX
        val dy = touchY - markerY
        val distance = sqrt(dx * dx + dy * dy)

        return distance <= markerRadius
    }

    // Adds marker to map
    fun addMarker(marker: LocationMarker) {

        this.markers.add(marker)
        val (markerCenterX, markerCenterY) = getMarkerCenterCoordinates(marker)

        // Handles collision event
        if (marker.collisionEvent) {
            // Replace with marker ID when endpoint is finished
            val markerId = 11
            onCollisionListener?.onCollision(markerId)
        }

        matrix.reset()
        matrix.postTranslate(width / 2f - markerCenterX, height / 2f - markerCenterY)

        invalidate()
    }

    // Returns the X and Y coordinates of the marker in the view
    fun getMarkerCenterCoordinates(marker: LocationMarker): Pair<Float, Float> {
        val markerCenterX = marker.x * cellWidth + cellWidth / 2
        val markerCenterY = marker.y * cellHeight + cellHeight / 2

        return Pair(markerCenterX, markerCenterY)
    }

    // Centers the map on the current location of the mower
    fun centerMap() {
        val marker = markers.last()
        scaleFactor = 1f
        val (markerCenterX, markerCenterY) = getMarkerCenterCoordinates(marker)

        // Calculate the translation required to center the latest marker
        offsetX = (width / 2f - markerCenterX * scaleFactor)
        offsetY = (height / 2f - markerCenterY * scaleFactor)

        invalidate()

        isMarkerCentered = true
    }

}