package com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing


import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.projects.group.GroupResponseV2
import com.zstronics.ceibro.databinding.FragmentAddNewGroupSheetBinding
import com.zstronics.ceibro.ui.projectv2.projectdetailv2.newdrawing.adpter.NewDrawingGroupAdapter
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import javax.inject.Inject


@AndroidEntryPoint
class AddNewGroupBottomSheet(val model: NewDrawingV2VM, val callback: (GroupResponseV2) -> Unit) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddNewGroupSheetBinding

    @Inject
    lateinit var groupAdapter: NewDrawingGroupAdapter
    val items = ArrayList<String>()
    var onAddGroup: ((groupName: String) -> Unit)? = null
    var onRenameGroup: ((String, GroupResponseV2) -> Unit)? = null


    var groupDataToUpdate: GroupResponseV2? = null
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

        model._groupList.observe(viewLifecycleOwner) {
            groupAdapter.setList(it)
        }
        model._groupList.value?.let {
            groupAdapter.setList(it)
        }
        groupAdapter.deleteClickListener = { data ->
            data.Id?.let {
                model.deleteGroupByID(it) {
                    model.groupList.value?.let {
                        groupAdapter.setList(it)
                    }
                }
            }
        }
        groupAdapter.renameClickListener = { data ->
            data.Id?.let {

                binding.tvAddFloors.visibility = View.GONE
                binding.clAddGroup.visibility = View.VISIBLE
                binding.addGroupBtn.text = "Rename"
                binding.tvNewGroup.text = Editable.Factory.getInstance().newEditable(data.groupName)
                groupDataToUpdate = data
            }
        }
        groupAdapter.itemClickListener = { data ->
            callback.invoke(data)

        }

        binding.rvFloorsList.adapter = groupAdapter


        binding.tvAddFloors.setOnClickListener {
            binding.clAddGroup.visibility = View.VISIBLE
            it.visibility = View.GONE
        }
        binding.addGroupBtn.setOnClickListener {
            val text = binding.tvNewGroup.text.toString()
            if (text.isNotEmpty()) {
                if (!items.contains(binding.tvNewGroup.text.toString().toLowerCase(Locale.ROOT))) {

                    if (binding.addGroupBtn.text == "Rename") {
                        groupDataToUpdate?.let {
                            onRenameGroup?.invoke(text, it)
                        }

                    }

                    onAddGroup?.invoke(text)
                    binding.tvNewGroup.text = Editable.Factory.getInstance().newEditable("")
                }
            } else {
            }
            binding.tvAddFloors.visibility = View.VISIBLE
            binding.clAddGroup.visibility = View.GONE
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
