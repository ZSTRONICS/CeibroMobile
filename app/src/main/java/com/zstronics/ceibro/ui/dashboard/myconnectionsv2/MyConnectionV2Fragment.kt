package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.toast
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.FragmentConnectionsV2Binding
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import okhttp3.internal.immutableListOf
import javax.inject.Inject


@AndroidEntryPoint
class MyConnectionV2Fragment :
    BaseNavViewModelFragment<FragmentConnectionsV2Binding, IMyConnectionV2.State, MyConnectionV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: MyConnectionV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_connections_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.syncIV -> {
                checkPermission(
                    immutableListOf(
                        Manifest.permission.READ_CONTACTS,
                    )
                ) {
                    val builder = MaterialAlertDialogBuilder(requireContext())
                    builder.setMessage("Ceibro asks your permission to add all your phone contacts to Ceibro's contact list.")
                    builder.setCancelable(false)
                    builder.setPositiveButton("Allow") { dialog, which ->
                        // User clicked Allow button
                        // Add your logic here
                        viewModel.syncContactsEnabled {
                            toast("Your all contacts synced with server")
                            loadConnections()
                            viewModel.sessionManager.updateAutoSync(true)
                            viewState.isAutoSyncEnabled.postValue(true)
                        }
                    }
                    builder.setNegativeButton("Deny") { dialog, which ->

                    }
                    builder.show()
                }
            }
            R.id.addContactsBtn -> {
                val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
                startActivity(intent)
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
        mViewDataBinding.connectionRV.adapter = adapter


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

        viewState.searchName.observe(viewLifecycleOwner) { search ->
            viewModel.filterContacts(search.lowercase())
        }
    }

    override fun onResume() {
        super.onResume()
        loadConnections()
    }

    private fun loadConnections() {
        mViewDataBinding.connectionRV.loadSkeleton(R.layout.layout_item_connection) {
            itemCount(10)
            color(R.color.appLightGrey)
        }

        viewModel.getAllConnectionsV2 {
            mViewDataBinding.connectionRV.hideSkeleton()
        }
    }

    companion object {
        const val CONNECTION_KEY: String = "Connection"
    }
}