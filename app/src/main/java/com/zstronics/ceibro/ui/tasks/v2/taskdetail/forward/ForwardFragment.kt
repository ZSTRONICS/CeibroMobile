package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.ContactsContract
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.FragmentForwardBinding
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.ForwardChipsAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.section.ConnectionAdapterSectionRecycler
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.section.ConnectionsSectionHeader
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
    private var searchedContacts = false
    private var searchedRecentContacts = false
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
                    shortToastNow(getString(R.string.please_select_contacts_to_forward))
                } else {
                    val bundle = Bundle()
                    bundle.putParcelableArray("forwardContacts", selectedContactList.toTypedArray())
                    navigateBackWithResult(Activity.RESULT_OK, bundle)
                }
            }
        }
    }

    lateinit var adapter: ConnectionAdapterSectionRecycler

    @Inject
    lateinit var chipAdapter: ForwardChipsAdapter

    private var sectionList: MutableList<ConnectionsSectionHeader> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.selectedContactsRV.adapter = chipAdapter

        sectionList.add(
            0,
            ConnectionsSectionHeader(mutableListOf(), getString(R.string.recent_connections))
        )
        sectionList.add(
            1,
            ConnectionsSectionHeader(mutableListOf(), getString(R.string.all_connections))
        )
        adapter = ConnectionAdapterSectionRecycler(requireContext(), sectionList)

        val linearLayoutManager = LinearLayoutManager(requireContext())
        mViewDataBinding.allContactsRV.layoutManager = linearLayoutManager
        mViewDataBinding.allContactsRV.setHasFixedSize(true)
        mViewDataBinding.allContactsRV.adapter = adapter

        viewModel.allConnections.observe(viewLifecycleOwner) {
            if (it != null) {
                if (searchedContacts) {
                    searchedContacts = false
                    val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
                    viewModel.filterContacts(searchQuery)
                } else {
                    sectionList.removeAt(1)
                    sectionList.add(
                        1, ConnectionsSectionHeader(
                            it,
                            getString(R.string.all_connections)
                        )
                    )
                    adapter.insertNewSection(
                        ConnectionsSectionHeader(
                            it,
                            getString(R.string.all_connections)
                        ), 1
                    )
                    adapter = adapter.apply {
                        setData(viewModel.oldSelectedContacts)
                    }
                    adapter.notifyDataChanged(sectionList)

                }
            }
        }

        viewModel.recentAllConnections.observe(viewLifecycleOwner) {
            if (it != null) {
                if (searchedRecentContacts) {
                    searchedRecentContacts = false
                    val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
                    viewModel.filterRecentContacts(searchQuery)
                } else {
                    sectionList.removeAt(0)
                    sectionList.add(
                        0,
                        ConnectionsSectionHeader(it, getString(R.string.recent_connections))
                    )
                    adapter.insertNewSection(
                        ConnectionsSectionHeader(
                            it,
                            getString(R.string.recent_connections)
                        ), 0
                    )
                    adapter = adapter.apply {
                        setData(viewModel.oldSelectedContacts)
                    }
                    adapter.notifyDataChanged(sectionList)
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
                val selectedOnes = viewModel.selectedContacts.value?.toMutableList()
                data.isChecked = false

                val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    searchedContacts = true
                    searchedRecentContacts = true
                }

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
                    viewModel.updateContacts(allContacts)
                    viewModel.updateOriginalContacts(allContacts)
                }

                val recentContacts = viewModel.recentOriginalConnections.toMutableList()
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
                    viewModel.updateRecentContacts(recentContacts)
                    viewModel.updateRecentOriginalContacts(recentContacts)
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

        adapter.itemClickListener =
            { childView: View, position: Int, contact: AllCeibroConnections.CeibroConnection ->
                connectionsAdapterClickListener.invoke(childView, position, contact)
            }


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
            // all connections click handling
            val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
            if (searchQuery.isNotEmpty()) {
                searchedContacts = true
                searchedRecentContacts = true
            }
            var selectedContacts: MutableList<AllCeibroConnections.CeibroConnection> =
                mutableListOf()
            val selected = adapter.sectionList[1].childItems.filter { it.isChecked }.map { it }
            val selectedRecent =
                adapter.sectionList[0].childItems.filter { it.isChecked }.map { it }
            selectedContacts.addAll(selectedRecent)
            selectedContacts.addAll(selected)
            selectedContacts = selectedContacts.distinct().toMutableList()
            val oldSelected =
                viewModel.selectedContacts.value?.toMutableList() ?: mutableListOf()

            if (oldSelected.isNotEmpty()) {
                val itemFound =
                    oldSelected.find { it.id == contact.id } // contact is found in the oldSelected list so we will remove it because it is already checked
                if (itemFound != null) {
                    oldSelected.removeIf { it.id == itemFound.id }
                    selectedContacts.removeIf { it.id == itemFound.id }
                }
                val combinedList = selectedContacts + oldSelected
                val distinctList = combinedList.distinctBy { it.id }.toMutableList()
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

            val recentOriginalConnections = viewModel.recentOriginalConnections.toMutableList()
            val existedInRecentOriginal = recentOriginalConnections.find { it.id == contact.id }
            if (existedInRecentOriginal != null) {
                existedInRecentOriginal.isChecked = contact.isChecked
                val index = recentOriginalConnections.indexOf(existedInRecentOriginal)
                recentOriginalConnections[index] = existedInRecentOriginal
                viewModel.updateRecentOriginalContacts(recentOriginalConnections)
            }

            val allConnections = viewModel.allConnections.value
            val existedInAllConnection = allConnections?.find { it.id == contact.id }
            if (existedInAllConnection != null) {
                existedInAllConnection.isChecked = contact.isChecked
                val index = allConnections.indexOf(existedInAllConnection)
                allConnections[index] = existedInAllConnection
                viewModel.updateContacts(allConnections)
            }

            val originalConnections = viewModel.originalConnections.toMutableList()
            val existedInOriginalConnection = originalConnections.find { it.id == contact.id }
            if (existedInOriginalConnection != null) {
                existedInOriginalConnection.isChecked = contact.isChecked
                val index = originalConnections.indexOf(existedInOriginalConnection)
                originalConnections[index] = existedInOriginalConnection
                viewModel.updateOriginalContacts(originalConnections)
            }
        }

//    private val recentConnectionsAdapterClickListener =
//        { childView: View, position: Int, contact: AllCeibroConnections.CeibroConnection ->
//            val searchQuery = mViewDataBinding.forwardSearchBar.query.toString()
//            if (searchQuery.isNotEmpty()) {
//                searchedContacts = true
//                searchedRecentContacts = true
//            }
//            var selectedContacts: MutableList<AllCeibroConnections.CeibroConnection> =
//                mutableListOf()
//            val selected = adapter.sectionList[0].childItems.filter { it.isChecked }.map { it }
//            val selectedAllConnections =
//                adapter.sectionList[1].childItems.filter { it.isChecked }.map { it }
//            selectedContacts.addAll(selected)
//            selectedContacts.addAll(selectedAllConnections)
//            selectedContacts = selectedContacts.distinct().toMutableList()
//
//            val oldSelected =
//                viewModel.recentOriginalConnections.filter { it.isChecked }.map { it }
//                    .toMutableList()
//            if (oldSelected.isNotEmpty()) {
//                val combinedList = selectedContacts + oldSelected
//                val distinctList = combinedList.distinct().toMutableList()
//                selectedContacts.clear()
//                selectedContacts.addAll(distinctList)
//                viewModel.selectedContacts.postValue(distinctList)
//            } else {
//                viewModel.selectedContacts.postValue(selectedContacts)
//            }
//
//            val allConnections = viewModel.allConnections.value
//            val existedInAllConnection = allConnections?.find { it.id == contact.id }
//            if (existedInAllConnection != null) {
//                existedInAllConnection.isChecked = contact.isChecked
//                val index = allConnections.indexOf(existedInAllConnection)
//                allConnections[index] = existedInAllConnection
//                viewModel.updateContacts(allConnections)
//            }
//
//            val originalConnections = viewModel.originalConnections.toMutableList()
//            val existedInOriginalConnection = originalConnections.find { it.id == contact.id }
//            if (existedInOriginalConnection != null) {
//                existedInOriginalConnection.isChecked = contact.isChecked
//                val index = originalConnections.indexOf(existedInOriginalConnection)
//                originalConnections[index] = existedInOriginalConnection
//                viewModel.updateOriginalContacts(originalConnections)
//            }
//
//            val allContacts = viewModel.recentOriginalConnections.toMutableList()
//            if (allContacts.isNotEmpty() && selectedContacts.isNotEmpty()) {
//                for (allItem in allContacts) {
//                    for (selectedItem in selectedContacts) {
//                        if (allItem.id == selectedItem.id) {
//                            val index = allContacts.indexOf(allItem)
//                            allContacts.set(index, selectedItem)
//                        }
//                    }
//                }
//                viewModel.updateRecentContacts(allContacts)
//                viewModel.updateRecentOriginalContacts(allContacts)
//            }
//        }

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