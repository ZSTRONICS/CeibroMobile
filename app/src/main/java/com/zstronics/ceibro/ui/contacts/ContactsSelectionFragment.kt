package com.zstronics.ceibro.ui.contacts

import android.Manifest
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.databinding.FragmentContactsSelectionBinding
import com.zstronics.ceibro.ui.contacts.adapter.ContactsSelectionAdapter
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
                val selectedContacts = adapter.dataList.filter { it.isChecked }.map { it }
                viewModel.syncContacts(selectedContacts) {
                    navigateBack()
                    TODO("move to next screen")
                }
            }
        }
    }

    @Inject
    lateinit var adapter: ContactsSelectionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.contacts.observe(viewLifecycleOwner) {
            adapter.setList(it)
        }
        adapter.itemClickListener =
            { childView: View, position: Int, contacts: SyncContactsRequest.CeibroContactLight ->
                val selectedContacts = adapter.dataList.filter { it.isChecked }.map { it }
                mViewDataBinding.confirmBtn.isEnabled = selectedContacts.isNotEmpty()
            }
        mViewDataBinding.recyclerView.adapter = adapter

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
                viewModel.syncContactsEnabled(true) {
                    navigateBack()
                    TODO("move to next screen")
                }
            }
            builder.setNegativeButton("Deny") { dialog, which ->
                viewModel.syncContactsEnabled(false) {

                }
            }
            builder.show()
            viewModel.loadContacts()
        }
        CookiesManager.jwtToken =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2NDRlNTI4MWU4NWFmYzhjNzI1YjZhYmMiLCJpYXQiOjE2ODI5NDIzMTQsImV4cCI6MTY4NTcwNzExNCwidHlwZSI6ImFjY2VzcyJ9.4f046DCoPE8buN5tpno8mnAnrwk27dIYo3n0Xe8aYLA"
    }
}