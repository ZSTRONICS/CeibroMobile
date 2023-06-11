package ee.zstronics.ceibro.camera

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import ee.zstronics.ceibro.camera.databinding.ActivityCeibroImageEditorBinding

class CeibroImageEditorActivity : BaseActivity() {
    lateinit var binding: ActivityCeibroImageEditorBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding =
            DataBindingUtil.inflate(
                layoutInflater,
                R.layout.activity_ceibro_image_editor,
                null,
                false
            )
        setContentView(binding.root)

        // Retrieve the URI from the bundle
//        val capturedUri = intent.getParcelableExtra<Uri>("capturedUri")

        // Load the image into the ImageView using Glide
//        Glide.with(this)
//            .load(capturedUri)
//            .into(binding.displayImg)

        binding.closeBtn.setOnClickListener {

        }

    }

    override fun onBackPressed() {
        //do same as for cancel
    }
}