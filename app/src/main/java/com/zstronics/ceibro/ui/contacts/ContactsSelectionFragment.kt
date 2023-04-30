package com.zstronics.ceibro.ui.contacts

import android.Manifest
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.viewModels
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.databinding.FragmentContactsSelectionBinding
import com.zstronics.ceibro.ui.contacts.adapter.ContactsSelectionAdapter
import com.zstronics.ceibro.ui.contacts.worker.ContactsSyncWorker
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
                viewModel.syncContacts(selectedContacts)
                startSyncing()
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
                startSyncing()
            }
            builder.setNegativeButton("Deny") { dialog, which ->
                // User clicked Deny button
                // Add your logic here
            }
            builder.show()
            viewModel.loadContacts()
        }
        CookiesManager.jwtToken =
            "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiI2NDRlNTQ0ZGU4NWFmYzhjNzI1YjZhZDAiLCJpYXQiOjE2ODI4NTg4MzksImV4cCI6MTY4NTYyMzYzOSwidHlwZSI6ImFjY2VzcyJ9.MNdJZuwM1hJ_w5JUALWlYJYa8a21prdS3h-D-xYDdKk"
    }

    private fun startSyncing() {
        Log.d("ContactsSyncWorker", "Worker initiated")
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<ContactsSyncWorker>()
            .setConstraints(constraints)
            .addTag(ContactsSyncWorker.WORK_TAG)
            .build()

        val workManager = WorkManager.getInstance(requireContext())
        workManager.enqueue(
            request
        )
    }
}