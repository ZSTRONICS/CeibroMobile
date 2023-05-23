package ee.zstronics.ceibro.camera

import android.content.Intent
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
            reInitiateCamera()
        }

        binding.retakePhoto.setOnClickListener {
            reInitiateCamera()
        }
        binding.gotoNext.setOnClickListener {
            val listOfPickedImages = arrayListOf<PickedImages>()
            listOfPickedImages.add(getPickedImage(capturedUri))

            val ceibroCamera =
                Intent(applicationContext, CeibroImageViewerActivity::class.java)
            val bundle = Bundle()
            bundle.putParcelableArrayList("images", listOfPickedImages)
            ceibroCamera.putExtras(bundle)
            startActivity(ceibroCamera)
            finish()
        }
    }

    private fun reInitiateCamera() {
        val ceibroCamera = Intent(
            applicationContext,
            CeibroCameraActivity::class.java
        )
        startActivity(ceibroCamera)
        finish()
    }

    override fun onBackPressed() {
        reInitiateCamera()
    }
}