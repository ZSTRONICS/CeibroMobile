package com.zstronics.ceibro.ui.dashboard.connectionv2Profile

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import androidx.core.widget.ImageViewCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentConnectionsV2ProfileBinding
import com.zstronics.ceibro.ui.dashboard.connectionv2Profile.blockunblock.FragmentConnectionBlockUnblockBottomSheet
import com.zstronics.ceibro.ui.enums.ConnectionStatusActions
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class MyConnectionV2ProfileFragment :
    BaseNavViewModelFragment<FragmentConnectionsV2ProfileBinding, IMyConnectionV2Profile.State, MyConnectionV2ProfileVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: MyConnectionV2ProfileVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_connections_v2_profile
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.newTaskButton -> {
//                navigate(R.id.newTaskFragment)
                shortToastNow(resources.getString(R.string.not_available))
            }
            R.id.closeBtn -> navigateBack()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.connection.observe(viewLifecycleOwner) { item ->
            val binding = mViewDataBinding
            val context = binding.ceibroLogo.context
            binding.phoneNumber.text = item.phoneNumber
            when {
                item.isCeiborUser && !item.isBlocked -> {
                    ImageViewCompat.setImageTintList(
                        binding.ceibroLogo,
                        ColorStateList.valueOf(context.resources.getColor(R.color.appYellow))
                    )
                }
                item.isCeiborUser && item.isBlocked -> {
                    ImageViewCompat.setImageTintList(
                        binding.ceibroLogo,
                        ColorStateList.valueOf(context.resources.getColor(R.color.appRed))
                    )
                }
                else -> {
                    ImageViewCompat.setImageTintList(
                        binding.ceibroLogo,
                        ColorStateList.valueOf(context.resources.getColor(R.color.appGrey))
                    )
                }
            }
            binding.contactName.text =
                "${item.contactFirstName?.toCamelCase()} ${item.contactSurName?.toCamelCase()}"

            if (item.userCeibroData == null) {
                binding.emailTV.visibility = View.GONE
                binding.companyTV.visibility = View.GONE
            } else {
                binding.emailTV.visibility = View.VISIBLE
                binding.companyTV.visibility = View.VISIBLE
                binding.emailTV.text = item.userCeibroData.email
                binding.companyTV.text = item.userCeibroData.jobTitle
                binding.companyTV.text =
                    if (item.userCeibroData.companyName.equals("")) {
                        "N/A"
                    } else {
                        item.userCeibroData.companyName
                    }
            }

            if (item.userCeibroData?.profilePic.isNullOrEmpty()) {
                binding.contactInitials.visibility = View.VISIBLE
                binding.contactImage.visibility = View.GONE
                var initials = ""
                if (item.contactFirstName?.isNotEmpty() == true) {
                    initials += item.contactFirstName[0].uppercaseChar()
                }
                if (item.contactSurName?.isNotEmpty() == true) {
                    initials += item.contactSurName[0].uppercaseChar()
                }
                binding.contactInitials.text = initials
            } else {
                binding.contactInitials.visibility = View.GONE
                binding.contactImage.visibility = View.VISIBLE

                Glide.with(binding.contactImage.context)
                    .load(item.userCeibroData?.profilePic)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.profile_img)
                    .into(binding.contactImage)
            }

            mViewDataBinding.optionMenu.setOnClickListener {
                val action =
                    if (item.isBlocked) ConnectionStatusActions.UNBLOCK else ConnectionStatusActions.BLOCK
                val sheet = FragmentConnectionBlockUnblockBottomSheet(action)
                sheet.actionClick = { view, data ->
                    when (data) {
                        ConnectionStatusActions.BLOCK -> {
                            /// API CALL
                            viewModel.blockUser(item.id) {
                                /// HIDE SHEET
                                sheet.dismiss()

                            }
                        }
                        ConnectionStatusActions.UNBLOCK -> {
                            /// API CALL
                            viewModel.unblockUser(item.id) {
                                /// HIDE SHEET
                                sheet.dismiss()
                            }
                        }
                    }
                }
                sheet.isCancelable = true
                sheet.show(childFragmentManager, "FragmentConnectionBlockUnblockBottomSheet")
            }
        }
    }
}