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
import com.zstronics.ceibro.data.repos.projects.group.GroupResponseV2
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

                addNewGroupBottomSheet(viewModel) {

                    mViewDataBinding.groupText.text =
                        Editable.Factory.getInstance().newEditable(it.groupName)
                }

            }

            R.id.closeBtn -> {
                navigateBack()
            }

            R.id.cancelBtn -> {
                navigateBack()
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


        //  viewModel.createFloorByProjectTID(viewModel.projectId.value.toString(), "15")
        viewModel.getFloorsByProjectTID(viewModel.projectId.value.toString())
        //   viewModel.createGroupByProjectTIDV2(viewModel.projectId.value.toString(), "Mughal")
        viewModel.getGroupsByProjectTID(viewModel.projectId.value.toString())
        //  viewModel.deleteGroupByID(viewModel.projectId.value.toString())
        //  viewModel.updateGroupByIDV2(viewModel.projectId.value.toString(),"Mughal")

        // viewModel.getFloorsByProjectTid("657ac771753eb1365aef682a")
    }

    private fun addNewFloorBottomSheet(callback: (String) -> Unit) {

        val sheet = AddNewFloorBottomSheet {
            callback.invoke(it)
        }
        sheet.isCancelable = true
        sheet.show(childFragmentManager, "AddPhotoBottomSheet")
    }

    private fun addNewGroupBottomSheet(model: NewDrawingV2VM, callback: (GroupResponseV2) -> Unit) {

        val sheet = AddNewGroupBottomSheet(model) {
            callback.invoke(it)
        }
        sheet.onAddGroup = {
            viewModel.createGroupByProjectTIDV2(viewModel.projectId.value.toString(), it) {
                sheet.groupAdapter.addiItem(it)
            }
        }

        sheet.onRenameGroup = { name,data->
            data.Id?.let {
                viewModel.updateGroupByIDV2(groupName = name, groupId = it) {
                   viewModel.groupList.value?.let {
                       sheet.groupAdapter.setList(it)
                       sheet.binding.addGroupBtn.text="Save"
                   }
                }
            }
        }

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "AddPhotoBottomSheet")
    }

}