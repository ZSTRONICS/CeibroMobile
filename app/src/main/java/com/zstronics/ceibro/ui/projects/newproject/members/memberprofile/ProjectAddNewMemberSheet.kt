package com.zstronics.ceibro.ui.projects.newproject.members.memberprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.repos.projects.member.CreateProjectMemberRequest
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.databinding.FragmentProjectAddNewMemberBinding
import com.zstronics.ceibro.ui.tasks.newtask.MemberChipAdapter

class ProjectAddNewMemberSheet constructor(
    val projectId: String?,
    private val availableMembers: List<Member>,
    val groups: List<ProjectGroup>,
    val roles: List<ProjectRolesResponse.ProjectRole>
) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentProjectAddNewMemberBinding
    var onMemberAdd: ((body: CreateProjectMemberRequest) -> Unit)? = null
    private val _roleAssignee: MutableLiveData<ArrayList<Member>?> = MutableLiveData(arrayListOf())
    var selectedRole = ""
    var selectedGroup = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_project_add_new_member,
            container,
            false
        )
        return binding.root
    }

    private lateinit var assigneeChipsAdapter: MemberChipAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        assigneeChipsAdapter = MemberChipAdapter()

        assigneeChipsAdapter.itemClickListener = { _: View, position: Int, data: Member ->
            removeAssignee(data)
        }

        _roleAssignee.observe(viewLifecycleOwner) {
            if (it != null) {
                assigneeChipsAdapter.setList(it)
            }
        }
        binding.membersChipsRV.adapter = assigneeChipsAdapter

        /// Members spinner
        val membersStrings = availableMembers.map { "${it.firstName} ${it.surName}" }
        val arrayAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                membersStrings
            )

        arrayAdapter.setDropDownViewResource(
            android.R.layout
                .simple_spinner_dropdown_item
        )

        binding.memberSelectionSpinner.setAdapter(arrayAdapter)

        binding.memberSelectionSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                onAssigneeSelect(position)
            }
        /// End Members spinner

        /// Role spinner
        val roleStrings = roles.map { it.name }
        val rolesArrayAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                roleStrings
            )

        rolesArrayAdapter.setDropDownViewResource(
            android.R.layout
                .simple_spinner_dropdown_item
        )

        binding.roleSelectionSpinner.setAdapter(rolesArrayAdapter)

        binding.roleSelectionSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedRole = roles[position].id
            }
        /// End Members spinner

        /// Group spinner
        val groupStrings = groups.map { it.name }
        val groupArrayAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                groupStrings
            )

        groupArrayAdapter.setDropDownViewResource(
            android.R.layout
                .simple_spinner_dropdown_item
        )

        binding.groupSelectionSpinner.setAdapter(groupArrayAdapter)

        binding.groupSelectionSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedGroup = groups[position].id
            }
        /// End Members spinner

        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addBtn.setOnClick {
            if (_roleAssignee.value?.isEmpty() == true) {
                showToast("Members are required")
                return@setOnClick
            } else if (selectedRole.isEmpty()) {
                showToast("Role is required")
            } else if (selectedGroup.isEmpty()) {
                showToast("Group is required")
            } else {
                val body = CreateProjectMemberRequest(
                    group = selectedGroup,
                    role = selectedRole,
                    user = _roleAssignee.value?.map { it.id }
                )
                onMemberAdd?.invoke(body)
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    private fun onAssigneeSelect(position: Int) {
        val member: Member = availableMembers[position]
        val oldAssignees = _roleAssignee.value

        val selectedMember = oldAssignees?.find { it.id == member.id }

        if (selectedMember != null) {
            oldAssignees.remove(selectedMember)
        } else {
            oldAssignees?.add(member)
        }
        _roleAssignee.value = oldAssignees
    }

    private fun removeAssignee(data: Member) {
        val assignee = _roleAssignee.value
        assignee?.remove(data)
        _roleAssignee.value = assignee
    }

}