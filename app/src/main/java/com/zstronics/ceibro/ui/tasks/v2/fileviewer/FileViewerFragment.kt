package com.zstronics.ceibro.ui.tasks.v2.fileviewer

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentFileViewerBinding
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.FileUtils
import ee.zstronics.ceibro.camera.PickedImages
import java.io.File


@AndroidEntryPoint
class FileViewerFragment :
    BaseNavViewModelFragment<FragmentFileViewerBinding, IFileViewer.State, FileViewerVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: FileViewerVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_file_viewer
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                mViewDataBinding.pdfView.recycle()
                navigateBack()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.fileData.observe(viewLifecycleOwner) {

            mViewDataBinding.fileName.text = it.fileName
            mViewDataBinding.pdfView.useBestQuality(true)
            val file = File(it.localUri)
            val fileUri = Uri.fromFile(file)
            val fileDetails = getPickedFileDetail(requireContext(), fileUri)
            if (fileDetails.attachmentType == AttachmentTypes.Pdf) {
                mViewDataBinding.pdfView.fromFile(file)
                    .defaultPage(0)
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .enableAntialiasing(true)
                    .enableAnnotationRendering(true)
                    .load()
            } else {
                shortToastNow("File format not supported yet.")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mViewDataBinding.pdfView.recycle()
    }

    private fun getPickedFileDetail(context: Context, fileUri: Uri?): PickedImages {
        val mimeType = FileUtils.getMimeType(context, fileUri)
        val fileName = FileUtils.getFileName(context, fileUri)
        val fileSize = FileUtils.getFileSizeInBytes(context, fileUri)
        val fileSizeReadAble = FileUtils.getReadableFileSize(fileSize)
        println("mimeTypeFound: ${mimeType} - ${fileName}")
        val attachmentType = when {
            mimeType == null -> {
                AttachmentTypes.Doc
            }

            mimeType == "application/pdf" -> {
                AttachmentTypes.Pdf
            }

            mimeType == "application/x-rar-compressed" || mimeType == "application/zip" -> {
                AttachmentTypes.Zip
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
                    ) ||
                    mimeType.equals(
                        "image/vnd.dwg",
                        true
                    ) ||
                    mimeType.equals(
                        "application/acad",
                        true
                    ) -> {
                AttachmentTypes.Doc
            }

            mimeType.contains("image/vnd") -> {
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