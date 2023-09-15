package com.zstronics.location

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.zstronics.ceibro.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

class LocationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_location)
        // Assuming you have already created an instance of MyPDFMapView
        val pdfMapView = findViewById<MyPDFMapView>(R.id.pdfMapView)

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
        pdfMapView.fromFile(file)
            .load()
    }
}