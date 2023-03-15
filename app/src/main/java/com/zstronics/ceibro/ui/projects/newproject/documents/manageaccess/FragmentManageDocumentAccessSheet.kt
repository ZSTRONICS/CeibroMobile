package com.zstronics.ceibro.ui.projects.newproject.documents.manageaccess

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.databinding.FragmentManageDocumentAccessBinding
import com.zstronics.ceibro.ui.chat.newchat.GroupsAdapter
import com.zstronics.ceibro.ui.questioner.createquestion.members.ParticipantsAdapter

class FragmentManageDocumentAccessSheet(
    private val projectMembers: LiveData<List<Member?>>,
    private val projectGroups: LiveData<List<ProjectsWithMembersResponse.ProjectDetail.Group>>
) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentManageDocumentAccessBinding
    var onManageAccess: (() -> Unit)? = null

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

        projectMembers.observe(viewLifecycleOwner) {
            adapter.setList(it as List<Member>)
        }
        projectGroups.observe(viewLifecycleOwner) {
            groupsAdapter.setList(it)
        }

        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }
    }
}