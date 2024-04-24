package com.zstronics.ceibro.ui.groupsv2

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.data.repos.task.models.v2.TaskDetailEvents
import com.zstronics.ceibro.databinding.FragmentGroupDetailInfoBinding
import com.zstronics.ceibro.databinding.FragmentTaskInfoBinding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils

class GroupInfoBottomSheet(val groupData: CeibroConnectionGroupV2?) : BottomSheetDialogFragment() {
    lateinit var binding: FragmentGroupDetailInfoBinding
    var onChangePassword: ((oldPassword: String, newPassword: String) -> Unit)? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_group_detail_info,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (groupData != null) {

            binding.taskDetailCreationDate.text =
                "${requireContext().getString(R.string.creation_date_heading)}: ${
                    DateUtils.formatCreationUTCTimeToCustom(
                        utcTime = groupData.createdAt,
                        inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
                    )
                }"

            binding.groupName.text = groupData.name.toCamelCase()

            binding.groupCreatorName.text =
                "${groupData.creator.firstName} ${groupData.creator.surName}"

            if (groupData.groupAdmins.isNotEmpty()) {
                var adminMembers = ""

                var index = 0
                for (item in groupData.groupAdmins) {
                    adminMembers += if (index == groupData.groupAdmins.size - 1) {
                        if (item.firstName.isEmpty()) {
                            item.phoneNumber
                        } else {
                            "${item.firstName} ${item.surName}"
                        }
                    } else {
                        if (item.firstName.isEmpty()) {
                            "${item.phoneNumber}, "
                        } else {
                            "${item.firstName} ${item.surName}, "
                        }
                    }
                    index++
                }

                binding.groupAdminNames.text = adminMembers
            } else {
                binding.groupAdminNames.text = "No group admins"
            }

            if (groupData.assignedToState.isNotEmpty()) {
                val allAssignee = groupData.assignedToState.map { it }
                var assigneeMembers = ""

                var index = 0
                if (allAssignee.isNotEmpty()) {
                    for (item in allAssignee) {
                        assigneeMembers += if (index == allAssignee.size - 1) {
                            if (item.firstName.isEmpty()) {
                                item.phoneNumber
                            } else {
                                "${item.firstName} ${item.surName}"
                            }
                        } else {
                            if (item.firstName.isEmpty()) {
                                "${item.phoneNumber}, "
                            } else {
                                "${item.firstName} ${item.surName}, "
                            }
                        }
                        index++
                    }
                }
                binding.groupAssigneeNames.text = assigneeMembers
            } else {
                binding.groupAssigneeNames.text = "No assignee members"
            }

            if (groupData.confirmer.isNotEmpty()) {
                var confirmerMembers = ""

                var index = 0
                for (item in groupData.confirmer) {
                    confirmerMembers += if (index == groupData.confirmer.size - 1) {
                        if (item.firstName.isNotEmpty()) {
                            "${item.firstName} ${item.surName}"
                        } else {
                            item.phoneNumber
                        }
                    } else {
                        if (item.firstName.isNotEmpty()) {
                            "${item.firstName} ${item.surName}, "
                        } else {
                            "${item.phoneNumber}, "
                        }
                    }
                    index++
                }

                binding.groupConfirmerNames.text = confirmerMembers
            } else {
                binding.groupConfirmerNames.text = "No confirmer added"
            }

            if (groupData.viewer.isNotEmpty()) {
                var viewerMembers = ""

                var index = 0
                for (item in groupData.viewer) {
                    viewerMembers += if (index == groupData.viewer.size - 1) {
                        if (item.firstName.isNotEmpty()) {
                            "${item.firstName} ${item.surName}"
                        } else {
                            item.phoneNumber
                        }
                    } else {
                        if (item.firstName.isNotEmpty()) {
                            "${item.firstName} ${item.surName}, "
                        } else {
                            "${item.phoneNumber}, "
                        }
                    }
                    index++
                }

                binding.groupViewerName.text = viewerMembers
            } else {
                binding.groupViewerName.text = "No viewers added"
            }

            if (groupData.sharedWith.isNotEmpty()) {
                var sharedWithMembers = ""

                var index = 0
                for (item in groupData.viewer) {
                    sharedWithMembers += if (index == groupData.viewer.size - 1) {
                        if (item.firstName.isNotEmpty()) {
                            "${item.firstName} ${item.surName}"
                        } else {
                            item.phoneNumber
                        }
                    } else {
                        if (item.firstName.isNotEmpty()) {
                            "${item.firstName} ${item.surName}, "
                        } else {
                            "${item.phoneNumber}, "
                        }
                    }
                    index++
                }

                binding.groupSharedWithNames.text = sharedWithMembers
            } else {
                binding.groupSharedWithNames.text = "Not shared with any user"
            }

        }


        binding.closeBtn.setOnClick {
            dismiss()
        }
    }


    private fun showToast(text: String) {
        Toast.makeText(requireContext(), text, Toast.LENGTH_SHORT).show()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
//            dialog.behavior.skipCollapsed = true
            dialog.behavior.state = STATE_COLLAPSED
        }
        return dialog

    }
}