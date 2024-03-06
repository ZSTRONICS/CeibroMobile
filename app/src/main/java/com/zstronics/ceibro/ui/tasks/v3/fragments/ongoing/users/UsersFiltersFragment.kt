package com.zstronics.ceibro.ui.tasks.v3.fragments.ongoing.users

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.provider.ContactsContract
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.CheckBox
import android.widget.PopupWindow
import android.widget.SearchView
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.FragmentSelectUsersBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.tasks.v2.newtask.assignee.adapter.AssigneeChipsAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.section.ConnectionAdapterSectionRecycler
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.section.ConnectionsSectionHeader
import com.zstronics.ceibro.utils.getDefaultCountryCode
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class UsersFiltersFragment(val connectionSelectedList: ArrayList<AllCeibroConnections.CeibroConnection>) :
    BaseNavViewModelFragment<FragmentSelectUsersBinding, IUsersFilters.State, UsersFiltersVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: UsersFiltersVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_select_users
    override fun toolBarVisibility(): Boolean = false
    private var searchedContacts = false
    private var searchedRecentContacts = false
    private var fullItemClickedForDone = false
    private var userConnectionCallBack: ((ArrayList<AllCeibroConnections.CeibroConnection>) -> Unit)? = null
    fun setConnectionCallBack(connectionCallback: (ArrayList<AllCeibroConnections.CeibroConnection>) -> Unit) {
        userConnectionCallBack = connectionCallback
    }

    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
            R.id.addNewContact -> {
                val intent = Intent(Intent.ACTION_INSERT, ContactsContract.Contacts.CONTENT_URI)
                startActivity(intent)
            }

            R.id.roleBtn -> {
                createPopupWindow(mViewDataBinding.roleBtn) {
                }
            }

            R.id.btnApply -> {


                val dataList = ArrayList<AllCeibroConnections.CeibroConnection>()
                dataList.addAll(chipAdapter.dataList)
                userConnectionCallBack?.invoke(dataList)
            }

            R.id.tvClearAll -> {
                viewModel.selectedContacts.postValue(mutableListOf())
                viewModel.getAllConnectionsV2 {  }
                val dataList = ArrayList<AllCeibroConnections.CeibroConnection>()

                userConnectionCallBack?.invoke(dataList)
            }
        }
    }


    lateinit var adapter: ConnectionAdapterSectionRecycler

    @Inject
    lateinit var chipAdapter: AssigneeChipsAdapter
    private var sectionList: MutableList<ConnectionsSectionHeader> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.selectedContactsRV.adapter = chipAdapter



        sectionList.add(
            0,
            ConnectionsSectionHeader(mutableListOf(), getString(R.string.all_connections))
        )
        adapter = ConnectionAdapterSectionRecycler(requireContext(), sectionList)

        viewModel.selectedContacts.value=connectionSelectedList

        val linearLayoutManager = LinearLayoutManager(requireContext())
        mViewDataBinding.allContactsRV.layoutManager = linearLayoutManager
        mViewDataBinding.allContactsRV.setHasFixedSize(true)
        mViewDataBinding.allContactsRV.adapter = adapter

        viewModel.allConnections.observe(viewLifecycleOwner) {
            if (it != null) {
                if (searchedContacts) {
                    searchedContacts = false
                    val searchQuery = mViewDataBinding.assigneeSearchBar.query.toString()
                    viewModel.filterContacts(searchQuery)
                } else {
                    sectionList.removeAt(0)
                    sectionList.add(
                        0, ConnectionsSectionHeader(
                            it,
                            getString(R.string.all_connections),
                            false
                        )
                    )
                    adapter.insertNewSection(
                        ConnectionsSectionHeader(
                            it,
                            getString(R.string.all_connections),
                            false
                        ), 0
                    )
                    adapter.notifyDataChanged(sectionList)
                }
            }
        }




        viewModel.selectedContacts.observe(viewLifecycleOwner) {
            if (it != null) {

                chipAdapter.setList(it)

            }
            if (fullItemClickedForDone) {
                fullItemClickedForDone = false
                val selectedContactList = it
                var selfAssigned = viewState.isSelfAssigned.value

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
        chipAdapter.removeItemClickListener =
            { childView: View, position: Int, data: AllCeibroConnections.CeibroConnection ->
                val allContacts = viewModel.originalConnections.toMutableList()
                val selectedOnes = viewModel.selectedContacts.value?.toMutableList()
                data.isChecked = false
                /// Update All Contacts List
                val searchQuery = mViewDataBinding.assigneeSearchBar.query.toString()
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
                    if (searchedContacts) {
                        viewModel.filterContacts(searchQuery)
                    }
                }

                val recentContacts = viewModel.recentOriginalConnections.toMutableList()
                /// Update Recent Contacts List
                if (recentContacts.isNotEmpty()) {
                    val commonItem = recentContacts.find { item1 ->
                        item1.id == data.id
                    }
                    if (commonItem != null) {
                        val index = recentContacts.indexOf(commonItem)
                        recentContacts.set(
                            index,
                            data
                        )//set is used for updating the specific item
                    }

                    viewModel.updateRecentContacts(recentContacts)
                    viewModel.updateRecentOriginalContacts(recentContacts)
                    if (searchedContacts) {
                        viewModel.filterRecentContacts(searchQuery)
                    }
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

        adapter.itemClickListener =
            { childView: View, position: Int, contact: AllCeibroConnections.CeibroConnection ->
                fullItemClickedForDone = false
                connectionsAdapterClickListener.invoke(childView, position, contact)
            }

        adapter.fullItemClickListener =
            { childView: View, position: Int, contact: AllCeibroConnections.CeibroConnection ->
                fullItemClickedForDone = true
                connectionsAdapterClickListener.invoke(childView, position, contact)
            }

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

                    val currentContactList: ArrayList<AllCeibroConnections.CeibroConnection> =
                        arrayListOf()
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
            // all connections click handling
            val searchQuery = mViewDataBinding.assigneeSearchBar.query.toString()
            if (searchQuery.isNotEmpty()) {
                searchedContacts = true
                searchedRecentContacts = true
            }

            var selectedContacts: MutableList<AllCeibroConnections.CeibroConnection> =
                mutableListOf()
            val selected = adapter.sectionList[0].childItems.filter { it.isChecked }.map { it }


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

    @SuppressLint("MissingInflatedId")
    private fun createPopupWindow(
        v: View,
        callback: (String) -> Unit
    ): PopupWindow {
        var selectionCounter = 0
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.role_type_menu_dialog, null)

        val popupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )


        val applyBtn: AppCompatTextView = view.findViewById(R.id.applyBtn)
        val cbSelectAll: CheckBox = view.findViewById(R.id.cbSelectAll)
        val cbAll: CheckBox = view.findViewById(R.id.cbAll)
        val cbCreator: CheckBox = view.findViewById(R.id.cbCreator)
        val cbAssignee: CheckBox = view.findViewById(R.id.cbAssignee)
        val cbViewer: CheckBox = view.findViewById(R.id.cbViewer)
        val cbApproval: CheckBox = view.findViewById(R.id.cbApproval)


// Usage in your click listeners
        cbAll.setOnClickListener {
            selectionCounter =
                updateSelectionCounter(cbAll.isChecked, selectionCounter, cbSelectAll)
        }

        cbCreator.setOnClickListener {
            selectionCounter =
                updateSelectionCounter(cbCreator.isChecked, selectionCounter, cbSelectAll)
        }

        cbAssignee.setOnClickListener {
            selectionCounter =
                updateSelectionCounter(cbAssignee.isChecked, selectionCounter, cbSelectAll)
        }

        cbViewer.setOnClickListener {
            selectionCounter =
                updateSelectionCounter(cbViewer.isChecked, selectionCounter, cbSelectAll)
        }

        cbApproval.setOnClickListener {
            selectionCounter =
                updateSelectionCounter(cbApproval.isChecked, selectionCounter, cbSelectAll)
        }

        cbSelectAll.setOnClickListener {
            val isChecked = cbSelectAll.isChecked
            cbAll.isChecked = isChecked
            cbCreator.isChecked = isChecked
            cbAssignee.isChecked = isChecked
            cbViewer.isChecked = isChecked
            cbApproval.isChecked = isChecked
            selectionCounter = if (isChecked) {
                5
            } else {
                0
            }

        }



        applyBtn.setOnClickListener {
            popupWindow.dismiss()
        }
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true


        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        popupWindow.showAsDropDown(v, 0, -125)


        return popupWindow
    }

    private fun updateSelectionCounter(
        isChecked: Boolean,
        selectionCounter: Int,
        cbSelectAll: CheckBox
    ): Int {
        val updatedCounter = selectionCounter + if (isChecked) 1 else -1
        cbSelectAll.isChecked = updatedCounter == 5
        return updatedCounter
    }
}