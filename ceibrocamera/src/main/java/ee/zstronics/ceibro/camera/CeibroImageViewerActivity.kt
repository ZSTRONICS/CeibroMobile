package ee.zstronics.ceibro.camera

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
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
    var lastSelectedPosition: Int = 0
    var newImagesAdded: Boolean = false
    var oldListIndexesSize: Int = 0         //this will be index count, considering from 0

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

        binding.commentsField.addTextChangedListener(textWatcher)

        val bundle = intent.extras
        val images = bundle?.getParcelableArrayList<PickedImages>("images")
        listOfImages.value = images


        listOfImages.observe(this) {
            fullImageAdapter.setList(it)
            smallImageAdapter.setList(it)
            try {
                binding.fullSizeImagesVP.offscreenPageLimit =
                    fullImageAdapter.itemCount     //this is set bcz sometimes when image loaded from gallery again, it doesn't show
            } catch (e: Exception) {
                println(e.toString())
            }
            if (newImagesAdded) {
                val newItemPosition = oldListIndexesSize + 1
                binding.fullSizeImagesVP.setCurrentItem(newItemPosition, true)
                binding.smallFooterImagesRV.smoothScrollToPosition(newItemPosition)
                smallImageAdapter.setSelectedItem(newItemPosition)
                newImagesAdded = false
            }
        }
        binding.fullSizeImagesVP.adapter = fullImageAdapter
        binding.smallFooterImagesRV.adapter = smallImageAdapter

        smallImageAdapter.itemClickListener =
            { _: View, position: Int ->
                binding.fullSizeImagesVP.setCurrentItem(position, true)
                setUserCommentLogic(position)
                hideKeyboard()
                binding.commentsField.clearFocus()
                lastSelectedPosition = position
            }
        binding.fullSizeImagesVP.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                binding.smallFooterImagesRV.smoothScrollToPosition(position)
                smallImageAdapter.setSelectedItem(position)
                setUserCommentLogic(position)
                hideKeyboard()
                binding.commentsField.clearFocus()
                lastSelectedPosition = position
            }
        })

        binding.commentsField.setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) {
                binding.confirmBtn.visibility = View.VISIBLE
                binding.editBtn.visibility = View.INVISIBLE
            }
        }

        binding.confirmBtn.setOnClickListener {
            val comment = binding.commentsField.text.toString()
            if (comment.isNotEmpty()) {
                val oldImages = listOfImages.value
                oldImages?.get(lastSelectedPosition)?.comment = comment
                listOfImages.postValue(oldImages)
                binding.confirmBtn.visibility = View.INVISIBLE
                binding.editBtn.visibility = View.VISIBLE
            } else {
                binding.confirmBtn.visibility = View.INVISIBLE
                binding.editBtn.visibility = View.INVISIBLE
            }

            binding.commentsField.clearFocus()
            hideKeyboard()
        }

        binding.editBtn.setOnClickListener {
            binding.commentsField.requestFocus()
            binding.commentsField.text?.length?.let { it1 -> binding.commentsField.setSelection(it1) }
            showKeyboard()
            binding.confirmBtn.visibility = View.VISIBLE
            binding.editBtn.visibility = View.INVISIBLE
        }

        binding.closeBtn.setOnClickListener {
            showCancelDialog()
        }

        binding.deleteBtn.setOnClickListener {
            showDeleteDialog()
        }

        binding.doneBtn.setOnClickListener {
            listOfImages.value?.let { listOfPickedImages ->
                val newList = listOfPickedImages.map { it ->
                    it.apply {
                        this.file = FileUtils.getFile(
                            applicationContext,
                            it.fileUri
                        )
                    }
                } as ArrayList<PickedImages>

                val ceibroImagesIntent =
                    Intent()
                val newBundle = Bundle()
                newBundle.putParcelableArrayList("images", newList)
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
        binding.editInnerLayout.setOnClickListener {
            listOfImages.value?.let {
                images?.get(lastSelectedPosition)?.fileUri?.let { uri ->
                    startEditor(uri) { updatedUri ->
                        if (updatedUri != null) {
                            val files = listOfImages.value
                            val file = files?.get(lastSelectedPosition)
                            file?.fileUri = updatedUri
                            files?.removeAt(lastSelectedPosition)
                            if (file != null) {
                                files.add(lastSelectedPosition, file)
                            }
                            listOfImages.postValue(files)
                        }
                    }
                }
            }
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s.toString().isNotEmpty()) {
                binding.confirmBtn.visibility = View.VISIBLE
                binding.editBtn.visibility = View.INVISIBLE
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    private fun setUserCommentLogic(position: Int) {
        val userComment = listOfImages.value?.get(position)?.comment
        binding.commentsField.setText(userComment)
        binding.confirmBtn.visibility = View.INVISIBLE
        if (userComment?.isNotEmpty() == true) {
            binding.editBtn.visibility = View.VISIBLE
        } else {
            binding.editBtn.visibility = View.INVISIBLE
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.commentsField.windowToken, 0)
    }

    private fun showKeyboard() {
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(binding.commentsField, InputMethodManager.SHOW_IMPLICIT)
    }

    private val ceibroImagesPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                val listOfPickedImages =
                    result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                        ?: arrayListOf()
                val oldImages = listOfImages.value
                if (listOfPickedImages.size > 0) {
                    oldListIndexesSize = oldImages?.size?.minus(1) ?: 0
                    newImagesAdded = true
                }
                oldImages?.addAll(listOfPickedImages)
                listOfImages.postValue(oldImages)
            }
        }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
//        super.onBackPressed()
        showCancelDialog()
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

    private fun showDeleteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.layout_cancel_dialog, null)

        val alertDialogBuilder = AlertDialog.Builder(this).setView(dialogView)
        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()

        val yesBtn = dialogView.findViewById<Button>(R.id.yesBtn)
        val cancelBtn = dialogView.findViewById<Button>(R.id.cancelBtn)
        val dialog_text = dialogView.findViewById<TextView>(R.id.dialog_text)
        dialog_text.text = resources.getString(R.string.do_you_want_to_delete_photo)
        alertDialog?.window?.setBackgroundDrawable(null)

        yesBtn.setOnClickListener {
            val oldImages = listOfImages.value
            alertDialog.dismiss()

            if (oldImages?.size == 1) {
                val ceibroImagesIntent = Intent()
                val newBundle = Bundle()
                newBundle.putParcelableArrayList("images", arrayListOf())
                ceibroImagesIntent.putExtras(newBundle)
                setResult(RESULT_OK, ceibroImagesIntent)
                finish()
            } else {
                oldImages?.removeAt(lastSelectedPosition)
                listOfImages.postValue(oldImages)
            }
        }

        cancelBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }
}