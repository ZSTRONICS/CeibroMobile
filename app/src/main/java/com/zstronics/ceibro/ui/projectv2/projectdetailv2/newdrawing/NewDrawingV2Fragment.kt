package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing

import android.os.Bundle
import android.text.Editable
import android.util.Size
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.PdfThumbnailGenerator
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentNewDrawingV2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class NewDrawingV2Fragment :
    BaseNavViewModelFragment<FragmentNewDrawingV2Binding, INewDrawingV2.State, NewDrawingV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: NewDrawingV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_new_drawing_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.floorText -> {

                addNewFloorBottomSheet {
                    mViewDataBinding.floorText.text = Editable.Factory.getInstance().newEditable(it)
                }

            }

            R.id.groupText -> {

                addNewGroupBottomSheet {

                    mViewDataBinding.groupText.text = Editable.Factory.getInstance().newEditable(it)
                }

            }

            R.id.closeBtn -> {
                navigateBack()
            }

            R.id.cancelBtn -> {
                navigateBack()
            }

            R.id.saveBtn -> {
                viewState.projectName.value?.let {
                    if (it.isEmpty()) {
                        showToast(getString(R.string.project_name_is_required))
                    } else {
                        viewModel.addNewProject(requireContext()) { isSuccess ->
                            if (isSuccess) {
                                navigateBack()
                            }
                        }
                    }
                }
            }

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.pdfFilePath.observe(viewLifecycleOwner) { filePath ->
            if (!filePath.isNullOrEmpty()) {
                val thumbnail =
                    PdfThumbnailGenerator().generateThumbnail(filePath, 0, Size(150, 300))
                mViewDataBinding.locationImg.setImageBitmap(thumbnail)
            }
        }
    }

    private fun addNewFloorBottomSheet(callback: (String) -> Unit) {

        val sheet = AddNewFloorBottomSheet {
            callback.invoke(it)
        }
        sheet.isCancelable = true
        sheet.show(childFragmentManager, "AddPhotoBottomSheet")
    }

    private fun addNewGroupBottomSheet(callback: (String) -> Unit) {

        val sheet = AddNewGroupBottomSheet {
            callback.invoke(it)
        }
        sheet.isCancelable = true
        sheet.show(childFragmentManager, "AddPhotoBottomSheet")
    }

}