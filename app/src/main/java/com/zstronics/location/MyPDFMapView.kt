package com.zstronics.location

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.Toast
import com.github.barteksc.pdfviewer.PDFView
import kotlin.math.pow
import kotlin.math.sqrt

class MyPDFMapView(context: Context, attrs: AttributeSet?) : PDFView(context, attrs) {

    private val pdfMatrix = Matrix()
    private val tappedPoint = PointF()

    private val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val markerText = "Location"

    private var xPosition = 0f
    private var yPosition = 0f
    private val circleCoordinates = ArrayList<PointF>()

    init {
        markerPaint.color = Color.RED // Marker color
        markerPaint.style = Paint.Style.STROKE

        markerTextPaint.color = Color.RED // Text color
        markerTextPaint.textSize = 40f // Text size
        markerTextPaint.textAlign = Paint.Align.CENTER

        setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val pdfPoint = floatArrayOf(event.x, event.y)
                    pdfMatrix.invert(pdfMatrix)
                    pdfMatrix.mapPoints(pdfPoint)

                    tappedPoint.x = pdfPoint[0]
                    tappedPoint.y = pdfPoint[1]

                    // Check if the tap is within the bounds of an existing circle
                    val tappedCircle = findTappedCircle(tappedPoint.x, tappedPoint.y)
                    if (tappedCircle != null) {
                        showToast("Click on location ${circleCoordinates.indexOf(tappedCircle)} ${tappedCircle.x}, ${tappedCircle.y}")
                    } else {
                        // Add the tapped point to the list of coordinates
                        circleCoordinates.add(PointF(tappedPoint.x, tappedPoint.y))
                    }
                    invalidate()

                    true
                }

                else -> false
            }
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)


        // Draw circles at the specified coordinates
        for (coordinate in circleCoordinates) {
            // Draw the marker (map pin)
            val markerRadius = 20f
            canvas?.drawCircle(coordinate.x, coordinate.y - markerRadius, markerRadius, markerPaint)

            // Draw text (e.g., "Pin") above the marker
            val textBounds = Rect()
            markerTextPaint.getTextBounds(markerText, 0, markerText.length, textBounds)
            canvas?.drawText(
                "$markerText ${circleCoordinates.indexOf(coordinate)}",
                coordinate.x,
                coordinate.y - markerRadius - textBounds.height(),
                markerTextPaint
            )
        }
    }

    private fun createTaskAtLocation(x: Float, y: Float) {
        Log.d("MyPDFMapView", "createTaskAtLocation: x = $x, y = $y")
        setPosition(x, y)
        Toast.makeText(context, "createTaskAtLocation: x = $x, y = $y", Toast.LENGTH_SHORT).show()
        // Implement task creation logic here based on the tapped coordinates (x, y)
        // You can add a marker or perform any other action to indicate the task location.
    }

    // Set the position where you want to display the map pin
    private fun setPosition(x: Float, y: Float) {
        xPosition = x
        yPosition = y
        invalidate() // Trigger a redraw
    }

    private fun findTappedCircle(x: Float, y: Float): PointF? {
        // Check if the tap is within the bounds of an existing circle
        for (coordinate in circleCoordinates) {
            val distance = sqrt(
                (x - coordinate.x).toDouble().pow(2.0) + (y - coordinate.y).toDouble().pow(2.0)
            )
            if (distance <= 20) { // 20 is the radius of the circle
                return coordinate
            }
        }
        return null
    }

    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
