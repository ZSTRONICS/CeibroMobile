package ee.zstronics.ceibro.camera

import android.Manifest
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import ee.zstronics.ceibro.camera.databinding.ActivityCeibroImageViewerBinding

class CeibroImageViewerActivity : BaseActivity() {
    lateinit var binding: ActivityCeibroImageViewerBinding
    val listOfImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData()
    var isBottomImageLayoutVisible = true
    var fullImageAdapter: CeibroFullImageVPAdapter = CeibroFullImageVPAdapter()
    var smallImageAdapter: CeibroSmallImageRVAdapter = CeibroSmallImageRVAdapter()

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
            fullImageAdapter.setList(it)
            smallImageAdapter.setList(it)
        }
        binding.fullSizeImagesVP.adapter = fullImageAdapter
        binding.smallFooterImagesRV.adapter = smallImageAdapter

        smallImageAdapter.itemClickListener =
            { _: View, position: Int ->
                binding.fullSizeImagesVP.setCurrentItem(position, true)
            }
        binding.fullSizeImagesVP.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.smallFooterImagesRV.smoothScrollToPosition(position)
                smallImageAdapter.setSelectedItem(position)
            }
        })


        binding.closeBtn.setOnClickListener {
            showCancelDialog()
        }

        binding.doneBtn.setOnClickListener {
            listOfImages.value?.let { listOfPickedImages ->
                val ceibroImagesIntent =
                    Intent()
                val newBundle = Bundle()
                newBundle.putParcelableArrayList("images", listOfPickedImages)
                ceibroImagesIntent.putExtras(newBundle)
                setResult(RESULT_OK, ceibroImagesIntent)
                finish()
            }
        }

        binding.galleryButton.setOnClickListener {
            checkPermission(
                listOf(
                    Manifest.permission.CAMERA,
                )
            ) {
                pickFiles { listOfPickedImages ->
                    val oldImages = listOfImages.value
                    oldImages?.addAll(listOfPickedImages)
                    listOfImages.postValue(oldImages)
                }
            }
        }
        binding.cameraBtn.setOnClickListener {
            val ceibroCamera = Intent(
                applicationContext,
                CeibroCameraActivity::class.java
            )
            ceibroCamera.putExtra("source_name", CeibroImageViewerActivity::class.java.name)
            ceibroImagesPickerLauncher.launch(ceibroCamera)
        }

        binding.footerImageLayoutMoveBtn.setOnClickListener {
            if (isBottomImageLayoutVisible) {
                isBottomImageLayoutVisible = false
                binding.footerImagesLayout.animate()
                    .translationY(binding.footerImagesLayout.height.toFloat() - 50)
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
    }

    private val ceibroImagesPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val listOfPickedImages =
                    result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                        ?: arrayListOf()
                val oldImages = listOfImages.value
                oldImages?.addAll(listOfPickedImages)
                listOfImages.postValue(oldImages)
            }
        }

    private fun showCancelDialog() {
        val dialogView = layoutInflater.inflate(R.layout.layout_cancel_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        val yesBtn = dialogView.findViewById<Button>(R.id.yesBtn)
        val cancelBtn = dialogView.findViewById<Button>(R.id.cancelBtn)
        alertDialog?.window?.setBackgroundDrawable(null)

        yesBtn.setOnClickListener {
            val ceibroImagesIntent = Intent()
            val newBundle = Bundle()
            newBundle.putParcelableArrayList("images", arrayListOf())
            ceibroImagesIntent.putExtras(newBundle)
            setResult(RESULT_OK, ceibroImagesIntent)
            alertDialog.dismiss()
            finish()
        }

        cancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }
}