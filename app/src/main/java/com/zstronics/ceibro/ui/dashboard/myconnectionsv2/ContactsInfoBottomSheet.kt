package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

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
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentContactsInfoBinding
import com.zstronics.ceibro.databinding.FragmentTaskInfoBinding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils

class ContactsInfoBottomSheet(_deviceInfo: String, _contactsPermission: String, _autoSync: Boolean, _localPhoneContactsSize: Int, _syncedContactsSizeInDB: Int, _syncedContactsSizeInList: Int) : BottomSheetDialogFragment() {
    lateinit var binding: FragmentContactsInfoBinding
    var onChangePassword: ((oldPassword: String, newPassword: String) -> Unit)? = null
    var onChangePasswordDismiss: (() -> Unit)? = null
    val deviceInfo = _deviceInfo
    val contactsPermission = _contactsPermission
    val autoSync = _autoSync
    val localPhoneContactsSize = _localPhoneContactsSize
    val syncedContactsSizeInDB = _syncedContactsSizeInDB
    val syncedContactsSizeInList = _syncedContactsSizeInList

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_contacts_info,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.deviceInfoName.text = deviceInfo
        binding.contactsPermission.text = contactsPermission
        binding.autoSyncName.text = if (autoSync) "Enabled" else "Disabled"
        binding.localPhoneContactsSize.text = "$localPhoneContactsSize"
        binding.dbPhoneContactsSize.text = "$syncedContactsSizeInDB"
        binding.listPhoneContactsSize.text = "$syncedContactsSizeInList"


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