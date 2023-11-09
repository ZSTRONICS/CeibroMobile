package com.zstronics.location

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.barteksc.pdfviewer.PDFView

class MyPDFMapView(context: Context, attrs: AttributeSet?) : PDFView(context, attrs) {

//    fun convertScreenPointsToPdfCoordinates(e: MotionEvent): PointF? {
//        val x = e.x
//        val y = e.y
//
//        val mappedX = -currentXOffset + x
//        val mappedY = -currentYOffset + y
//
//        val page = getPageAtPositionOffset(if (isSwipeVertical()) mappedY else mappedX)
//        val pageSize = this.pdfFile.getScaledPageSize(page, zoom)
//
//        val pageX: Int
//        val pageY: Int
//
//        if (isSwipeVertical()) {
//            pageX = pdfFile.getSecondaryPageOffset(page, zoom).toInt()
//            pageY = pdfFile.getPageOffset(page, zoom).toInt()
//        } else {
//            pageY = pdfFile.getSecondaryPageOffset(page, zoom).toInt()
//            pageX = pdfFile.getPageOffset(page, zoom).toInt()
//        }
//
//        return pdfiumSDK.mapDeviceCoordinateToPage(
//            pdfFile.getPdfDocument(), page, pageX, pageY, pageSize.width.toInt(),
//            pageSize.height.toInt(), 0, mappedX.toInt(), mappedY.toInt()
//        )
//    }
}
