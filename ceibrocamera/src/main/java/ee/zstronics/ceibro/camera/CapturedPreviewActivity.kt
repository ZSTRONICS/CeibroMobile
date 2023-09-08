package ee.zstronics.ceibro.camera

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import ee.zstronics.ceibro.camera.databinding.ActivityCeibroCapturedPreviewBinding

class CapturedPreviewActivity : BaseActivity() {
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
            val intent = Intent()
            val bundle = Bundle()
            bundle.putParcelableArrayList("images", listOfPickedImages)
            intent.putExtras(bundle)
            setResult(RESULT_OK, intent)
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