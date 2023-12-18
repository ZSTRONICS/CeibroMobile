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
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
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

                addNewFloorBottomSheet(viewModel) { floorName ->

                    viewModel.createFloorByProjectID(
                        viewModel.projectId.value.toString(),
                        floorName
                    )
                    mViewDataBinding.floorText.text =
                        Editable.Factory.getInstance().newEditable(floorName)
                }

            }

            R.id.groupText -> {

                addNewGroupBottomSheet(viewModel) { groupData ->
                    viewModel.selectedGroup = groupData
                    mViewDataBinding.groupText.text =
                        Editable.Factory.getInstance().newEditable(groupData.groupName)
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
    }

    private fun addNewFloorBottomSheet(model: NewDrawingV2VM, callback: (String) -> Unit) {

        val sheet = AddNewFloorBottomSheet(model) {
            callback.invoke(it)
        }

        sheet.deleteClickListener = { data ->
            showToast("Coming Soon")
        }
        sheet.isCancelable = true
        sheet.show(childFragmentManager, "AddPhotoBottomSheet")
    }

    private fun addNewGroupBottomSheet(
        model: NewDrawingV2VM,
        callback: (group: CeibroGroupsV2) -> Unit
    ) {
        val sheet = AddNewGroupBottomSheet(model) { groupData ->
            callback.invoke(groupData)
        }
        sheet.onAddGroup = {
            viewModel.createGroupByProjectTIDV2(viewModel.projectId.value.toString(), it) {
                sheet.groupAdapter.addItem(it)
                sheet.binding.tvNewGroup.text = Editable.Factory.getInstance().newEditable("")
                sheet.binding.addGroupBtn.text = "Save"
                sheet.binding.tvAddNewGroup.visibility = View.VISIBLE
                sheet.binding.clAddGroup.visibility = View.GONE
            }
        }

        sheet.onRenameGroup = { name, data ->
            viewModel.updateGroupByIDV2(groupId = data._id, groupName = name) { group ->
                sheet.groupAdapter.updateItem(group)
                sheet.binding.addGroupBtn.text = "Save"
                sheet.binding.tvAddNewGroup.visibility = View.VISIBLE
                sheet.binding.clAddGroup.visibility = View.GONE
            }
        }

        sheet.onDeleteGroup = { data ->
            viewModel.deleteGroupByID(groupId = data._id) {
                sheet.groupAdapter.deleteItem(data._id)
                if (!sheet.binding.addGroupBtn.text.toString().equals("Save", true)) {
                    sheet.binding.addGroupBtn.text = "Save"
                    sheet.binding.tvNewGroup.text = Editable.Factory.getInstance().newEditable("")
                    sheet.binding.tvAddNewGroup.visibility = View.VISIBLE
                    sheet.binding.clAddGroup.visibility = View.GONE
                }
            }
        }
        sheet.isCancelable = true
        sheet.show(childFragmentManager, "AddPhotoBottomSheet")
    }
}