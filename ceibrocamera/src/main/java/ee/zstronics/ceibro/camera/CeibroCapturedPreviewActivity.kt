package ee.zstronics.ceibro.camera

import android.net.Uri
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import ee.zstronics.ceibro.camera.databinding.ActivityCeibroCapturedPreviewBinding

class CeibroCapturedPreviewActivity : BaseActivity() {
    lateinit var binding: ActivityCeibroCapturedPreviewBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.activity_ceibro_captured_preview,
                null,
                false
            )
        setContentView(binding.root)

        // Retrieve the URI from the bundle
        val capturedUri = intent.getParcelableExtra<Uri>("capturedUri")

        // Load the image into the ImageView using Glide
        Glide.with(this)
            .load(capturedUri)
            .into(binding.previewViewImage)

        binding.close.setOnClickListener {
            onBackPressed()
        }

        binding.retakePhoto.setOnClickListener {
            onBackPressed()
        }
        binding.gotoNext.setOnClickListener {
            val pickedImages = arrayListOf<PickedImages>()
            pickedImages.add(getPickedImage(capturedUri))
        }
    }
}