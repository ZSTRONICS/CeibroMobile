package com.zstronics.ceibro.ui.projects.newproject.members.memberprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.extensions.gone
import com.zstronics.ceibro.base.extensions.visible
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.data.repos.projects.member.EditProjectMemberRequest
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.databinding.FragmentEditProjectMemberBinding

class EditProjectMemberSheet constructor(
    val projectId: String?,
    val groups: List<ProjectGroup>,
    val roles: List<ProjectRolesResponse.ProjectRole>,
    val member: GetProjectMemberResponse.ProjectMember,
) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentEditProjectMemberBinding
    var onMemberUpdate: ((body: EditProjectMemberRequest) -> Unit)? = null
    var roleId = ""
    var groupId = ""
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_edit_project_member,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        roleId =
            if (member.role?.id != null)
                member.role.id
            else ""
        binding.roleSelectionSpinner.setText(member.role?.name)

        groupId =
            if (member.group?.id != null)
                member.group.id
            else ""
        binding.groupSelectionSpinner.setText(member.group?.name)

        /// Set user information
        val user = member.user
        if (user?.profilePic == "" || user?.profilePic.isNullOrEmpty()) {
            binding.memberPhotoInitials.text =
                "${user?.firstName?.get(0)?.uppercaseChar()}${
                    user?.surName?.get(0)?.uppercaseChar()
                }"
            binding.memberPhoto.gone()
            binding.memberPhotoInitials.visible()
        } else {
            Glide.with(binding.memberPhoto.context)
                .load(user?.profilePic)
                .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                .placeholder(R.drawable.profile_img)
                .into(binding.memberPhoto)
            binding.memberPhoto.visible()
            binding.memberPhotoInitials.gone()
        }

        binding.userName.text = "${user?.firstName} ${user?.surName}"
        binding.companyName.text =
            if (user?.companyName != null)
                user.companyName
            else "No company added"

        binding.email.text =
            if (user?.workEmail != null)
                user.workEmail
            else "No work email added"

        binding.phone.text =
            if (user?.companyPhone != null)
                user.companyPhone
            else "No phone number added"

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
                roleId = roles[position].id
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
                groupId = groups[position].id
            }
        /// End Members spinner

        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.cancelButton.setOnClickListener {
            dismiss()
        }

        binding.addBtn.setOnClick {
            if (roleId.isEmpty()) {
                showToast("Role is required")
            } else if (groupId.isEmpty()) {
                showToast("Group is required")
            } else {
                val body = EditProjectMemberRequest(
                    group = groupId,
                    role = roleId,
                )
                onMemberUpdate?.invoke(body)
            }
        }
    }

    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }
}