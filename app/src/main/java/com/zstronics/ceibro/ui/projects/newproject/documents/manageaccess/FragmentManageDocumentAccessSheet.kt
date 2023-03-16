package com.zstronics.ceibro.ui.projects.newproject.documents.manageaccess

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.toast
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.databinding.FragmentManageDocumentAccessBinding
import com.zstronics.ceibro.ui.chat.newchat.GroupsAdapter
import com.zstronics.ceibro.ui.questioner.createquestion.members.ParticipantsAdapter

class FragmentManageDocumentAccessSheet(
    private val projectMembers: List<Member?>,
    private val projectGroups: List<ProjectsWithMembersResponse.ProjectDetail.Group>,
    private val accessList: List<String>
) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentManageDocumentAccessBinding
    var onManageAccess: ((
        groups: List<String>,
        users: List<String>,
    ) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_manage_document_access,
            container,
            false
        )
        return binding.root
    }

    lateinit var adapter: ParticipantsAdapter

    lateinit var groupsAdapter: GroupsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        adapter = ParticipantsAdapter()
        groupsAdapter = GroupsAdapter()

        binding.recyclerView.adapter = adapter
        binding.groupsRecyclerView.adapter = groupsAdapter

        val updatedMembers = projectMembers.map { member ->
            member?.copy(isChecked = accessList.contains(member.id))
        }
        adapter.setList(updatedMembers as List<Member>)
        groupsAdapter.setList(projectGroups)

        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
        binding.updateBtn.setOnClickListener {
            val selectedGroupsId = groupsAdapter.dataList.filter { it.isChecked }.map { it.id }
            val selectedUsersId = adapter.dataList.filter { it.isChecked }.map { it.id }
            if (selectedUsersId.isNotEmpty()) {
                onManageAccess?.invoke(selectedGroupsId, selectedUsersId)
                dismiss()
            } else {
                toast("Select the members to update access")
            }
        }
    }
}