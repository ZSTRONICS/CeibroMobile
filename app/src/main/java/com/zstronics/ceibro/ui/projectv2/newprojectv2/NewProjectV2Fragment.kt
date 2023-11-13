package com.zstronics.ceibro.ui.projectv2.newprojectv2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentNewProjectV2Binding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class NewProjectV2Fragment :
    BaseNavViewModelFragment<FragmentNewProjectV2Binding, INewProjectV2.State, NewProjectV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: NewProjectV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_new_project_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.cl_newPhoto -> {
                showAddPhotoBottomSheet()

            }

            R.id.newPhoto -> {
                showAddPhotoBottomSheet()

            }

            R.id.closeBtn -> {
                navigateBack()
            }

            R.id.cancelBtn -> {
                navigateBack()
            }

            R.id.saveBtn -> {
                navigateBack()
            }

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    private fun showAddPhotoBottomSheet() {

        val coroutineScope = viewLifecycleOwner.lifecycleScope
        coroutineScope.launch(Dispatchers.Main) {

            val sheet = AddNewPhotoBottomSheet {

                showToast(it)
            }
            sheet.isCancelable = true
            sheet.setStyle(
                BottomSheetDialogFragment.STYLE_NORMAL,
                R.style.CustomBottomSheetDialogTheme
            );
            sheet.show(childFragmentManager, "AddPhotoBottomSheet")
        }


    }


}