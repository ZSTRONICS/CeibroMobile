package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing


import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.extensions.showKeyboard
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.databinding.FragmentAddNewGroupSheetBinding
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing.adpter.NewDrawingGroupAdapter
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.shortToastNow
import javax.inject.Inject


@AndroidEntryPoint
class AddNewGroupBottomSheet(
    val model: NewDrawingV2VM,
    val callback: (group: CeibroGroupsV2) -> Unit
) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddNewGroupSheetBinding

    @Inject
    lateinit var groupAdapter: NewDrawingGroupAdapter
    val items = ArrayList<String>()
    var onAddGroup: ((groupName: String) -> Unit)? = null
    var onRenameGroup: ((updatedName: String, groupData: CeibroGroupsV2) -> Unit)? = null
    var onDeleteGroup: ((groupData: CeibroGroupsV2) -> Unit)? = null
    var groupDataToUpdate: CeibroGroupsV2? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_new_group_sheet,
            container,
            false
        )
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        model.groupList.observe(viewLifecycleOwner) {
//            println("floorList. groupList observer called")
            val otherGroups = it.filter { it.isCreator }.toMutableList() ?: mutableListOf()
            groupAdapter.setList(otherGroups)
        }
        binding.rvGroupList.adapter = groupAdapter

        groupAdapter.deleteClickListener = { data ->
            onDeleteGroup?.invoke(data)
        }

        groupAdapter.renameClickListener = { data ->
            binding.tvAddNewGroup.visibility = View.GONE
            binding.addGroupBtn.text = "Rename"
            binding.clAddGroup.visibility = View.VISIBLE
            binding.etNewGroup.text = Editable.Factory.getInstance().newEditable(data.groupName)
            groupDataToUpdate = data

            Handler(Looper.getMainLooper()).postDelayed({
                binding.etNewGroup.requestFocus()
                requireContext().showKeyboard()
                requireContext().showKeyboard()
            },200)


        }

        groupAdapter.itemClickListener = { data ->
            callback.invoke(data)
            dismiss()
        }
        groupAdapter.hideKeyboardListener = {
            Handler(Looper.getMainLooper()).postDelayed({
                binding.etNewGroup.clearFocus()
                binding.etNewGroup.hideKeyboard()
                requireContext().hideKeyboard()
                context?.hideKeyboard()
            },200)


        }


        binding.tvAddNewGroup.setOnClickListener {
            binding.etNewGroup.text = Editable.Factory.getInstance().newEditable("")
            binding.tvAddNewGroup.visibility = View.GONE
            binding.clAddGroup.visibility = View.VISIBLE
            binding.etNewGroup.requestFocus()
            requireContext().showKeyboard()
            requireContext().showKeyboard()
        }

        binding.addGroupBtn.setOnClickListener {
            val text = binding.etNewGroup.text.toString().trim()
            if (text.isNotEmpty()) {
                if (binding.addGroupBtn.text.toString().equals("Rename", true)) {
                    groupDataToUpdate?.let {
                        if (!it.groupName.equals(text, true)) {

                            if (model.groupList.value?.any {
                                    it.groupName.equals(
                                        text,
                                        true
                                    )
                                } == true) {
                                shortToastNow("Group name already exists")
                            } else {
                                onRenameGroup?.invoke(text, it)
                            }

                        } else {
                            shortToastNow("Group name is same as old group")
                        }
                    }
                } else {

                    if (model.groupList.value?.any { it.groupName.equals(text, true) } == true) {
                        shortToastNow("Group name already exists")
                    } else {
                        onAddGroup?.invoke(text)
                    }

                }
            } else {
                shortToastNow("Group name cannot be empty")
            }
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
        }
        return dialog
    }
}
