package com.zstronics.ceibro.ui.tasks.editsubtaskdetails

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.databinding.FragmentEditSubTaskDetailsBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import com.zstronics.ceibro.ui.tasks.newtask.MemberChipAdapter
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class EditSubTaskDetailsFragment :
    BaseNavViewModelFragment<FragmentEditSubTaskDetailsBinding, IEditSubTaskDetails.State, EditSubTaskDetailsVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: EditSubTaskDetailsVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_edit_sub_task_details
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn -> navigateBack()
        }
    }


    @Inject
    lateinit var addMembersChipsAdapter: MemberChipAdapter

    @Inject
    lateinit var adapter: EditSubTaskDetailsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.subtask.observe(viewLifecycleOwner) {
        }

        viewModel.subTaskStatus.observe(viewLifecycleOwner) {
            adapter.setList(it, viewModel.subtask.value)
        }
        mViewDataBinding.allMembersRV.adapter = adapter



        viewModel.subtaskAssignee.observe(viewLifecycleOwner) {
            addMembersChipsAdapter.setList(it)
        }
        mViewDataBinding.addMembersChipsRV.adapter = addMembersChipsAdapter

        addMembersChipsAdapter.itemClickListener = { _: View, position: Int, data: Member ->
            viewModel.removeAssignee(data)
        }

        viewModel.projectMemberNames.observe(viewLifecycleOwner) {
            val arrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    it
                )

            arrayAdapter.setDropDownViewResource(
                android.R.layout
                    .simple_spinner_dropdown_item
            )

            mViewDataBinding.editDetailsMemberSpinner.setAdapter(arrayAdapter)
        }

        mViewDataBinding.editDetailsMemberSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.onAssigneeSelect(position)
            }


    }
}