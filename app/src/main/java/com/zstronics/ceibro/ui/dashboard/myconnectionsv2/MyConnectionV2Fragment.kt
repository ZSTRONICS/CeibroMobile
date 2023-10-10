package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.SearchView
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.BuildConfig
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.KEY_CONTACTS_CURSOR
import com.zstronics.ceibro.base.KEY_TOKEN_VALID
import com.zstronics.ceibro.base.KEY_updatedAndNewContacts
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.toast
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.FragmentConnectionsV2Binding
import com.zstronics.ceibro.extensions.getLocalContacts
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.TaskInfoBottomSheet
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import okhttp3.internal.immutableListOf
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject


@AndroidEntryPoint
class MyConnectionV2Fragment :
    BaseNavViewModelFragment<FragmentConnectionsV2Binding, IMyConnectionV2.State, MyConnectionV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: MyConnectionV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_connections_v2
    override fun toolBarVisibility(): Boolean = false
    var runUIOnce = false
    var contactsPermissionGranted = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.optionMenuIV -> {
                showPopupMenu(mViewDataBinding.optionMenuIV)
            }

            R.id.addContactsBtn -> {
                val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
                addContactResultLauncher.launch(intent)
            }

            R.id.closeBtn -> {
                navigateBack()
            }

            R.id.connectionDescAutoSyncBtn -> {
                viewModel.syncContactsEnabled {
//                    viewModel.sessionManager.updateAutoSync(true)
                    viewState.isAutoSyncEnabled.value = true
                    startOneTimeContactSyncWorker(requireContext())
                    toast("Contacts sync enabled")
                }
            }

            R.id.connectionDescManualSelectionBtn -> {
                navigate(R.id.manualContactsSelectionFragment)
            }

            R.id.connectionDescGrantContactPermissionBtn -> {
                goToPermissionSettings(
                    Manifest.permission.READ_CONTACTS,
                    immutableListOf(Manifest.permission.READ_CONTACTS)
                )
                runUIOnce = false
            }

            R.id.connectionDescSaveContactBtn -> {
                val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
                addContactResultLauncher.launch(intent)
            }

            R.id.contactsInfoBtn -> {
                showContactsInfoBottomSheet()
            }
        }
    }

    @Inject
    lateinit var adapter: CeibroConnectionsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        contactsPermissionGranted = isPermissionGranted(Manifest.permission.READ_CONTACTS)
        if (BuildConfig.DEBUG) {
            mViewDataBinding.contactsInfoBtn.visibility = View.VISIBLE
        }
        mViewDataBinding.connectionInfoNoContactPermission.visibility = View.GONE
        mViewDataBinding.connectionInfoOnDisabledAutoSyncLayout.visibility = View.GONE
        mViewDataBinding.connectionInfoNoContactFound.visibility = View.GONE
        mViewDataBinding.connectionLogoBackground.visibility = View.GONE
        mViewDataBinding.connectionRV.visibility = View.GONE
        mViewDataBinding.connectionRV.adapter = adapter
        mViewDataBinding.connectionRV.layoutManager?.isItemPrefetchEnabled = true
        mViewDataBinding.connectionRV.setRecycledViewPool(RecyclerView.RecycledViewPool())

        viewModel.allConnections.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter.setList(it)
                mViewDataBinding.connectionRV.visibility = View.VISIBLE
                mViewDataBinding.searchBar.visibility = View.VISIBLE
                mViewDataBinding.connectionInfoNoContactPermission.visibility = View.GONE
                mViewDataBinding.connectionInfoOnDisabledAutoSyncLayout.visibility = View.GONE
                mViewDataBinding.connectionInfoNoContactFound.visibility = View.GONE
                mViewDataBinding.connectionLogoBackground.visibility = View.GONE
            } else {
                adapter.setList(listOf())
                if (!contactsPermissionGranted) {
                    mViewDataBinding.connectionRV.visibility = View.GONE
                    mViewDataBinding.searchBar.visibility = View.GONE
                    mViewDataBinding.connectionInfoNoContactPermission.visibility = View.VISIBLE
                    mViewDataBinding.connectionInfoOnDisabledAutoSyncLayout.visibility = View.GONE
                    mViewDataBinding.connectionInfoNoContactFound.visibility = View.GONE
                    mViewDataBinding.connectionLogoBackground.visibility = View.VISIBLE
                }
                if (viewState.isAutoSyncEnabled.value == false && viewModel.originalConnections.isEmpty() && contactsPermissionGranted) {
                    mViewDataBinding.connectionRV.visibility = View.GONE
                    mViewDataBinding.searchBar.visibility = View.GONE
                    mViewDataBinding.connectionInfoNoContactPermission.visibility = View.GONE
                    mViewDataBinding.connectionInfoOnDisabledAutoSyncLayout.visibility =
                        View.VISIBLE
                    mViewDataBinding.connectionInfoNoContactFound.visibility = View.GONE
                    mViewDataBinding.connectionLogoBackground.visibility = View.VISIBLE
                }
                if (viewState.isAutoSyncEnabled.value == true && viewModel.originalConnections.isEmpty() && contactsPermissionGranted) {
                    mViewDataBinding.connectionRV.visibility = View.GONE
                    mViewDataBinding.searchBar.visibility = View.GONE
                    mViewDataBinding.connectionInfoNoContactPermission.visibility = View.GONE
                    mViewDataBinding.connectionInfoOnDisabledAutoSyncLayout.visibility = View.GONE
                    mViewDataBinding.connectionInfoNoContactFound.visibility = View.VISIBLE
                    mViewDataBinding.connectionLogoBackground.visibility = View.VISIBLE
                }
            }
        }
        adapter.itemClickListener =
            { _: View, position: Int, data: AllCeibroConnections.CeibroConnection ->
                if (data.isCeiborUser)
                    navigate(R.id.myConnectionV2ProfileFragment, bundleOf(CONNECTION_KEY to data))
            }

        mViewDataBinding.searchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.filterContacts(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.filterContacts(newText)
                }
                return true
            }

        })
    }

    override fun onResume() {
        super.onResume()
        contactsPermissionGranted = isPermissionGranted(Manifest.permission.READ_CONTACTS)
        mViewDataBinding.searchBar.setQuery("", true)
        if (viewModel.user == null) {
            viewModel.sessionManager.setUser()
            viewModel.sessionManager.setToken()
            println("PhoneNumber-MyConnectionV2VM- on start user and token updated because of null")
            viewModel.user = viewModel.sessionManager.getUser().value
        }
        if (!runUIOnce) {
            loadConnections(true)
            runUIOnce = true
        }
        if (isPermissionGranted(Manifest.permission.READ_CONTACTS)) {
            viewState.contactsPermission = "Granted"
            startOneTimeContactSyncWorker(requireContext())
        } else {
            viewState.contactsPermission = "Not granted"
        }
        val contacts = getLocalContacts(viewModel.resProvider.context, viewModel.sessionManager)
        viewState.localContactsSize = contacts.size

        val isTokenValid = viewModel.sessionManager.getBooleanValue(KEY_TOKEN_VALID)
        val isContactsCursorValid = viewModel.sessionManager.getBooleanValue(KEY_CONTACTS_CURSOR)
        val updatedAndNewContactsSize =
            viewModel.sessionManager.getIntegerValue(KEY_updatedAndNewContacts)

        viewState.isValidSession = isTokenValid
        viewState.isCursorValid = isContactsCursorValid
        viewState.newUpdatedContactListSize = updatedAndNewContactsSize
    }

    private fun loadConnections(skeletonVisible: Boolean) {
        if (skeletonVisible) {
            mViewDataBinding.connectionRV.loadSkeleton(R.layout.layout_invitations_box) {
                itemCount(10)
                color(R.color.appLightGrey)
            }

            viewModel.getAllConnectionsV2 {
                mViewDataBinding.connectionRV.hideSkeleton()
                val searchQuery = mViewDataBinding.searchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.filterContacts(searchQuery)
                }
            }
        } else {
            viewModel.getAllConnectionsV2 {
                val searchQuery = mViewDataBinding.searchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.filterContacts(searchQuery)
                }
            }
        }
    }

    private fun showPopupMenu(anchorView: View) {
        val popupMenu = PopupMenu(
            requireActivity(),
            anchorView
        )    //requireActivity is done because theme styles work with activity context, not with application context
        popupMenu.inflate(if (viewState.isAutoSyncEnabled.value == true) R.menu.sync_enabled_menu else R.menu.sync_disabled_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.disableSync -> {
//                    val builder = MaterialAlertDialogBuilder(requireContext())
//                    builder.setMessage(resources.getString(R.string.sync_contacts_statement_disable))
//                    builder.setCancelable(false)
//                    builder.setPositiveButton("Allow") { dialog, which ->
//                        viewModel.syncContactsDisable {
//                            viewModel.sessionManager.updateAutoSync(false)
//                            viewState.isAutoSyncEnabled.value = false
//                        }
//                    }
//                    builder.setNegativeButton("Deny") { dialog, which -> }
//                    builder.show()
                    viewModel.syncContactsDisable {
//                        viewModel.sessionManager.updateAutoSync(false)
                        viewState.isAutoSyncEnabled.value = false
                        toast("Contacts sync disabled")
                    }
                    true
                }

                R.id.enableSync -> {
//                    val builder = MaterialAlertDialogBuilder(requireContext())
//                    builder.setMessage(resources.getString(R.string.sync_contacts_statement))
//                    builder.setCancelable(false)
//                    builder.setPositiveButton("Allow") { dialog, which ->
//                        viewModel.syncContactsEnabled {
//                            viewModel.sessionManager.updateAutoSync(true)
//                            viewState.isAutoSyncEnabled.value = true
//                        }
//                    }
//                    builder.setNegativeButton("Deny") { dialog, which -> }
//                    builder.show()
                    viewModel.syncContactsEnabled {
//                        viewModel.sessionManager.updateAutoSync(true)
                        viewState.isAutoSyncEnabled.value = true
                        startOneTimeContactSyncWorker(requireContext())
                        toast("Contacts sync enabled")
                    }
                    true
                }

                R.id.selectContacts -> {
                    navigate(R.id.manualContactsSelectionFragment)
                    true
                }

                R.id.refreshSync -> {
                    startOneTimeContactSyncWorker(requireContext())
                    true
                }

                R.id.addContactsBtn -> {
                    val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
                    addContactResultLauncher.launch(intent)
                    true
                }

                else -> {
                    true
                }
            }
        }
        popupMenu.show()
    }

    private fun showContactsInfoBottomSheet() {
        val isTokenValid = viewModel.sessionManager.getBooleanValue(KEY_TOKEN_VALID)
        val isContactsCursorValid = viewModel.sessionManager.getBooleanValue(KEY_CONTACTS_CURSOR)
        val updatedAndNewContactsSize =
            viewModel.sessionManager.getIntegerValue(KEY_updatedAndNewContacts)
        viewState.isValidSession = isTokenValid
        viewState.isCursorValid = isContactsCursorValid
        viewState.newUpdatedContactListSize = updatedAndNewContactsSize

        val sheet = ContactsInfoBottomSheet(
            _deviceInfo = viewState.deviceInfo,
            _contactsPermission = viewState.contactsPermission,
            _autoSync = viewState.isAutoSyncEnabled.value ?: false,
            _localPhoneContactsSize = viewState.localContactsSize,
            _syncedContactsSizeInDB = viewState.dbContactsSize,
            _syncedContactsSizeInList = viewModel.allConnections.value?.size ?: -1,
            _isTokenValid = viewState.isValidSession,
            _isContactsCursorValid = viewState.isCursorValid,
            _updatedAndNewContactsSize = viewState.newUpdatedContactListSize
        )
//        sheet.dialog?.window?.setSoftInputMode(
//            WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE or
//                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
//        );

        sheet.isCancelable = true
        sheet.show(childFragmentManager, "ContactsInfoBottomSheet")
    }


    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    companion object {
        const val CONNECTION_KEY: String = "Connection"
    }


    private val addContactResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            val resultCode = result.resultCode
//            if (resultCode == Activity.RESULT_OK) {
            // Contact added successfully, trigger your desired action here
            startOneTimeContactSyncWorker(requireContext())
//            }
        }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onGetAllContactsFromAPI(event: LocalEvents.UpdateConnections) {
//        shortToastNow("Update contacts called")
        loadConnections(false)
    }
}