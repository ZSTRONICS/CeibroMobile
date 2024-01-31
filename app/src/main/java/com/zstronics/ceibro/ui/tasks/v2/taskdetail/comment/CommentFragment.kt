package com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment

import android.app.Activity
import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.finish
import com.zstronics.ceibro.base.extensions.launchActivityWithFinishAffinity
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.showKeyboardWithFocus
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.databinding.FragmentCommentBinding
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroFilesRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroImageWithCommentRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroOnlyImageRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.CeibroCameraActivity
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CommentFragment :
    BaseNavViewModelFragment<FragmentCommentBinding, IComment.State, CommentVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: CommentVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_comment
    override fun toolBarVisibility(): Boolean = false


    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalZeroShutterLag::class)
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                CeibroApplication.CookiesManager.taskIdInDetails = ""
                val instances = countActivitiesInBackStack(requireContext())
                if (viewModel.notificationTaskData.value != null) {
                    if (instances <= 1) {
                        launchActivityWithFinishAffinity<NavHostPresenterActivity>(
                            options = Bundle(),
                            clearPrevious = true
                        ) {
                            putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
                            putExtra(
                                NAVIGATION_Graph_START_DESTINATION_ID,
                                R.id.homeFragment
                            )
                        }
                    } else {
                        //finish is called so that second instance of app will be closed and only one last instance will remain
                        finish()
                    }
                } else {
                    navigateBack()
                }
            }

            R.id.newCommentPhotoBtn -> {
                val listOfPickedImages = arrayListOf<PickedImages>()
                viewModel.listOfImages.value?.let { listOfPickedImages.addAll(it) }
                val bundle = Bundle()
                bundle.putParcelableArrayList("allImagesList", listOfPickedImages)
                val ceibroCamera = Intent(
                    requireContext(),
                    CeibroCameraActivity::class.java
                )
                ceibroCamera.putExtra("allImagesBundle", bundle)
                ceibroImagesPickerLauncher.launch(ceibroCamera)
            }

            R.id.imageRequiredPhotoBtn -> {
                val listOfPickedImages = arrayListOf<PickedImages>()
                viewModel.listOfImages.value?.let { listOfPickedImages.addAll(it) }
                val bundle = Bundle()
                bundle.putParcelableArrayList("allImagesList", listOfPickedImages)
                val ceibroCamera = Intent(
                    requireContext(),
                    CeibroCameraActivity::class.java
                )
                ceibroCamera.putExtra("allImagesBundle", bundle)
                ceibroImagesPickerLauncher.launch(ceibroCamera)
            }

            R.id.filesHeaderLayout -> {
                if (mViewDataBinding.filesRV.visibility == View.VISIBLE) {
                    mViewDataBinding.filesRV.visibility = View.GONE
                    mViewDataBinding.filesDownUpIcon.setImageResource(R.drawable.icon_navigate_down)
                } else {
                    mViewDataBinding.filesRV.visibility = View.VISIBLE
                    mViewDataBinding.filesDownUpIcon.setImageResource(R.drawable.icon_navigate_up)
                }
            }

            R.id.newCommentAttachBtn -> {
                if (viewState.isAttachLayoutOpen.value == true) {
                    viewState.isAttachLayoutOpen.value = false
                    mViewDataBinding.newCommentAttachmentLayout.animate()
                        .translationY(mViewDataBinding.newCommentAttachmentLayout.height.toFloat())
                        .setDuration(350)
                        .withEndAction {
                            mViewDataBinding.newCommentAttachmentLayout.visibility = View.GONE
                        }
                        .start()
                } else {
                    viewState.isAttachLayoutOpen.value = true
                    mViewDataBinding.newCommentAttachmentLayout.visibility = View.VISIBLE
                    mViewDataBinding.newCommentAttachmentLayout.animate()
                        .translationY(0f)
                        .setDuration(350)
                        .start()
                }
            }

            R.id.newCommentDocumentBtn -> {
                chooseDocuments(
                    mimeTypes = arrayOf(
                        "text/plain",
                        "text/csv",
                        "application/pdf",
                        "application/rtf",
                        "application/zip",
                        "application/vnd.oasis.opendocument.text",                                  // .odt
                        "application/vnd.oasis.opendocument.spreadsheet",                           // .ods
                        "application/vnd.oasis.opendocument.presentation",                          // .odp
                        "application/x-rar-compressed",
                        "application/vnd.android.package-archive",      //for APK file
                        "application/msword",
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",  // .docx
                        "application/vnd.ms-word.document.macroEnabled.12",                         // .doc
                        "application/vnd.ms-excel",
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",        // .xlsx
                        "application/vnd.ms-excel.sheet.macroEnabled.12",                           // .xls
                        "application/vnd.ms-powerpoint",
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation", // .pptx
                        "application/vnd.ms-powerpoint.presentation.macroEnabled.12"                 // .ppt
//                        "image/vnd.dwg",    // AutoCAD Drawing Database (DWG)
//                        "application/acad"  // AutoCAD Drawing
//                        "image/vnd.adobe.photoshop", // Photoshop Document (PSD)
                    )
                )
            }

            R.id.newCommentLibraryBtn -> {
                chooseImages(
                    mimeTypes = arrayOf(
                        "image/jpeg",
                        "image/jpg",
                        "image/png",
                        "image/gif",
                        "image/webp",
                        "image/bmp",
                        "image/*"
                    )
                )
            }

            R.id.nextBtn -> {
                if (viewModel.actionToPerform.value.equals(
                        TaskDetailEvents.Comment.eventValue,
                        true
                    )
                ) {
                    viewModel.uploadComment(
                        requireContext()
                    ) { eventData ->
                        if (viewModel.notificationTaskData.value != null) {
                            CeibroApplication.CookiesManager.taskIdInDetails = ""
                            shortToastNow(getString(R.string.commented_successfully))
                            val instances = countActivitiesInBackStack(requireContext())
                            if (instances <= 1) {
                                launchActivityWithFinishAffinity<NavHostPresenterActivity>(
                                    options = Bundle(),
                                    clearPrevious = true
                                ) {
                                    putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
                                    putExtra(
                                        NAVIGATION_Graph_START_DESTINATION_ID,
                                        R.id.homeFragment
                                    )
                                }
                            } else {
                                //finish is called so that second instance of app will be closed and only one last instance will remain
                                finish()
                            }
                        } else {
                            CeibroApplication.CookiesManager.taskIdInDetails = ""
                            val bundle = Bundle()
                            bundle.putParcelable("eventData", null)
                            navigateBackWithResult(Activity.RESULT_OK, bundle)
                        }
                    }
                } else if (viewModel.actionToPerform.value.equals(
                        TaskDetailEvents.DoneTask.eventValue,
                        true
                    )
                ) {
                    viewModel.doneTask(
                        requireContext()
                    ) { eventData,isBeingDone ->
                        CeibroApplication.CookiesManager.taskIdInDetails = ""
                        val bundle = Bundle()
                        bundle.putParcelable("eventData", eventData)
                        bundle.putBoolean("isBeingDone", isBeingDone)
                        navigateBackWithResult(Activity.RESULT_OK, bundle)
                    }
                }
            }
        }
    }

    //This function is called when fragment is closed and detach from activity
    override fun onDetach() {
        CeibroApplication.CookiesManager.taskIdInDetails = ""
        super.onDetach()
    }

    @Inject
    lateinit var onlyImageAdapter: CeibroOnlyImageRVAdapter

    @Inject
    lateinit var imageWithCommentAdapter: CeibroImageWithCommentRVAdapter

    @Inject
    lateinit var filesAdapter: CeibroFilesRVAdapter

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            CeibroApplication.CookiesManager.taskIdInDetails = ""
            val instances = countActivitiesInBackStack(requireContext())
            if (instances <= 1) {
                launchActivityWithFinishAffinity<NavHostPresenterActivity>(
                    options = Bundle(),
                    clearPrevious = true
                ) {
                    putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
                    putExtra(
                        NAVIGATION_Graph_START_DESTINATION_ID,
                        R.id.homeFragment
                    )
                }
            } else {
                //finish is called so that second instance of app will be closed and only one last instance will remain
                finish()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.filesLayout.visibility = View.GONE
        mViewDataBinding.onlyImagesRV.visibility = View.GONE
        mViewDataBinding.imagesWithCommentRV.visibility = View.GONE

        mViewDataBinding.onlyImagesRV.isNestedScrollingEnabled = false
        mViewDataBinding.imagesWithCommentRV.isNestedScrollingEnabled = false
        mViewDataBinding.filesRV.isNestedScrollingEnabled = false

        viewModel.notificationTaskData.observe(viewLifecycleOwner) { notificationData ->
            if (notificationData != null) {
                requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
            }
        }

        val handler = Handler()
        handler.postDelayed(Runnable {
            mViewDataBinding.newCommentAttachmentLayout.animate()
                .translationY(mViewDataBinding.newCommentAttachmentLayout.height.toFloat())
                .setDuration(20)
                .withEndAction {
                    mViewDataBinding.newCommentAttachmentLayout.visibility = View.GONE
                }
                .start()
        }, 20)

        mViewDataBinding.commentText.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener false
        }

        viewModel.actionToPerform.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                if (it.equals(TaskDetailEvents.Comment.eventValue, true)) {
                    mViewDataBinding.commentHeading.text =
                        resources.getString(R.string.reply_heading)
                    mViewDataBinding.commentRequiredHeading.visibility = View.GONE
                    mViewDataBinding.imageRequiredHeading.visibility = View.GONE
                    mViewDataBinding.imageRequiredPhotoBtn.visibility = View.GONE
                    mViewDataBinding.imageRequiredBottomLine.visibility = View.GONE

                } else if (it.equals(TaskDetailEvents.DoneTask.eventValue, true)
                ) {
                    mViewDataBinding.commentHeading.text =
                        resources.getString(R.string.done_requirements_heading)
                    if (viewModel.doneCommentsRequired) {
                        mViewDataBinding.commentRequiredHeading.visibility = View.VISIBLE
                    }
                    if (viewModel.doneImageRequired) {
                        mViewDataBinding.imageRequiredHeading.visibility = View.VISIBLE
                        mViewDataBinding.imageRequiredPhotoBtn.visibility = View.VISIBLE
                        mViewDataBinding.imageRequiredBottomLine.visibility = View.VISIBLE
                    }

                }
            }
        }

        viewState.comment.observe(viewLifecycleOwner) {
            if (viewModel.actionToPerform.value.equals(
                    TaskDetailEvents.DoneTask.eventValue,
                    true
                )
            ) {
                if (!it.isNullOrEmpty()) {
                    mViewDataBinding.commentRequiredHeading.visibility = View.GONE
                } else {
                    if (viewModel.doneCommentsRequired) {
                        mViewDataBinding.commentRequiredHeading.visibility = View.VISIBLE
                    } else {
                        mViewDataBinding.commentRequiredHeading.visibility = View.GONE
                    }
                }
            }
        }

        viewModel.listOfImages.observe(viewLifecycleOwner) {
            if (viewModel.actionToPerform.value.equals(
                    TaskDetailEvents.DoneTask.eventValue,
                    true
                )
            ) {
                if (!it.isNullOrEmpty()) {
                    mViewDataBinding.imageRequiredHeading.visibility = View.GONE
                    mViewDataBinding.imageRequiredPhotoBtn.visibility = View.GONE
                    mViewDataBinding.imageRequiredBottomLine.visibility = View.GONE
                } else {
                    if (viewModel.doneImageRequired) {
                        mViewDataBinding.imageRequiredHeading.visibility = View.VISIBLE
                        mViewDataBinding.imageRequiredPhotoBtn.visibility = View.VISIBLE
                        mViewDataBinding.imageRequiredBottomLine.visibility = View.VISIBLE
                    } else {
                        mViewDataBinding.imageRequiredHeading.visibility = View.GONE
                        mViewDataBinding.imageRequiredPhotoBtn.visibility = View.GONE
                        mViewDataBinding.imageRequiredBottomLine.visibility = View.GONE
                    }
                }
            }
            if (!it.isNullOrEmpty()) {
                val allImages = it
                println("ImagesURISelected: $it")
                val onlyImages1 = arrayListOf<PickedImages>()
                val imagesWithComment1 = arrayListOf<PickedImages>()
                for (item in allImages) {
                    if (item.comment.isNotEmpty()) {
                        imagesWithComment1.add(item)
                    } else {
                        onlyImages1.add(item)
                    }
                }
                viewModel.onlyImages.postValue(onlyImages1)
                viewModel.imagesWithComments.postValue(imagesWithComment1)
            } else {
                viewModel.onlyImages.postValue(arrayListOf())
                viewModel.imagesWithComments.postValue(arrayListOf())
            }
        }

        viewModel.imagesWithComments.observe(viewLifecycleOwner) {
            imageWithCommentAdapter.setList(it)
            if (it.isNotEmpty()) {
                mViewDataBinding.imagesWithCommentRV.visibility = View.VISIBLE
                mViewDataBinding.imagesWithCommentBottomLine.visibility = View.VISIBLE
            } else {
                mViewDataBinding.imagesWithCommentRV.visibility = View.GONE
                mViewDataBinding.imagesWithCommentBottomLine.visibility = View.GONE
            }
        }
        mViewDataBinding.imagesWithCommentRV.adapter = imageWithCommentAdapter
        imageWithCommentAdapter.textClickListener = { _: View, position: Int, data: PickedImages ->
            showEditCommentDialog(data)
        }
        imageWithCommentAdapter.openImageClickListener =
            { _: View, position: Int, data: PickedImages ->
                val bundle = Bundle()
                bundle.putParcelableArray(
                    "images",
                    viewModel.imagesWithComments.value?.toTypedArray()
                )
                bundle.putInt("position", position)
                bundle.putBoolean("fromServerUrl", false)
                navigate(R.id.imageViewerFragment, bundle)
            }


        viewModel.onlyImages.observe(viewLifecycleOwner) {
            onlyImageAdapter.setList(it)
            if (it.isNotEmpty()) {
                mViewDataBinding.onlyImagesRV.visibility = View.VISIBLE
                mViewDataBinding.onlyImagesBottomLine.visibility = View.VISIBLE
            } else {
                mViewDataBinding.onlyImagesRV.visibility = View.GONE
                mViewDataBinding.onlyImagesBottomLine.visibility = View.GONE
            }
        }
        mViewDataBinding.onlyImagesRV.adapter = onlyImageAdapter
        onlyImageAdapter.openImageClickListener =
            { _: View, position: Int, fileUri: String,obj ->
                val bundle = Bundle()
                bundle.putParcelableArray("images", viewModel.onlyImages.value?.toTypedArray())
                bundle.putInt("position", position)
                bundle.putBoolean("fromServerUrl", false)
                navigate(R.id.imageViewerFragment, bundle)
            }


        viewModel.documents.observe(viewLifecycleOwner) {
            filesAdapter.setList(it)
            if (it.isNotEmpty()) {
                mViewDataBinding.filesLayout.visibility = View.VISIBLE
                mViewDataBinding.filesBottomLine.visibility = View.VISIBLE
            } else {
                mViewDataBinding.filesLayout.visibility = View.GONE
                mViewDataBinding.filesBottomLine.visibility = View.GONE
            }
            mViewDataBinding.filesCount.text = "${it.size} file(s)"
        }
        mViewDataBinding.filesRV.adapter = filesAdapter

        filesAdapter.itemClickListener = { _: View, position: Int, data: PickedImages ->
            val oldDocuments = viewModel.documents.value
            oldDocuments?.remove(data)
            viewModel.documents.postValue(oldDocuments)
        }

        val handler1 = Handler()
        handler1.postDelayed(Runnable {
            mViewDataBinding.commentText.post {
                mViewDataBinding.commentText.showKeyboardWithFocus()
            }
        }, 300)

    }

    private fun showEditCommentDialog(data: PickedImages) {
        val sheet = EditCommentDialogSheet(data)
        sheet.updateCommentOnClick = { updatedComment ->
            val allImagesWithComment = viewModel.imagesWithComments.value
            val foundData = allImagesWithComment?.find { it.fileUri == data.fileUri }
            if (foundData != null) {
                val index = allImagesWithComment.indexOf(foundData)
                foundData.comment = updatedComment
                allImagesWithComment[index] = foundData
                viewModel.imagesWithComments.postValue(allImagesWithComment)
            }
        }

        sheet.isCancelable = false
        sheet.show(childFragmentManager, "EditCommentDialogSheet")
    }

    private val ceibroImagesPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val images = result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                if (images != null) {
//                    val oldImages = viewModel.listOfImages.value
//                    oldImages?.addAll(images)
                    viewModel.listOfImages.postValue(images)
                } else {
                    viewModel.listOfImages.postValue(arrayListOf())
                }
            }
        }


    private fun chooseImages(mimeTypes: Array<String>) {
        requireActivity().openFilePicker(
            mimeTypes = mimeTypes,
            allowMultiple = true
        ) { resultCode, data ->
            val pickedImage = arrayListOf<PickedImages>()
            val oldImages = viewModel.onlyImages.value

            if (resultCode == Activity.RESULT_OK && data != null) {
                val clipData = data.clipData
                GlobalScope.launch {
                    viewModel.loading(true)
                    if (clipData != null) {
                        for (i in 0 until clipData.itemCount) {
                            val fileUri = clipData.getItemAt(i).uri
                            val fileName = getFileNameFromUri(requireContext(), fileUri)
                            var fileExtension = getFileExtension(requireContext(), fileUri)
                            if (fileExtension.isNullOrEmpty()){
                                fileExtension="jpg"
                            }
                            val newUri = createFileUriFromContentUri(requireContext(), fileUri, fileName,fileExtension!!)
                            val file = FileUtils.getFile(requireContext(), newUri)
                            val selectedImgDetail =
                                getPickedFileDetail(requireContext(), newUri)
                            val foundImage = oldImages?.find {oldImage -> oldImage.fileName == selectedImgDetail.fileName}
                            if (foundImage != null) {
                                viewModel.launch(Dispatcher.Main) {
                                    shortToastNow("You selected an already-added image")
                                }
                            } else {
                                val compressedImageFile =
                                    Compressor.compress(requireContext(), file) {
                                        quality(80)
                                        format(Bitmap.CompressFormat.JPEG)
                                    }
                                val compressedImageUri = Uri.fromFile(compressedImageFile)

                                if (compressedImageUri != null) {
                                    val selectedNewImgDetail =
                                        getPickedFileDetail(requireContext(), compressedImageUri)

                                    pickedImage.add(selectedNewImgDetail)
                                }
                            }
                        }
                    } else {
                        val fileUri = data.data
                        fileUri?.let {
                            val fileName = getFileNameFromUri(requireContext(), it)
                            var fileExtension = getFileExtension(requireContext(), fileUri)
                            if (fileExtension.isNullOrEmpty()){
                                fileExtension="jpg"
                            }
                            val newUri = createFileUriFromContentUri(
                                requireContext(),
                                it,
                                fileName,
                                fileExtension!!
                            )
                            val file = FileUtils.getFile(requireContext(), newUri)
                            val selectedImgDetail =
                                getPickedFileDetail(requireContext(), newUri)

                            val foundImage = oldImages?.find {oldImage -> oldImage.fileName == selectedImgDetail.fileName}
                            if (foundImage != null) {
                                viewModel.launch(Dispatcher.Main) {
                                    shortToastNow("You selected an already-added image")
                                }
                            } else {
                                val compressedImageFile =
                                    Compressor.compress(requireContext(), file) {
                                        quality(80)
                                        format(Bitmap.CompressFormat.JPEG)
                                    }
                                val compressedImageUri = Uri.fromFile(compressedImageFile)

                                if (compressedImageUri != null) {
                                    val selectedNewImgDetail =
                                        getPickedFileDetail(requireContext(), compressedImageUri)

                                    pickedImage.add(selectedNewImgDetail)
                                }
                            }
                        }
                    }

                    val allOldImages = viewModel.listOfImages.value
                    allOldImages?.addAll(pickedImage)
                    viewModel.listOfImages.postValue(allOldImages)
                    viewModel.loading(false, "")
                }
            }
        }
    }

    private fun chooseDocuments(mimeTypes: Array<String>) {
        requireActivity().openFilePicker(
            mimeTypes = mimeTypes,
            allowMultiple = true
        ) { resultCode, data ->
            val pickedDocuments = arrayListOf<PickedImages>()
            val oldDocuments = viewModel.documents.value

            if (resultCode == Activity.RESULT_OK && data != null) {
                val clipData = data.clipData
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val fileUri = clipData.getItemAt(i).uri
                        val selectedDocDetail = getPickedFileDetail(requireContext(), fileUri)

                        if (oldDocuments?.contains(selectedDocDetail) == true) {
                            shortToastNow("You selected an already-added document")
                        } else {
                            pickedDocuments.add(selectedDocDetail)
                        }
                    }
                } else {
                    val fileUri = data.data
                    fileUri.let {
                        val selectedDocDetail = getPickedFileDetail(requireContext(), it)

                        if (oldDocuments?.contains(selectedDocDetail) == true) {
                            shortToastNow("You selected an already-added document")
                        } else {
                            pickedDocuments.add(selectedDocDetail)
                        }
                    }
                }
            }

            oldDocuments?.addAll(pickedDocuments)
            viewModel.documents.postValue(oldDocuments)
        }
    }

    private fun getPickedFileDetail(context: Context, fileUri: Uri?): PickedImages {
        val mimeType = FileUtils.getMimeType(context, fileUri)
        val fileName = FileUtils.getFileName(context, fileUri)
        val fileSize = FileUtils.getFileSizeInBytes(context, fileUri)
        val fileSizeReadAble = FileUtils.getReadableFileSize(fileSize)
        val attachmentType = when {
            mimeType == null -> {
                AttachmentTypes.UnKnown
            }

            mimeType == "application/pdf" -> {
                AttachmentTypes.Pdf
            }

            mimeType.equals("text/plain", true) ||
                    mimeType.equals("text/csv", true) ||
                    mimeType.equals("application/rtf", true) ||
                    mimeType.equals("application/zip", true) ||
                    mimeType.equals("application/x-rar-compressed", true) ||
                    mimeType.equals("application/vnd.oasis.opendocument.text", true) ||
                    mimeType.equals("application/vnd.oasis.opendocument.spreadsheet", true) ||
                    mimeType.equals("application/vnd.oasis.opendocument.presentation", true) ||
                    mimeType.equals("application/vnd.android.package-archive", true) ||
                    mimeType.equals("application/msword", true) ||
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                        true
                    ) ||
                    mimeType.equals(
                        "application/vnd.ms-word.document.macroEnabled.12",
                        true
                    ) ||
                    mimeType.equals("application/vnd.ms-excel", true) ||
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                        true
                    ) ||
                    mimeType.equals(
                        "application/vnd.ms-excel.sheet.macroEnabled.12",
                        true
                    ) ||
                    mimeType.equals("application/vnd.ms-powerpoint", true) ||
                    mimeType.equals(
                        "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                        true
                    ) ||
                    mimeType.equals(
                        "application/vnd.ms-powerpoint.presentation.macroEnabled.12",
                        true
                    ) -> {
                AttachmentTypes.Doc
            }

            mimeType.startsWith("image") -> {
                AttachmentTypes.Image
            }

            mimeType.startsWith("video") -> {
                AttachmentTypes.Video
            }

            else -> AttachmentTypes.Doc
        }
        return PickedImages(
            fileUri = fileUri,
            attachmentType = attachmentType,
            fileName = fileName,
            fileSizeReadAble = fileSizeReadAble,
            file = FileUtils.getFile(requireContext(), fileUri)
        )
    }

    private fun countActivitiesInBackStack(context: Context): Int {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val runningTasks = activityManager.appTasks
        var activityCount = 0

        for (task in runningTasks) {
            val taskInfo = task.taskInfo
            activityCount += taskInfo.numActivities
        }

        return activityCount
    }
}