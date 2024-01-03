package com.zstronics.ceibro.ui.tasks.v2.fileviewer

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentFileViewerBinding
import dagger.hilt.android.AndroidEntryPoint
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
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.fileData.observe(viewLifecycleOwner) {
            val file = File(it.localUri)
            mViewDataBinding.pdfView.fromFile(file)
                .defaultPage(0)
                .enableSwipe(true)
                .enableDoubletap(true)
                .enableAntialiasing(true)
                .load()
        }
    }
}