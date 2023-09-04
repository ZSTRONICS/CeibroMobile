package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.gone
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.visible
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.FragmentForwardBinding
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.ForwardChipsAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.ForwardSelectionAdapter
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import javax.inject.Inject

@AndroidEntryPoint
class ForwardFragment :
    BaseNavViewModelFragment<FragmentForwardBinding, IForward.State, ForwardVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ForwardVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_forward
    override fun toolBarVisibility(): Boolean = false
    var searchedContacts = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
            R.id.addNewContact -> {
                val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
                startActivity(intent)
            }
            R.id.doneBtn -> {
                val selectedContactList = viewModel.selectedContacts.value
                if (selectedContactList.isNullOrEmpty()) {
                    shortToastNow("Please select contacts to forward")
                } else {
                    val bundle = Bundle()
                    bundle.putParcelableArray("forwardContacts", selectedContactList.toTypedArray())
                    navigateBackWithResult(Activity.RESULT_OK, bundle)
                }
            }
        }
    }


    @Inject
    lateinit var adapter: ForwardSelectionAdapter

    @Inject
    lateinit var recentAdapter: ForwardSelectionAdapter

    @Inject
    lateinit var chipAdapter: ForwardChipsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.allContactsRV.adapter = adapter
        mViewDataBinding.recentConnectionsRV.adapter = recentAdapter
        mViewDataBinding.selectedContactsRV.adapter = chipAdapter

        mViewDataBinding.allContactsRV.isNestedScrollingEnabled = false
        mViewDataBinding.recentConnectionsRV.isNestedScrollingEnabled = false
        mViewDataBinding.selectedContactsRV.isNestedScrollingEnabled = false

        viewModel.allConnections.observe(viewLifecycleOwner) {
            if (it != null) {
                if (searchedContacts) {
                    searchedContacts = false
                    val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
                    viewModel.filterContacts(searchQuery)
                } else {
                    adapter.setList(it, viewModel.oldSelectedContacts)
                }

                if (it.isEmpty()) {
                    mViewDataBinding.allConnectionsLayout.gone()
                } else {
                    mViewDataBinding.allConnectionsLayout.visible()
                }
            }
        }

        viewModel.recentAllConnections.observe(viewLifecycleOwner) {
            if (it != null) {
                if (searchedContacts) {
                    searchedContacts = false
                    val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
                    viewModel.filterRecentContacts(searchQuery)
                } else {
                    recentAdapter.setList(it, viewModel.oldSelectedContacts)
                }
                if (it.isEmpty()) {
                    mViewDataBinding.recentConnectionsLayout.gone()
                } else {
                    mViewDataBinding.recentConnectionsLayout.visible()
                }
            }
        }

        viewModel.selectedContacts.observe(viewLifecycleOwner) {
            if (it != null) {
                chipAdapter.setList(it)
            }
        }
        chipAdapter.removeItemClickListener =
            { childView: View, position: Int, data: AllCeibroConnections.CeibroConnection ->
                val allContacts = viewModel.originalConnections.toMutableList()
                val recentContacts = viewModel.recentOriginalConnections.toMutableList()
                val selectedOnes = viewModel.selectedContacts.value?.toMutableList()
                data.isChecked = false

                if (allContacts.isNotEmpty()) {
                    val commonItem = allContacts.find { item1 ->
                        item1.id == data.id
                    }
                    if (commonItem != null) {
                        val index = allContacts.indexOf(commonItem)
                        allContacts.set(
                            index,
                            data
                        )            //set is used for updating the specific item
                    }

                    val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
                    if (searchQuery.isNotEmpty()) {
                        searchedContacts = true
                    }
                    viewModel.updateContacts(allContacts)
                }

                if (recentContacts.isNotEmpty()) {
                    val commonItem = recentContacts.find { item1 ->
                        item1.id == data.id
                    }
                    if (commonItem != null) {
                        val index = recentContacts.indexOf(commonItem)
                        recentContacts.set(
                            index,
                            data
                        )            //set is used for updating the specific item
                    }

                    val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
                    if (searchQuery.isNotEmpty()) {
                        searchedContacts = true
                    }
                    viewModel.updateRecentContacts(recentContacts)
                }

                //selected contacts also updated so that exact list be sent back on done
                if (!selectedOnes.isNullOrEmpty()) {
                    val commonItem = selectedOnes.find { item1 ->
                        item1.id == data.id
                    }
                    if (commonItem != null) {
                        val index = selectedOnes.indexOf(commonItem)
                        selectedOnes.removeAt(index)            //set is used for updating the specific item
                    }
                    viewModel.selectedContacts.postValue(selectedOnes)
                }
            }

        adapter.itemClickListener = connectionsAdapterClickListener
        recentAdapter.itemClickListener = recentConnectionsAdapterClickListener


        mViewDataBinding.forwardSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.filterContacts(query)
                    viewModel.filterRecentContacts(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.filterContacts(newText)
                    viewModel.filterRecentContacts(newText)
                }
                return true
            }
        })

    }

    private val connectionsAdapterClickListener =
        { childView: View, position: Int, contact: AllCeibroConnections.CeibroConnection ->
            val selectedContacts: MutableList<AllCeibroConnections.CeibroConnection> =
                mutableListOf()
            val selected = adapter.dataList.filter { it.isChecked }.map { it }
            selectedContacts.addAll(selected)
            val oldSelected =
                viewModel.originalConnections.filter { it.isChecked }.map { it }.toMutableList()
            if (oldSelected.isNotEmpty()) {
                val combinedList = selectedContacts + oldSelected
                val distinctList = combinedList.distinct().toMutableList()
                selectedContacts.clear()
                selectedContacts.addAll(distinctList)
                viewModel.selectedContacts.postValue(distinctList)
            } else {
                viewModel.selectedContacts.postValue(selectedContacts)
            }

            val recentAllConnections = viewModel.recentAllConnections.value
            val existedInRecent = recentAllConnections?.find { it.id == contact.id }
            if (existedInRecent != null) {
                existedInRecent.isChecked = contact.isChecked
                val index = recentAllConnections.indexOf(existedInRecent)
                recentAllConnections[index] = existedInRecent
                viewModel.updateRecentContacts(recentAllConnections)
            }

            val allContacts = viewModel.originalConnections.toMutableList()
            if (allContacts.isNotEmpty() && selectedContacts.isNotEmpty()) {
                for (allItem in allContacts) {
                    for (selectedItem in selectedContacts) {
                        if (allItem.id == selectedItem.id) {
                            val index = allContacts.indexOf(allItem)
                            allContacts.set(index, selectedItem)
                        }
                    }
                }

                val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    searchedContacts = true
                }
                viewModel.updateContacts(allContacts)
            }
        }

    private val recentConnectionsAdapterClickListener =
        { childView: View, position: Int, contact: AllCeibroConnections.CeibroConnection ->
            val selectedContacts: MutableList<AllCeibroConnections.CeibroConnection> =
                mutableListOf()
            val selected = recentAdapter.dataList.filter { it.isChecked }.map { it }
            selectedContacts.addAll(selected)
            val oldSelected =
                viewModel.recentOriginalConnections.filter { it.isChecked }.map { it }
                    .toMutableList()
            if (oldSelected.isNotEmpty()) {
                val combinedList = selectedContacts + oldSelected
                val distinctList = combinedList.distinct().toMutableList()
                selectedContacts.clear()
                selectedContacts.addAll(distinctList)
                viewModel.selectedContacts.postValue(distinctList)
            } else {
                viewModel.selectedContacts.postValue(selectedContacts)
            }

            val allConnections = viewModel.allConnections.value
            val existedInRecent = allConnections?.find { it.id == contact.id }
            if (existedInRecent != null) {
                existedInRecent.isChecked = contact.isChecked
                val index = allConnections.indexOf(existedInRecent)
                allConnections[index] = existedInRecent
                viewModel.updateContacts(allConnections)
            }

            val allContacts = viewModel.recentOriginalConnections.toMutableList()
            if (allContacts.isNotEmpty() && selectedContacts.isNotEmpty()) {
                for (allItem in allContacts) {
                    for (selectedItem in selectedContacts) {
                        if (allItem.id == selectedItem.id) {
                            val index = allContacts.indexOf(allItem)
                            allContacts.set(index, selectedItem)
                        }
                    }
                }

                val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    searchedContacts = true
                }
                viewModel.updateRecentContacts(allContacts)
            }
        }

    override fun onResume() {
        super.onResume()
        loadConnections(true)
        mViewDataBinding.forwardSearchBar.setQuery("", true)
    }

    private fun loadConnections(skeletonVisible: Boolean) {
        if (skeletonVisible) {
            mViewDataBinding.allContactsRV.loadSkeleton(R.layout.layout_invitations_box) {
                itemCount(10)
                color(R.color.appLightGrey)
            }

            viewModel.getAllConnectionsV2 {
                mViewDataBinding.allContactsRV.hideSkeleton()
                val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.filterContacts(searchQuery)
                }
            }
        } else {
            viewModel.getAllConnectionsV2 {
                val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.filterContacts(searchQuery)
                }
            }
        }
    }
}