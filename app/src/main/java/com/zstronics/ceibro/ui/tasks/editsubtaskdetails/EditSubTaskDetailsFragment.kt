package com.zstronics.ceibro.ui.tasks.editsubtaskdetails

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.subtask.AllSubtask
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.databinding.FragmentEditSubTaskDetailsBinding
import com.zstronics.ceibro.ui.tasks.newtask.MemberChipAdapter
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus
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
            R.id.editDetailsAddMemberBtn -> {
                val assigneeMember = viewModel.subtaskAssignee.value
                if (assigneeMember?.isEmpty() == true) {
                    shortToastNow("Select members to add")
                }
                else {
                    viewModel.addMemberToSubtask(viewModel.subtask.value, SubTaskStatus.ASSIGNED.name)
                }
            }
        }
    }


    @Inject
    lateinit var addMembersChipsAdapter: MemberChipAdapter

    @Inject
    lateinit var addedByAdapter: EditSubTaskDetailsAddedByAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.subtask.observe(viewLifecycleOwner) { it ->
            mViewDataBinding.editDetailsMemberSpinner.setText("")
            it?.taskData?.project?.id?.let { viewModel.loadMemberByProjectId(it, mViewDataBinding.skeletonLayout, mViewDataBinding.editDetailsMemberSpinner) }
        }

        viewModel.subTaskStatus.observe(viewLifecycleOwner) {

        }

        viewModel.assignToMembers.observe(viewLifecycleOwner) {
            addedByAdapter.setList(it, viewModel.subtask.value)
        }
        mViewDataBinding.allMembersRV.adapter = addedByAdapter


        addedByAdapter.deleteItemClickListener = { childView: View, position: Int, taskId: String, subTaskId: String, memberId: String ->
            viewModel.removeMemberFromSubtask(taskId, subTaskId, memberId)
        }

        addedByAdapter.doneItemClickListener = { childView: View, position: Int, taskId: String, subTaskId: String, memberId: String ->
            viewModel.markAsDoneForSubtaskMember(taskId, subTaskId, memberId)
        }




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