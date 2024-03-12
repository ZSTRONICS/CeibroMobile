package com.zstronics.ceibro.ui.tasks.v3.fragments.approval.approvalsheet

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.viewmodel.Dispatcher
import com.zstronics.ceibro.databinding.FragmentTaskApproveOrRejectBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import com.zstronics.ceibro.extensions.openFilePicker
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
}