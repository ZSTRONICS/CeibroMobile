package com.zstronics.ceibro.ui.projects.newproject.role.addrole

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
import com.zstronics.ceibro.base.extensions.gone
import com.zstronics.ceibro.base.extensions.visible
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.role.CreateRoleRequest
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.databinding.FragmentAddNewRoleBinding
import com.zstronics.ceibro.ui.tasks.newtask.MemberChipAdapter

class AddNewRoleSheet constructor(
    val projectId: String?,
    private val members: ArrayList<Member>,
    private val roleData: ProjectRolesResponse.ProjectRole?
) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddNewRoleBinding

    private val _roleAssignee: MutableLiveData<ArrayList<Member>?> = MutableLiveData(arrayListOf())

    var onAdd: ((roleData: CreateRoleRequest) -> Unit)? = null
    var onUpdate: ((roleData: CreateRoleRequest) -> Unit)? = null

    private lateinit var assigneeChipsAdapter: MemberChipAdapter
    private val availableMembers = members.map { it } as ArrayList<Member>
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_new_role,
            container,
            false
        )
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        with(binding) {
            assigneeChipsAdapter = MemberChipAdapter()
            assigneeChipsAdapter.itemClickListener = { _: View, position: Int, data: Member ->
                removeAssignee(data)
            }
            if (roleData == null) {
                headingText.text = context?.getString(R.string.add_role_heading)
                addBtn.text = context?.getString(R.string.add_heading)
            } else {
                /// set role data to fields.
                headingText.text = context?.getString(R.string.edit_role_heading)
                addBtn.text = context?.getString(R.string.update_btn_text)
                roleText.setText(roleData.name)
                projectAdminSwitch.isChecked = roleData.admin

                /// check role permissions
                if (roleData.rolePermission.create || roleData.rolePermission.edit || roleData.rolePermission.delete) {
                    roleSwitch.isChecked = true
                }
                roleSwitch.visible()
                roleLayout.visible()

                createRoleCheckbox.isChecked = roleData.rolePermission.create
                editRoleCheckbox.isChecked = roleData.rolePermission.edit
                deleteRoleCheckbox.isChecked = roleData.rolePermission.delete

                /// Check the member permissions

                if (roleData.memberPermission.create || roleData.memberPermission.edit || roleData.memberPermission.delete) {
                    memberSwitch.isChecked = true
                }

                memberSwitch.visible()
                memberPermissionLayout.visible()

                createMemberCheckbox.isChecked = roleData.memberPermission.create
                editMemberCheckbox.isChecked = roleData.memberPermission.edit
                deleteMemberCheckbox.isChecked = roleData.memberPermission.delete
                val previousMembers = roleData.members
                _roleAssignee.value = previousMembers as ArrayList<Member>
            }

            _roleAssignee.observe(viewLifecycleOwner) {
                if (it != null) {
                    assigneeChipsAdapter.setList(it)
                }
            }
            membersChipsRV.adapter = assigneeChipsAdapter

            /// spinner
            setMemberSelectionSpinner(availableMembers)
            /// End spinner
            closeBtn.setOnClickListener {
                dismiss()
            }
            cancelButton.setOnClickListener {
                dismiss()
            }

            addBtn.setOnClick {
                if (roleText.text?.isEmpty() == true) {
                    showToast("Role Title is required")
                    return@setOnClick
                } else if (
                    projectAdminSwitch.isChecked ||
                    roleSwitch.isChecked ||
                    memberSwitch.isChecked
                ) {
                    if (
                        projectAdminSwitch.isChecked ||
                        createRoleCheckbox.isChecked ||
                        editRoleCheckbox.isChecked ||
                        deleteRoleCheckbox.isChecked ||
                        createMemberCheckbox.isChecked ||
                        editMemberCheckbox.isChecked ||
                        deleteMemberCheckbox.isChecked
                    ) {
                        val request = CreateRoleRequest(
                            admin = projectAdminSwitch.isChecked,
                            memberPermission = CreateRoleRequest.PermissionRequest(
                                create = createMemberCheckbox.isChecked,
                                delete = deleteMemberCheckbox.isChecked,
                                edit = editMemberCheckbox.isChecked
                            ),
                            members = _roleAssignee.value?.map { it.id },
                            name = roleText.text.toString(),
                            project = projectId.toString(),
                            rolePermission = CreateRoleRequest.PermissionRequest(
                                create = createRoleCheckbox.isChecked,
                                delete = deleteRoleCheckbox.isChecked,
                                edit = editRoleCheckbox.isChecked
                            )
                        )

                        if (roleData == null)
                            onAdd?.invoke(request)
                        else {
                            onUpdate?.invoke(request)
                        }
                    }
                }
            }

            // Admin Switch handling
            projectAdminSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    roleSwitch.gone()
                    roleLayout.gone()
                    memberSwitch.gone()
                    memberPermissionLayout.gone()
                    roleSwitch.isChecked = false
                    memberSwitch.isChecked = false

                    /// check all role permissions
                    createRoleCheckbox.isChecked = true
                    editRoleCheckbox.isChecked = true
                    deleteRoleCheckbox.isChecked = true

                    /// check all member permissions
                    createMemberCheckbox.isChecked = true
                    editMemberCheckbox.isChecked = true
                    deleteMemberCheckbox.isChecked = true

                } else {
                    roleSwitch.visible()
                    memberSwitch.visible()

                    /// uncheck all role permissions
                    createRoleCheckbox.isChecked = false
                    editRoleCheckbox.isChecked = false
                    deleteRoleCheckbox.isChecked = false

                    /// uncheck all member permissions
                    createMemberCheckbox.isChecked = false
                    editMemberCheckbox.isChecked = false
                    deleteMemberCheckbox.isChecked = false
                }
            }

            // Role Switch handling
            roleSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    roleLayout.visible()
//                    createRoleCheckbox.isChecked = false
//                    editRoleCheckbox.isChecked = false
//                    deleteRoleCheckbox.isChecked = false
                } else {
                    roleLayout.gone()
                }
            }
            createRoleCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    roleSwitch.isChecked = true
                }
            }
            editRoleCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    roleSwitch.isChecked = true
                }
            }
            deleteRoleCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    roleSwitch.isChecked = true
                }
            }

            // Member Switch handling
            memberSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    memberPermissionLayout.visible()
//                    createMemberCheckbox.isChecked = false
//                    editMemberCheckbox.isChecked = false
//                    deleteMemberCheckbox.isChecked = false
                } else {
                    memberPermissionLayout.gone()
                }
            }

            createMemberCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    memberSwitch.isChecked = true
                }
            }
            editMemberCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    memberSwitch.isChecked = true
                }
            }
            deleteMemberCheckbox.setOnCheckedChangeListener { buttonView, isChecked ->
                if (isChecked) {
                    memberSwitch.isChecked = true
                }
            }
        }
    }

    private fun setMemberSelectionSpinner(
        availableMembers: List<Member>
    ) {
        val arrayAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                availableMembers.map { "${it.firstName} ${it.surName}" }
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
    }

    fun hideSheet() {
        dismiss()
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
        availableMembers.add(data)
        setMemberSelectionSpinner(availableMembers)
        val assignee = _roleAssignee.value
        assignee?.remove(data)
        _roleAssignee.value = assignee
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}
