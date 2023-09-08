package ee.zstronics.ceibro.camera

import android.content.Intent
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.core.content.ContextCompat
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
                Intent()
            val bundle = Bundle()
            bundle.putParcelableArrayList("images", listOfPickedImages)
            ceibroCamera.putExtras(bundle)
            setResult(RESULT_OK, ceibroCamera)
            finish()
        }
    }

    private fun reInitiateCamera() {
        val handler = Handler()
        handler.postDelayed(Runnable {
            setResult(RESULT_CANCELED)
            finish()
        }, 60)
    }

    override fun onBackPressed() {
        reInitiateCamera()
    }
}