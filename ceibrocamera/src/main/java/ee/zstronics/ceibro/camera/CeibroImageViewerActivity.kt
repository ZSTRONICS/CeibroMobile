package ee.zstronics.ceibro.camera

import android.Manifest
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.viewpager2.widget.ViewPager2
import ee.zstronics.ceibro.camera.databinding.ActivityCeibroImageViewerBinding
import java.io.File

class CeibroImageViewerActivity : BaseActivity() {
    lateinit var binding: ActivityCeibroImageViewerBinding
    val listOfImages: MutableLiveData<ArrayList<PickedImages>> = MutableLiveData()
    var isBottomImageLayoutVisible = true
    var fullImageAdapter: CeibroFullImageVPAdapter = CeibroFullImageVPAdapter()
    var smallImageAdapter: CeibroSmallImageRVAdapter = CeibroSmallImageRVAdapter()
    var lastSelectedPosition: Int = 0
    var newImagesAdded: Boolean = false
    var oldListIndexesSize: Int = 0         //this will be index count, considering from 0
    var imagesOnceSet = false
    var imageDeleted = false

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalZeroShutterLag::class)
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
//        images?.map { println("ImagesURI: ${it.fileUri}") }
        listOfImages.value = images


        listOfImages.observe(this) {
            it.map { println("ImagesURI: ${it.fileUri}") }
            fullImageAdapter.setList(it)
            smallImageAdapter.setList(it)
            try {
                binding.fullSizeImagesVP.offscreenPageLimit =
                    fullImageAdapter.itemCount     //this is set bcz sometimes when image loaded from gallery again, it doesn't show
            } catch (e: Exception) {
                println(e.toString())
            }
            if (newImagesAdded) {
                val newItemPosition = 0
                binding.fullSizeImagesVP.setCurrentItem(newItemPosition, true)
                binding.smallFooterImagesRV.smoothScrollToPosition(newItemPosition)
                smallImageAdapter.setSelectedItem(newItemPosition)
                newImagesAdded = false
            }
            if (it.isNotEmpty()) {
                if (!imagesOnceSet) {
                    setUserCommentLogic(0)
                    imagesOnceSet = true
                }
            }
        }
        binding.fullSizeImagesVP.adapter = fullImageAdapter
        binding.smallFooterImagesRV.adapter = smallImageAdapter

        smallImageAdapter.itemClickListener =
            { _: View, position: Int ->
                saveComment()
                binding.fullSizeImagesVP.setCurrentItem(position, true)
                setUserCommentLogic(position)
                hideKeyboard()
                binding.commentsField.clearFocus()
                lastSelectedPosition = position
            }
        binding.fullSizeImagesVP.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                if (!imageDeleted) {     //registerOnPageChangeCallback is called even when we delete photo because image view pager is scrolled to next image
                    saveComment()       //this is done because when image is deleted, its comment shifts to next image, in order to stop it, dont save on delete
                }
                imageDeleted = false
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
                binding.okBtn.visibility = View.VISIBLE
                binding.saveBtn.visibility = View.INVISIBLE
                binding.commentsField.text?.length?.let { it1 -> binding.commentsField.setSelection(it1) }
            } else {
                binding.okBtn.visibility = View.INVISIBLE
                binding.saveBtn.visibility = View.VISIBLE
            }
        }

        binding.okBtn.setOnClickListener {
            saveComment()
        }


        binding.closeBtn.setOnClickListener {
            showCancelDialog()
        }

        binding.deleteBtn.setOnClickListener {
            showDeleteDialog()
        }

        binding.saveBtn.setOnClickListener {
            listOfImages.value?.let { listOfPickedImages ->
                println("ImagesURIOnDone: ${listOfPickedImages}")
                val newList = listOfPickedImages.map { it ->
                    it.apply {
                        this.file = FileUtils.getFile(
                            applicationContext,
                            it.fileUri
                        )
                    }
                } as ArrayList<PickedImages>
                println("ImagesURIOnDoneNew: ${newList}")
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
            saveComment()
            checkPermission(
                listOf(
                    Manifest.permission.CAMERA,
                )
            ) {
                pickFiles { listOfPickedImages ->
                    val newList: java.util.ArrayList<PickedImages> = arrayListOf()
                    val oldImages = listOfImages.value

                    oldImages?.map {oldImage ->
                        val foundImage = listOfPickedImages.find { it.fileName == oldImage.fileName }
                        if (foundImage != null) {
                            val index = listOfPickedImages.indexOf(foundImage)
                            listOfPickedImages.removeAt(index)
                            cancelAndMakeToast(this, "Removed duplicate images", Toast.LENGTH_SHORT)
                        }
                    }

                    if (listOfPickedImages.size > 0) {
                        oldListIndexesSize = oldImages?.size?.minus(1) ?: 0
                        newImagesAdded = true
                        newList.addAll(listOfPickedImages)
                    }
                    oldImages?.let { newList.addAll(it) }

                    listOfImages.postValue(newList)
                }
            }
        }
        binding.cameraBtn.setOnClickListener {
            saveComment()
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

        binding.squareBtn.setOnClickListener {
            openEditor("square")
        }
        binding.arrowBtn.setOnClickListener {
            openEditor("arrow")
        }
        binding.drawBtn.setOnClickListener {
            openEditor("draw")
        }
        binding.insertTextBtn.setOnClickListener {
            openEditor("insertText")
        }
        binding.undoBtn.setOnClickListener {
            openEditor("undo")
        }
    }

    private val textWatcher = object : TextWatcher {
        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
        }

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            if (s.toString().isNotEmpty()) {
                binding.okBtn.visibility = View.VISIBLE
                binding.saveBtn.visibility = View.INVISIBLE
            }
        }

        override fun afterTextChanged(s: Editable?) {
        }
    }

    private fun openEditor(editType: String) {
        saveComment()
        listOfImages.value?.let {
            it[lastSelectedPosition].fileUri?.let { uri ->
//                    println("ImagesURIBeforeEdit: ${uri}")
                startEditor(uri, editType) { updatedUri ->
                    if (updatedUri != null) {
                        var updatedPickedImage = getPickedImage(updatedUri)
//                            println("ImagesURIAfterEdit1: ${updatedUri}")

                        if (updatedUri.toString().contains("content://media/")) {
                            try {
                                val contentResolver = applicationContext.contentResolver
                                val projection = arrayOf(MediaStore.Images.Media.DATA)
                                val cursor = contentResolver.query(
                                    updatedUri,
                                    projection,
                                    null,
                                    null,
                                    null
                                )
                                val filePath: String? = cursor?.use { cursor1 ->
                                    if (cursor1.moveToFirst()) {
                                        cursor1.getString(cursor1.getColumnIndexOrThrow(MediaStore.Images.Media.DATA))
                                    } else {
                                        null
                                    }
                                }
                                val file = filePath?.let { File(it) }
                                val uri1 = Uri.fromFile(file)
                                updatedPickedImage = getPickedImage(uri1)
//                                    println("ImagesURIAfterEdit222: $updatedPickedImage")
                            } catch (_: Exception) {
                            }
                        }

                        val files = listOfImages.value

                        if (files != null) {
                            val singleFile = files.get(lastSelectedPosition)
                            updatedPickedImage.comment = singleFile.comment

                            files.removeAt(lastSelectedPosition)
                            files.add(
                                lastSelectedPosition,
                                updatedPickedImage
                            )    //added after old file removal because file name and URI, everything is changed
                        }
                        listOfImages.postValue(files)
                    }
                }
            }
        }
    }

    private fun saveComment(){
        val comment = binding.commentsField.text.toString().trim()
        val oldImages = listOfImages.value
        oldImages?.get(lastSelectedPosition)?.comment = comment
        listOfImages.postValue(oldImages)

        binding.okBtn.visibility = View.INVISIBLE
        binding.saveBtn.visibility = View.VISIBLE

        binding.commentsField.clearFocus()
        hideKeyboard()
    }

    private fun setUserCommentLogic(position: Int) {
        val userComment = listOfImages.value?.get(position)?.comment
        binding.commentsField.setText(userComment)
        binding.okBtn.visibility = View.INVISIBLE
        binding.saveBtn.visibility = View.VISIBLE
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
                val newList: java.util.ArrayList<PickedImages> = arrayListOf()
                val listOfPickedImages =
                    result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                        ?: arrayListOf()
                val oldImages = listOfImages.value
                if (listOfPickedImages.size > 0) {
                    oldListIndexesSize = oldImages?.size?.minus(1) ?: 0
                    newImagesAdded = true
                    newList.addAll(listOfPickedImages)
                }
                oldImages?.let { newList.addAll(it) }

                listOfImages.postValue(newList)
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
            imageDeleted = true

            if (oldImages?.size == 1) {
                val ceibroImagesIntent = Intent()
                val newBundle = Bundle()
                newBundle.putParcelableArrayList("images", arrayListOf<PickedImages>())
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