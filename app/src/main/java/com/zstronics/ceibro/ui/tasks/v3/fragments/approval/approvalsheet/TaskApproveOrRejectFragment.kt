package com.zstronics.ceibro.ui.tasks.v3.fragments.approval.approvalsheet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.databinding.FragmentTaskApproveOrRejectBinding
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroFilesRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroImageWithCommentRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroOnlyImageRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment.EditCommentDialogSheet
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.CeibroCameraActivity
import ee.zstronics.ceibro.camera.CeibroImageViewerActivity
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.format
import id.zelory.compressor.constraint.quality
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class TaskApproveOrRejectFragment :
    BaseNavViewModelFragment<FragmentTaskApproveOrRejectBinding, ITaskApproveOrReject.State, TaskApproveOrRejectVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskApproveOrRejectVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_approve_or_reject
    override fun toolBarVisibility(): Boolean = false

    @androidx.annotation.OptIn(androidx.camera.core.ExperimentalZeroShutterLag::class)
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                navigateBack()
            }

            R.id.approveRejectPhotoBtn -> {
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

            R.id.approveRejectAttachBtn -> {
                if (viewState.isAttachLayoutOpen.value == true) {
                    viewState.isAttachLayoutOpen.value = false
                    mViewDataBinding.approveRejectAttachmentLayout.animate()
                        .translationY(mViewDataBinding.approveRejectAttachmentLayout.height.toFloat())
                        .setDuration(350)
                        .withEndAction {
                            mViewDataBinding.approveRejectAttachmentLayout.visibility = View.GONE
                        }
                        .start()
                } else {
                    viewState.isAttachLayoutOpen.value = true
                    mViewDataBinding.approveRejectAttachmentLayout.visibility = View.VISIBLE
                    mViewDataBinding.approveRejectAttachmentLayout.animate()
                        .translationY(0f)
                        .setDuration(350)
                        .start()
                }
            }

            R.id.approveRejectDocumentBtn -> {
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
//                        "application/vnd.android.package-archive",      //for APK file
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

            R.id.approveRejectLibraryBtn -> {
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
        }
    }

    @Inject
    lateinit var onlyImageAdapter: CeibroOnlyImageRVAdapter

    @Inject
    lateinit var imageWithCommentAdapter: CeibroImageWithCommentRVAdapter

    @Inject
    lateinit var filesAdapter: CeibroFilesRVAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.task.observe(viewLifecycleOwner) {
            viewModel.viewState.taskTitle.value = it.title
        }

        viewModel.taskType.observe(viewLifecycleOwner) { taskUpdateType ->
            if (taskUpdateType.equals("approveClose", true)) {
                viewModel.viewState.title.value = "Approve"
                mViewDataBinding.tvUpdateStatus.setTextColor(resources.getColor(R.color.appGreen))
                viewModel.viewState.description.value =
                    (resources.getString(R.string.approve_close_detail))
            } else if (taskUpdateType.equals("rejectReOpen", true)) {
                viewModel.viewState.title.value = "Reject-reopen"
                mViewDataBinding.tvUpdateStatus.setTextColor(resources.getColor(R.color.appRed))
                viewModel.viewState.description.value =
                    (resources.getString(R.string.reject_reopen_detail))
            } else if (taskUpdateType.equals("rejectClose", true)) {
                viewModel.viewState.title.value = "reject-Close"
                mViewDataBinding.tvUpdateStatus.setTextColor(resources.getColor(R.color.appRed))
                viewModel.viewState.description.value =
                    (resources.getString(R.string.reject_close_detail))
            }
        }



        mViewDataBinding.filesLayout.visibility = View.GONE
        mViewDataBinding.onlyImagesRV.visibility = View.GONE
        mViewDataBinding.imagesWithCommentRV.visibility = View.GONE

        mViewDataBinding.onlyImagesRV.isNestedScrollingEnabled = false
        mViewDataBinding.imagesWithCommentRV.isNestedScrollingEnabled = false
        mViewDataBinding.filesRV.isNestedScrollingEnabled = false


        val handler = Handler()
        handler.postDelayed(Runnable {
            mViewDataBinding.approveRejectAttachmentLayout.animate()
                .translationY(mViewDataBinding.approveRejectAttachmentLayout.height.toFloat())
                .setDuration(20)
                .withEndAction {
                    mViewDataBinding.approveRejectAttachmentLayout.visibility = View.GONE
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



        viewModel.listOfImages.observe(viewLifecycleOwner) {

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
                /*  val bundle = Bundle()
                  bundle.putParcelableArray(
                      "images",
                      viewModel.imagesWithComments.value?.toTypedArray()
                  )
                  bundle.putInt("position", position)
                  bundle.putBoolean("fromServerUrl", false)
                  navigate(R.id.imageViewerFragment, bundle)*/

                val newList: ArrayList<PickedImages> = arrayListOf()
                //  val listOfPickedImages = result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                val ceibroCamera = Intent(requireActivity(), CeibroImageViewerActivity::class.java)
                val bundle = Bundle()
                if (viewModel.listOfImages.value != null) {
                    newList.addAll(viewModel.listOfImages.value!!)
                }
                //  newList.addAll(oldImages)

                bundle.putParcelableArrayList("images", newList)
                bundle.putParcelable("object", data)
                bundle.putBoolean("isFromNewTaskFragment", true)
                ceibroCamera.putExtras(bundle)
                ceibroImageViewerLauncher.launch(ceibroCamera)

            }

        imageWithCommentAdapter.removeItemClickListener =
            {
                val listOfImages = viewModel.listOfImages.value
                if (listOfImages?.contains(it) == true) {
                    listOfImages.remove(it)
                    viewModel.listOfImages.postValue(listOfImages)
                }

                setImagesAndFilesHeadingVisibility()
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
            { _: View, position: Int, fileUri: String, obj ->
                /* val bundle = Bundle()
                 bundle.putParcelableArray("images", viewModel.onlyImages.value?.toTypedArray())
                 bundle.putInt("position", position)
                 bundle.putBoolean("fromServerUrl", false)
                 navigate(R.id.imageViewerFragment, bundle)*/

                val newList: ArrayList<PickedImages> = arrayListOf()
                //  val listOfPickedImages = result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                val ceibroCamera = Intent(requireActivity(), CeibroImageViewerActivity::class.java)
                val bundle = Bundle()
                if (viewModel.listOfImages.value != null) {
                    newList.addAll(viewModel.listOfImages.value!!)
                }
                //  newList.addAll(oldImages)

                bundle.putParcelableArrayList("images", newList)
                bundle.putParcelable("object", obj)
                bundle.putBoolean("isFromNewTaskFragment", true)
                ceibroCamera.putExtras(bundle)
                ceibroImageViewerLauncher.launch(ceibroCamera)
            }

        onlyImageAdapter.removeItemClickListener =
            {
                val listOfImages = viewModel.listOfImages.value
                if (listOfImages?.contains(it) == true) {
                    listOfImages.remove(it)
                    viewModel.listOfImages.postValue(listOfImages)
                }
                setImagesAndFilesHeadingVisibility()
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
            setImagesAndFilesHeadingVisibility()
        }

//        val handler1 = Handler()
//        handler1.postDelayed(Runnable {
//            mViewDataBinding.commentText.post {
//                mViewDataBinding.commentText.showKeyboardWithFocus()
//            }
//        }, 300)


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
                            if (fileExtension.isNullOrEmpty()) {
                                fileExtension = "jpg"
                            }
                            val newUri = createFileUriFromContentUri(
                                requireContext(),
                                fileUri,
                                fileName,
                                fileExtension!!
                            )
                            val file = FileUtils.getFile(requireContext(), newUri)
                            val selectedImgDetail =
                                getPickedFileDetail(requireContext(), newUri)
                            val foundImage =
                                oldImages?.find { oldImage -> oldImage.fileName == selectedImgDetail.fileName }
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
                            if (fileExtension.isNullOrEmpty()) {
                                fileExtension = "jpg"
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

                            val foundImage =
                                oldImages?.find { oldImage -> oldImage.fileName == selectedImgDetail.fileName }
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


    private val ceibroImageViewerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                var newList: ArrayList<PickedImages> = arrayListOf()
                val listOfPickedImages =
                    result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                if (listOfPickedImages?.isNotEmpty() == true) {
                    newList = listOfPickedImages
                }
                viewModel.listOfImages.postValue(newList)
            }
        }

    override fun onResume() {
        super.onResume()
        setImagesAndFilesHeadingVisibility()
    }

    private fun setImagesAndFilesHeadingVisibility() {

        Handler(Looper.getMainLooper()).postDelayed({
            if (viewModel.filesCounter() > 0) {
                mViewDataBinding.imageRequiredHeading.visibility = View.GONE
                mViewDataBinding.imageRequiredHeadingBottomLine.visibility = View.GONE
            } else {
                mViewDataBinding.imageRequiredHeading.visibility = View.VISIBLE
                mViewDataBinding.imageRequiredHeadingBottomLine.visibility = View.VISIBLE
            }
        }, 200)

    }
}