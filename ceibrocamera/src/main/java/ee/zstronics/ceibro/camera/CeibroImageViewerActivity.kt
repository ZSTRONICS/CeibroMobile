package ee.zstronics.ceibro.camera

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import ee.zstronics.ceibro.camera.databinding.ActivityCeibroCapturedPreviewBinding
import ee.zstronics.ceibro.camera.databinding.ActivityCeibroImageViewerBinding

class CeibroImageViewerActivity : AppCompatActivity() {
    lateinit var binding: ActivityCeibroImageViewerBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.activity_ceibro_image_viewer,
                null,
                false
            )
        setContentView(binding.root)
        binding.closeBtn.setOnClickListener {
            finishAffinity()
        }


//        val capturedUri = intent.getParcelableExtra<Uri>("capturedUri")
//
//        Glide.with(this)
//            .load(capturedUri)
//            .into(binding.previewViewImage)
    }
}