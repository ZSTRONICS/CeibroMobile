package com.zstronics.location

import android.content.Context
import android.graphics.PointF
import android.util.AttributeSet
import android.view.MotionEvent
import com.github.barteksc.pdfviewer.PDFView
import com.shockwave.pdfium.PdfiumCore
import com.shockwave.pdfium.util.SizeF

class MyPDFMapView(context: Context, attrs: AttributeSet?) : PDFView(context, attrs) {

//    fun convertScreenPointsToPdfCoordinates(e: MotionEvent): PointF? {
//        val x = e.x
//        val y = e.y
//
//        val mappedX = -currentXOffset + x
//        val mappedY = -currentYOffset + y
//
//        val page = getPageAtPositionOffset(if (isSwipeVertical) mappedY else mappedX)
////        val pageSize = this.pdfFile.getScaledPageSize(page, zoom)
//        val pageSize = SizeF(getPageSize(page).width * zoom, getPageSize(page).height * zoom)
//
//        val pageX: Int
//        val pageY: Int
//
//        if (isSwipeVertical) {
//            pageX = getSecondaryPageOffset(page, zoom).toInt()
//            pageY = y.toInt()
//        } else {
//            pageY = getSecondaryPageOffset(page, zoom).toInt()
//            pageX = x.toInt()
//        }
//         val pdfiumSDK = PdfiumCore(context)
//        return pdfiumSDK.mapPageCoordsToDevice(pdfFile, page, pageX, pageY, pageSize.width.toInt(),
//            pageSize.height.toInt(), 0, mappedX.toInt(), mappedY.toInt()
//        )
//    }

    private fun getSecondaryPageOffset(pageIndex: Int, zoom: Float): Float {
        val pageSize = getPageSize(pageIndex)
        return if (isSwipeVertical) {
            val maxWidth: Float = width.toFloat()
            zoom * (maxWidth - pageSize.width) / 2 //x
        } else {
            val maxHeight: Float = height.toFloat()
            zoom * (maxHeight - pageSize.height) / 2 //y
        }
    }
}
