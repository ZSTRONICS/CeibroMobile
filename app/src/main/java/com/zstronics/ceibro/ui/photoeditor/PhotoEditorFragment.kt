package com.zstronics.ceibro.ui.photoeditor

import android.graphics.Typeface
import android.os.Bundle
import android.view.View
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentPhotoEditorBinding
import dagger.hilt.android.AndroidEntryPoint
import ja.burhanrashid52.photoeditor.PhotoEditor


@AndroidEntryPoint
class PhotoEditorFragment :
    BaseNavViewModelFragment<FragmentPhotoEditorBinding, IPhotoEditor.State, PhotoEditorVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: PhotoEditorVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_photo_editor
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.photoUri.observe(viewLifecycleOwner) {
            mViewDataBinding.photoEditorView.source.setImageURI(it)
        }
    }
}