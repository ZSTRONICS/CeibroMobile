package com.zstronics.ceibro.ui.tasks.v2.newtask.assignee

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.FragmentAssigneeBinding
import com.zstronics.ceibro.ui.tasks.v2.newtask.assignee.adapter.AssigneeChipsAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.assignee.adapter.AssigneeSelectionHeaderAdapter
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import javax.inject.Inject

@AndroidEntryPoint
class AssigneeFragment :
    BaseNavViewModelFragment<FragmentAssigneeBinding, IAssignee.State, AssigneeVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: AssigneeVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_assignee
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
    }



    @Inject
    lateinit var adapter: AssigneeSelectionHeaderAdapter
    @Inject
    lateinit var chipAdapter: AssigneeChipsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.allContactsRV.adapter = adapter
        mViewDataBinding.selectedContactsRV.adapter = chipAdapter

        viewModel.allConnections.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.groupDataByFirstLetter(it)
            }
        }

        viewModel.allGroupedConnections.observe(viewLifecycleOwner) {
            if (it != null) {
                adapter.setList(it)
            }
        }

        viewModel.selectedContacts.observe(viewLifecycleOwner) {
            if (it != null) {
                chipAdapter.setList(it)
            }
        }
        chipAdapter.removeItemClickListener = { childView: View, position: Int, data: AllCeibroConnections.CeibroConnection ->
            val allContacts = viewModel.originalConnections.toMutableList()
            if (allContacts.isNotEmpty()) {
                data.isChecked = false

                val commonItem = allContacts.find { item1 ->
                    item1.id == data.id
                }
                if (commonItem != null) {
                    val index = allContacts.indexOf(commonItem)
                    allContacts.set(index, data)            //set is used for updating the specific item
                }
                viewModel.updateContacts(allContacts)
            }
        }

        adapter.itemClickListener =
            { childView: View, position: Int, contact: AllCeibroConnections.CeibroConnection ->
                val selectedContacts: MutableList<AllCeibroConnections.CeibroConnection> =
                    mutableListOf()
                for (item in adapter.listItems) {
                    val selected = item.items.filter { it.isChecked }.map { it }
                    selectedContacts.addAll(selected)
                }
                viewModel.selectedContacts.postValue(selectedContacts)

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

                    viewModel.updateContacts(allContacts)
                }
            }



//        viewState.searchName.observe(viewLifecycleOwner) { search ->
//            viewModel.filterContacts(search.lowercase())
//        }
//        mViewDataBinding.searchBar.setOnQueryTextListener(object :
//            SearchView.OnQueryTextListener {
//            override fun onQueryTextSubmit(query: String?): Boolean {
//                if (query != null) {
//                    viewModel.filterContacts(query)
//                }
//                return true
//            }
//            override fun onQueryTextChange(newText: String?): Boolean {
//                if (newText != null) {
//                    viewModel.filterContacts(newText)
//                }
//                return true
//            }
//
//        })
    }

    override fun onResume() {
        super.onResume()
        loadConnections(true)
    }

    private fun loadConnections(skeletonVisible: Boolean) {
        if (skeletonVisible) {
            mViewDataBinding.allContactsRV.loadSkeleton(R.layout.layout_invitations_box) {
                itemCount(10)
                color(R.color.appLightGrey)
            }

            viewModel.getAllConnectionsV2 {
                mViewDataBinding.allContactsRV.hideSkeleton()
            }
        } else {
            viewModel.getAllConnectionsV2 { }
        }
    }

}