package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

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
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.FragmentConnectionsV2Binding
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
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
        }
    }

    @Inject
    lateinit var adapter: CeibroConnectionsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startOneTimeContactSyncWorker(requireContext())
        mViewDataBinding.connectionRV.adapter = adapter
        mViewDataBinding.connectionRV.layoutManager?.isItemPrefetchEnabled = true
        mViewDataBinding.connectionRV.setRecycledViewPool(RecyclerView.RecycledViewPool())

        viewModel.allConnections.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.setList(it)
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
        if (!runUIOnce) {
            loadConnections(true)
            runUIOnce = true
        }
        mViewDataBinding.searchBar.setQuery("", true)
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
        val popupMenu = PopupMenu(requireContext(), anchorView)
        popupMenu.inflate(if (viewModel.sessionManager.getUser().value?.autoContactSync == true) R.menu.sync_enabled_menu else R.menu.sync_disabled_menu)
        popupMenu.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.disableSync -> {
                    val builder = MaterialAlertDialogBuilder(requireContext())
                    builder.setMessage(resources.getString(R.string.sync_contacts_statement_disable))
                    builder.setCancelable(false)
                    builder.setPositiveButton("Allow") { dialog, which ->
                        viewModel.syncContactsDisable {
                            viewModel.sessionManager.updateAutoSync(false)
                            viewState.isAutoSyncEnabled.value = false
                        }
                    }
                    builder.setNegativeButton("Deny") { dialog, which -> }
                    builder.show()
                    true
                }
                R.id.enableSync -> {
                    val builder = MaterialAlertDialogBuilder(requireContext())
                    builder.setMessage(resources.getString(R.string.sync_contacts_statement))
                    builder.setCancelable(false)
                    builder.setPositiveButton("Allow") { dialog, which ->
                        viewModel.syncContactsEnabled {
                            viewModel.sessionManager.updateAutoSync(true)
                            viewState.isAutoSyncEnabled.value = true
                        }
                    }
                    builder.setNegativeButton("Deny") { dialog, which -> }
                    builder.show()
                    true
                }
                R.id.selectContacts -> {
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

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onContactsSynced(event: LocalEvents.ContactsSynced) {
        loadConnections(false)
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
        loadConnections(false)
    }
}