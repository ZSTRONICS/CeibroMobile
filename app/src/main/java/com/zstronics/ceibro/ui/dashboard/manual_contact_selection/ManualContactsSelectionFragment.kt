package com.zstronics.ceibro.ui.dashboard.manual_contact_selection

import android.os.Bundle
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.databinding.FragmentManualContactsSelectionBinding
import com.zstronics.ceibro.ui.contacts.adapter.ContactsSelectionAdapter
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class ManualContactsSelectionFragment :
    BaseNavViewModelFragment<FragmentManualContactsSelectionBinding, IManualContactsSelection.State, ManualContactsSelectionVM>() {
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ManualContactsSelectionVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_manual_contacts_selection
    override fun toolBarVisibility(): Boolean = true
    override fun getToolBarTitle() = getString(R.string.select_contacts)
    var searchedContacts = false
    var isLoadingTrue = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.confirmBtn -> {
                isLoadingTrue = true
                viewModel.loading(true)
                val selectedContacts = adapter.dataList.filter { it.isChecked }.map { it }
                viewModel.sessionManager.saveSyncedContacts(selectedContacts)
                startOneTimeContactSyncWorker(requireContext())
                mViewDataBinding.confirmBtn.isEnabled = false
            }
        }
    }

    @Inject
    lateinit var adapter: ContactsSelectionAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.contacts.observe(viewLifecycleOwner) {
            if (it != null) {
                if (searchedContacts) {
                    searchedContacts = false
                    val searchQuery = mViewDataBinding.searchBar.text.toString()
                    viewModel.filterContacts(searchQuery)
                }
                adapter.setList(it)
            }
        }
        adapter.itemClickListener =
            { childView: View, position: Int, contacts: SyncContactsRequest.CeibroContactLight ->
                val selectedContacts = adapter.dataList.filter { it.isChecked }.map { it }
                mViewDataBinding.confirmBtn.isEnabled = true
            }
        mViewDataBinding.recyclerView.adapter = adapter
        mViewDataBinding.searchBar.addTextChangedListener { editable ->
            val searchQuery = editable.toString()
            viewModel.filterContacts(searchQuery)
            searchedContacts = true
        }
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
    fun onGetAllContactsFromAPI(event: LocalEvents.UpdateConnections) {
        if (isLoadingTrue) {
            isLoadingTrue = false
            viewModel.loading(false)
            navigateBack()
        }
    }
}