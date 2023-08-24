package com.zstronics.ceibro.ui.tasks.v2.newtask.assignee

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.RecyclerView
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.gone
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.extensions.visible
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.FragmentAssigneeBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.v2.newtask.assignee.adapter.AssigneeChipsAdapter
import com.zstronics.ceibro.ui.tasks.v2.newtask.assignee.adapter.AssigneeSelectionAdapter
import com.zstronics.ceibro.utils.getDefaultCountryCode
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class AssigneeFragment :
    BaseNavViewModelFragment<FragmentAssigneeBinding, IAssignee.State, AssigneeVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: AssigneeVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_assignee
    override fun toolBarVisibility(): Boolean = false
    var searchedContacts = false
    var runUIOnce = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
            R.id.addNewContact -> {
                val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
                startActivity(intent)
            }
            R.id.doneBtn -> {
                val selectedContactList = viewModel.selectedContacts.value
                val selfAssigned = viewState.isSelfAssigned.value
                if (selectedContactList.isNullOrEmpty() && selfAssigned == false) {
                    shortToastNow("Please select contacts to proceed")
                } else {
                    val bundle = Bundle()
                    bundle.putParcelableArray("contacts", selectedContactList?.toTypedArray())
                    bundle.putBoolean("self-assign", selfAssigned ?: false)
                    navigateBackWithResult(Activity.RESULT_OK, bundle)
                }
            }
        }
    }


    @Inject
    lateinit var adapter: AssigneeSelectionAdapter

    @Inject
    lateinit var recentAdapter: AssigneeSelectionAdapter

    @Inject
    lateinit var chipAdapter: AssigneeChipsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.allContactsRV.adapter = adapter
        mViewDataBinding.selectedContactsRV.adapter = chipAdapter
        mViewDataBinding.allContactsRV.layoutManager?.isItemPrefetchEnabled = true
        mViewDataBinding.allContactsRV.setRecycledViewPool(RecyclerView.RecycledViewPool())
        mViewDataBinding.recentConnectionsRV.adapter = recentAdapter

        viewModel.allConnections.observe(viewLifecycleOwner) {
            if (it != null) {
                if (searchedContacts) {
                    searchedContacts = false
                    val searchQuery = mViewDataBinding.assigneeSearchBar.query.toString()
                    viewModel.filterContacts(searchQuery)
                } else {
                    adapter.setList(it)
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
                    val searchQuery = mViewDataBinding.assigneeSearchBar.query.toString()
                    viewModel.filterRecentContacts(searchQuery)
                } else {
                    recentAdapter.setList(it)
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

                    val searchQuery = mViewDataBinding.assigneeSearchBar.query.toString()
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

                    val searchQuery = mViewDataBinding.assigneeSearchBar.query.toString()
                    if (searchQuery.isNotEmpty()) {
                        searchedContacts = true
                    }
                    viewModel.updateRecentContacts(recentContacts)
                }

                //selected contacts also updated so that exact list be sent back on done
                if (!selectedOnes.isNullOrEmpty()) {
                    val index = selectedOnes.indexOfFirst { it.id == data.id }
                    if (index != -1) {
                        selectedOnes.removeAt(index)
                        if (data.id == viewModel.user?.id) {
                            viewState.isSelfAssigned.value = false
                        }
                    }
                    viewModel.selectedContacts.postValue(selectedOnes)
                }
            }

        adapter.itemClickListener = connectionsAdapterClickListener
        recentAdapter.itemClickListener = recentConnectionsAdapterClickListener

        mViewDataBinding.selfAssignCheckBox.setOnCheckedChangeListener { buttonView, isChecked ->
            viewState.isSelfAssigned.value = isChecked
            val currentUser = viewModel.user
            if (isChecked) {
                if (currentUser != null) {
                    val pn =
                        PhoneNumberUtil.getInstance()
                            .parse(
                                currentUser.phoneNumber,
                                getDefaultCountryCode(requireContext())
                            )

                    val userContact = AllCeibroConnections.CeibroConnection(
                        contactFullName = null,
                        contactFirstName = "M",
                        contactSurName = "e",
                        countryCode = "+${pn.countryCode}",
                        id = currentUser.id,
                        createdAt = "",
                        isBlocked = false,
                        isCeiborUser = true,
                        isChecked = true,
                        isSilent = false,
                        phoneNumber = currentUser.phoneNumber,
                        updatedAt = "",
                        userCeibroData = AllCeibroConnections.CeibroConnection.UserCeibroData(
                            companyName = currentUser.companyName ?: "",
                            email = currentUser.email,
                            firstName = currentUser.firstName,
                            id = currentUser.id,
                            jobTitle = currentUser.jobTitle,
                            phoneNumber = currentUser.phoneNumber,
                            profilePic = currentUser.profilePic,
                            surName = currentUser.surName
                        )
                    )

                    val currentContactList: ArrayList<AllCeibroConnections.CeibroConnection> = arrayListOf()
                    currentContactList.add(userContact)
                    val selectedContacts = viewModel.selectedContacts.value?.toMutableList()
                    if (selectedContacts.isNullOrEmpty()) {
                        viewModel.selectedContacts.postValue(currentContactList)
                    } else {
                        val selectedItem = selectedContacts.find { item1 ->
                            item1.id == currentUser.id
                        }
                        if (selectedItem != null) {       //if not null then current user contact is already part of selected contact list
                            val combinedList = selectedContacts + currentContactList
                            val distinctList = combinedList.distinct().toMutableList()
                            selectedContacts.clear()
                            selectedContacts.addAll(distinctList)
                            viewModel.selectedContacts.postValue(distinctList)
                        } else {
                            selectedContacts.addAll(currentContactList)
                        }
                        viewModel.selectedContacts.postValue(selectedContacts)
                    }
                }
            } else {
                val selectedContacts = viewModel.selectedContacts.value
                if (!selectedContacts.isNullOrEmpty()) {
                    val currentSelected =
                        selectedContacts.find { it1 -> it1.id == currentUser?.id }
                    if (currentSelected != null) {
                        val index = selectedContacts.indexOf(currentSelected)
                        selectedContacts.removeAt(index)
                    }
                    viewModel.selectedContacts.postValue(selectedContacts)
                }
            }
        }

        mViewDataBinding.assigneeSearchBar.setOnQueryTextListener(object :
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

            val selectedContacts: MutableSet<AllCeibroConnections.CeibroConnection> =
                mutableSetOf()
            selectedContacts.addAll(adapter.dataList.filter { it.isChecked })

            val oldSelected = viewModel.selectedContacts.value?.toHashSet()

            if (!oldSelected.isNullOrEmpty()) {
                val tappedItem = selectedContacts.find { it.id == contact.id }
                if (tappedItem != null) {       //if not null then this contact has recently came and it means tapped item needs to be added
                    selectedContacts.addAll(oldSelected)
                } else {
                    //if NULL then this contact has to be removed from selected contact list and then update list
                    oldSelected.removeIf { it.id == contact.id }
                    selectedContacts.addAll(oldSelected)
                }
                val distinctList = selectedContacts.toList().distinct().toMutableList()
                viewModel.selectedContacts.postValue(distinctList)
            } else {
                viewModel.selectedContacts.postValue(selectedContacts.toList().toMutableList())
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
                for (selectedItem in selectedContacts) {
                    val index = allContacts.indexOfFirst { it.id == selectedItem.id }
                    if (index != -1) {
                        allContacts[index] = selectedItem
                    }
                }

                val searchQuery = mViewDataBinding.assigneeSearchBar.query.toString()
                searchedContacts = searchQuery.isNotEmpty()
                viewModel.updateContacts(allContacts)
            }
        }

    private val recentConnectionsAdapterClickListener =
        { childView: View, position: Int, contact: AllCeibroConnections.CeibroConnection ->

            val selectedContacts: MutableSet<AllCeibroConnections.CeibroConnection> =
                mutableSetOf()
            selectedContacts.addAll(recentAdapter.dataList.filter { it.isChecked })

            val oldSelected = viewModel.selectedContacts.value?.toHashSet()

            if (!oldSelected.isNullOrEmpty()) {
                val tappedItem = selectedContacts.find { it.id == contact.id }
                if (tappedItem != null) {       //if not null then this contact has recently came and it means tapped item needs to be added
                    selectedContacts.addAll(oldSelected)
                } else {
                    //if NULL then this contact has to be removed from selected contact list and then update list
                    oldSelected.removeIf { it.id == contact.id }
                    selectedContacts.addAll(oldSelected)
                }
                val distinctList = selectedContacts.toList().distinct().toMutableList()
                viewModel.selectedContacts.postValue(distinctList)
            } else {
                viewModel.selectedContacts.postValue(selectedContacts.toList().toMutableList())
            }

            val recentAllConnections = viewModel.originalConnections.toMutableList()
            val existedInRecent = recentAllConnections.find { it.id == contact.id }
            if (existedInRecent != null) {
                existedInRecent.isChecked = contact.isChecked
                val index = recentAllConnections.indexOf(existedInRecent)
                recentAllConnections[index] = existedInRecent
                viewModel.updateContacts(recentAllConnections)
            }

            val allContacts = viewModel.recentOriginalConnections.toMutableList()
            if (allContacts.isNotEmpty() && selectedContacts.isNotEmpty()) {
                for (selectedItem in selectedContacts) {
                    val index = allContacts.indexOfFirst { it.id == selectedItem.id }
                    if (index != -1) {
                        allContacts[index] = selectedItem
                    }
                }

                val searchQuery = mViewDataBinding.assigneeSearchBar.query.toString()
                searchedContacts = searchQuery.isNotEmpty()
                viewModel.updateRecentContacts(allContacts)
            }
        }
    override fun onResume() {
        super.onResume()
        val handler = Handler()
        handler.postDelayed({
            loadConnections(true)
        }, 80)
    }

    private fun loadConnections(skeletonVisible: Boolean) {
        if (skeletonVisible) {
            mViewDataBinding.allContactsRV.loadSkeleton(R.layout.layout_invitations_box) {
                itemCount(10)
                color(R.color.appLightGrey)
            }

            viewModel.getAllConnectionsV2 {
                mViewDataBinding.allContactsRV.hideSkeleton()
                val searchQuery = mViewDataBinding.assigneeSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.filterContacts(searchQuery)
                }
            }
        } else {
            viewModel.getAllConnectionsV2 {
                val searchQuery = mViewDataBinding.assigneeSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.filterContacts(searchQuery)
                }
            }
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
        loadConnections(false)
    }

}