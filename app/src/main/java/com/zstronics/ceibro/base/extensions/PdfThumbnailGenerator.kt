package com.zstronics.ceibro.base.extensions

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import android.util.Size
import java.io.File
import java.io.IOException

class PdfThumbnailGenerator {

    fun generateThumbnail(pdfFilePath: String, pageNumber: Int, thumbnailSize: Size): Bitmap? {
        val file = File(pdfFilePath)

        try {
            // Open the PDF file with a ParcelFileDescriptor
            val fileDescriptor =
                ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
            val pdfRenderer = PdfRenderer(fileDescriptor)

            // Get the specific page from the PDF
            val pdfPage = pdfRenderer.openPage(pageNumber)

            // Create a bitmap for the thumbnail
            val size = Size(pdfPage.width, pdfPage.height)
            val thumbnailBitmap = Bitmap.createBitmap(
                size.width,
                size.height,
                Bitmap.Config.ARGB_8888
            )

            // Render the PDF page onto the bitmap
            pdfPage.render(thumbnailBitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            // Close the page and the renderer
            fileDescriptor.close()
            pdfPage.close()
            pdfRenderer.close()

            return thumbnailBitmap
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null
    }
}
