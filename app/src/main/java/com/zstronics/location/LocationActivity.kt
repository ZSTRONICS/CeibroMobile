package com.zstronics.location

import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.os.Bundle
import android.view.MotionEvent
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.zstronics.ceibro.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import kotlin.math.pow
import kotlin.math.sqrt

class LocationActivity : AppCompatActivity() {

    private val pdfMatrix = Matrix()
    private val tappedPoint = PointF()

    val markerPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val markerTextPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    val markerText = "Location"

    private var xPosition = 0f
    private var yPosition = 0f
    val circleCoordinates = ArrayList<PointF>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)

        markerPaint.color = Color.RED // Marker color
        markerPaint.style = Paint.Style.STROKE

        markerTextPaint.color = Color.RED // Text color
        markerTextPaint.textSize = 40f // Text size
        markerTextPaint.textAlign = Paint.Align.CENTER

        // Assuming you have already created an instance of MyPDFMapView
        val pdfMapView = findViewById<MyPDFMapView>(R.id.pdfMapView)
        val zoomSwitch = findViewById<SwitchCompat>(R.id.zoomSwitch)
        zoomSwitch.setOnCheckedChangeListener { _, isChecked ->
        }
        val file = File(cacheDir, "sample.pdf") // Adjust the file path as needed

        if (!file.exists()) {
            try {
                val assetManager = assets
                val asset: InputStream = assetManager.open("sample.pdf")
                val output = FileOutputStream(file)
                val buffer = ByteArray(1024)
                var size: Int
                while (asset.read(buffer).also { size = it } != -1) {
                    output.write(buffer, 0, size)
                }
                asset.close()
                output.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        var currentZoomFactor = 1.0f

        pdfMapView.fromFile(file)
            .enableSwipe(true)
            .enableAnnotationRendering(true)
            .onTap { event ->
                val pdfPoint = floatArrayOf(event.x, event.y)
                pdfMatrix.invert(pdfMatrix)
                pdfMatrix.mapPoints(pdfPoint)

                val adjustedX = pdfPoint[0]
                val adjustedY = pdfPoint[1]

                tappedPoint.x = adjustedX
                tappedPoint.y = adjustedY


                // Check if the tap is within the bounds of an existing circle
                val tappedCircle = findTappedCircle(tappedPoint.x, tappedPoint.y)
                if (tappedCircle != null) {
                    showToast("Click on location ${circleCoordinates.indexOf(tappedCircle)} ${tappedCircle.x}, ${tappedCircle.y}")
                } else {
                    // Add the tapped point to the list of coordinates
                    circleCoordinates.add(PointF(tappedPoint.x, tappedPoint.y))
                }
                pdfMapView.invalidate()
                true
            }
            .onDraw { canvas, pageWidth, pageHeight, displayedPage -> // Draw the circles on the canvas
                canvas.save()
                canvas.scale(pdfMapView.zoom, pdfMapView.zoom)
                // Draw circles at the specified coordinates
                for (coordinate in circleCoordinates) {
                    // Draw the marker (map pin)
                    val markerRadius = 20f
                    canvas?.drawCircle(
                        coordinate.x,
                        coordinate.y - markerRadius,
                        markerRadius,
                        markerPaint
                    )

                    // Draw text (e.g., "Pin") above the marker
                    val textBounds = Rect()
                    markerTextPaint.getTextBounds(
                        markerText,
                        0,
                        markerText.length,
                        textBounds
                    )
                    canvas?.drawText(
                        "$markerText ${circleCoordinates.indexOf(coordinate)}",
                        coordinate.x,
                        coordinate.y - markerRadius - textBounds.height(),
                        markerTextPaint
                    )
                }
                canvas.restore()
            }
            .load()
    }

    private fun showToast(message: String) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
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


//    private fun createTaskAtLocation(x: Float, y: Float) {
//        Log.d("MyPDFMapView", "createTaskAtLocation: x = $x, y = $y")
//        setPosition(x, y)
//        Toast.makeText(applicationContext, "createTaskAtLocation: x = $x, y = $y", Toast.LENGTH_SHORT).show()
//        // Implement task creation logic here based on the tapped coordinates (x, y)
//        // You can add a marker or perform any other action to indicate the task location.
//    }
//
//    // Set the position where you want to display the map pin
//    private fun setPosition(x: Float, y: Float) {
//        xPosition = x
//        yPosition = y
//        invalidate() // Trigger a redraw
//    }
}

interface ZoomListener {
    fun onZoomEnabled(zoomEnabled: Boolean)
}