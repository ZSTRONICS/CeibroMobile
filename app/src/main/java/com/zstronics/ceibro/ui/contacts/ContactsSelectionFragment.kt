package com.zstronics.ceibro.ui.contacts

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.launchActivity
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_ID
import com.zstronics.ceibro.base.navgraph.host.NAVIGATION_Graph_START_DESTINATION_ID
import com.zstronics.ceibro.base.navgraph.host.NavHostPresenterActivity
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.databinding.FragmentContactsSelectionBinding
import com.zstronics.ceibro.ui.contacts.adapter.ContactSelectionHeaderAdapter
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.immutableListOf
import javax.inject.Inject

@AndroidEntryPoint
class ContactsSelectionFragment :
    BaseNavViewModelFragment<FragmentContactsSelectionBinding, IContactsSelection.State, ContactsSelectionVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ContactsSelectionVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_contacts_selection
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.confirmBtn -> {
                val selectedContacts: MutableList<SyncContactsRequest.CeibroContactLight> =
                    mutableListOf()
                for (item in adapter.listItems) {
                    val selected = item.items.filter { it.isChecked }.map { it }
                    selectedContacts.addAll(selected)
                }
//                val selectedContacts = adapter.dataList.filter { it.isChecked }.map { it }
                viewModel.syncContacts(selectedContacts) {
                    viewModel.sessionManager.saveSyncedContacts(selectedContacts)
                    navigateToDashboard()
                }
            }
            R.id.skipBtn -> {
                viewModel.syncContactsEnabled(false) {

                }
                navigateToDashboard()
            }
        }
    }

    @Inject
    lateinit var adapter: ContactSelectionHeaderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.contacts.observe(viewLifecycleOwner) {
            viewModel.groupDataByFirstLetter(it)
        }
        viewModel.contactsGroup.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }
        adapter.itemClickListener =
            { childView: View, position: Int, contacts: SyncContactsRequest.CeibroContactLight ->
                val selectedContacts: MutableList<SyncContactsRequest.CeibroContactLight> =
                    mutableListOf()
                for (item in adapter.listItems) {
                    val selected = item.items.filter { it.isChecked }.map { it }
                    selectedContacts.addAll(selected)
                }
                mViewDataBinding.confirmBtn.isEnabled = selectedContacts.isNotEmpty()
            }
        mViewDataBinding.recyclerView.adapter = adapter

        checkPermission(
            immutableListOf(
                Manifest.permission.READ_CONTACTS,
            )
        ) {
            val builder = MaterialAlertDialogBuilder(requireContext())
            builder.setMessage(resources.getString(R.string.sync_contacts_statement))
            builder.setCancelable(false)
            builder.setPositiveButton("Allow") { dialog, which ->
                // User clicked Allow button
                // Add your logic here
                viewModel.syncContactsEnabled(true) {
                    navigateToDashboard()
                }
            }
            builder.setNegativeButton("Deny") { dialog, which ->
                viewModel.syncContactsEnabled(false) {

                }
                mViewDataBinding.contactSelectionParentLayout.visibility = View.VISIBLE
            }
            builder.show()
            viewModel.loadContacts()
        }

        viewState.searchName.observe(viewLifecycleOwner) { search ->
            viewModel.filterContacts(search.lowercase())
        }
    }

    private fun navigateToDashboard() {
        launchActivity<NavHostPresenterActivity>(
            options = Bundle(),
            clearPrevious = true
        ) {
            putExtra(NAVIGATION_Graph_ID, R.navigation.home_nav_graph)
            putExtra(
                NAVIGATION_Graph_START_DESTINATION_ID,
                R.id.homeFragment
            )
        }
    }
}