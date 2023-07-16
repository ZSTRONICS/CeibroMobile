package com.zstronics.ceibro.ui.tasks.v2.taskdetail.comment

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.MotionEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.databinding.FragmentCommentBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroFilesRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroImageWithCommentRVAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.adapter.CeibroOnlyImageRVAdapter
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.CeibroCameraActivity
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

@AndroidEntryPoint
class CommentFragment :
    BaseNavViewModelFragment<FragmentCommentBinding, IComment.State, CommentVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: CommentVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_comment
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
            R.id.newCommentPhotoBtn -> {
                val ceibroCamera = Intent(
                    requireContext(),
                    CeibroCameraActivity::class.java
                )
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
//                        "image/vnd.adobe.photoshop", // Photoshop Document (PSD)
//                        "image/vnd.dwg" // AutoCAD Drawing Database (DWG)
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
                        "image/x-icon",
                        "image/svg+xml",
                        "image/tiff",
                        "image/*"
                    )
                )
            }
            R.id.nextBtn -> {
                if (viewModel.actionToPerform.equals(TaskDetailEvents.Comment.eventValue, true)) {
                    viewModel.uploadComment(
                        requireContext()
                    ) {
                        val bundle = Bundle()
                        bundle.putParcelable("taskData", viewModel.taskData)
                        navigateBackWithResult(Activity.RESULT_OK, bundle)
                    }
                } else if (viewModel.actionToPerform.equals(TaskDetailEvents.DoneTask.eventValue, true)) {
                    viewModel.doneTask(
                        requireContext()
                    ) {
                        val bundle = Bundle()
                        bundle.putParcelable("taskData", viewModel.taskData)
                        navigateBackWithResult(Activity.RESULT_OK, bundle)
                    }
                }
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
        mViewDataBinding.filesLayout.visibility = View.GONE
        mViewDataBinding.onlyImagesRV.visibility = View.GONE
        mViewDataBinding.imagesWithCommentRV.visibility = View.GONE

        mViewDataBinding.onlyImagesRV.isNestedScrollingEnabled = false
        mViewDataBinding.imagesWithCommentRV.isNestedScrollingEnabled = false
        mViewDataBinding.filesRV.isNestedScrollingEnabled = false

        val handler = Handler()
        handler.postDelayed(Runnable {
            mViewDataBinding.newCommentAttachmentLayout.animate()
                .translationY(mViewDataBinding.newCommentAttachmentLayout.height.toFloat())
                .setDuration(20)
                .withEndAction { mViewDataBinding.newCommentAttachmentLayout.visibility = View.GONE }
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
    }




    private val ceibroImagesPickerLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val images = result.data?.extras?.getParcelableArrayList<PickedImages>("images")
                if (images != null) {
                    val oldImages = viewModel.listOfImages.value
                    oldImages?.addAll(images)
                    viewModel.listOfImages.postValue(oldImages)
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
                if (clipData != null) {
                    for (i in 0 until clipData.itemCount) {
                        val fileUri = clipData.getItemAt(i).uri
                        val selectedImgDetail = getPickedFileDetail(requireContext(), fileUri)

                        if (oldImages?.contains(selectedImgDetail) == true) {
                            shortToastNow("You selected an already-added image")
                        } else {
                            pickedImage.add(selectedImgDetail)
                        }
                    }
                } else {
                    val fileUri = data.data
                    fileUri.let {
                        val selectedImgDetail = getPickedFileDetail(requireContext(), it)

                        if (oldImages?.contains(selectedImgDetail) == true) {
                            shortToastNow("You selected an already-added image")
                        } else {
                            pickedImage.add(selectedImgDetail)
                        }
                    }
                }
            }

            val allOldImages = viewModel.listOfImages.value
            allOldImages?.addAll(pickedImage)
            viewModel.listOfImages.postValue(allOldImages)
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


}