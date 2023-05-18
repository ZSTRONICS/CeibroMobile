package ee.zstronics.ceibro.camera

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import ee.zstronics.ceibro.camera.databinding.ActivityCeibroImageViewerBinding

class CeibroImageViewerActivity : AppCompatActivity() {
    lateinit var binding: ActivityCeibroImageViewerBinding
    val listOfImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData()
    var isBottomImageLayoutVisible = true

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

        val bundle = intent.extras
        val images = bundle?.getParcelableArrayList<PickedImages>("images")
        listOfImages.value = images


        listOfImages.observe(this) {
            /// add these images to adapter
        }

        binding.closeBtn.setOnClickListener {
            finishAffinity()
        }


        binding.footerImageLayoutMoveBtn.setOnClickListener {
            if (isBottomImageLayoutVisible) {
                isBottomImageLayoutVisible = false
                binding.footerImagesLayout.animate()
                    .translationY(binding.footerImagesLayout.height.toFloat()-50)
                    .setDuration(350)
                    .withEndAction {
                        binding.footerImagesLayout.visibility = View.VISIBLE
                    }
                    .start()
            } else {
                isBottomImageLayoutVisible = true
                binding.footerImagesLayout.visibility = View.VISIBLE
                binding.footerImagesLayout.animate()
                    .translationY(0f)
                    .setDuration(350)
                    .start()
            }
        }

//        val capturedUri = intent.getParcelableExtra<Uri>("capturedUri")
//
//        Glide.with(this)
//            .load(capturedUri)
//            .into(binding.previewViewImage)
    }
}