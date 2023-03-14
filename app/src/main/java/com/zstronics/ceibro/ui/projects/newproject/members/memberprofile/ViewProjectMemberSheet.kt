package com.zstronics.ceibro.ui.projects.newproject.members.memberprofile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.extensions.gone
import com.zstronics.ceibro.base.extensions.visible
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.databinding.FragmentViewProjectMemberBinding

class ViewProjectMemberSheet constructor(
    val member: GetProjectMemberResponse.ProjectMember,
) :
    BottomSheetDialogFragment() {
    lateinit var binding: FragmentViewProjectMemberBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_view_project_member,
            container,
            false
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        binding.companyName.text = user?.companyName
        binding.email.text = user?.workEmail
        binding.phone.text = user?.companyPhone
        binding.roleText.setText(member.role.name)
        binding.groupText.setText(member.group.name)

        binding.closeBtn.setOnClick {
            dismiss()
        }
    }
}