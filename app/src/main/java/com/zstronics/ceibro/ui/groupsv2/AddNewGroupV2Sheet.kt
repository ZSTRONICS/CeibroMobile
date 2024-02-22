package com.zstronics.ceibro.ui.groupsv2

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncDBContactsList
import com.zstronics.ceibro.databinding.FragmentAddNewGroupV2Binding
import com.zstronics.ceibro.ui.contacts.toLightDBContacts
import com.zstronics.ceibro.ui.groupsv2.adapter.BottomSheetAllContactsAdapter
import com.zstronics.ceibro.ui.groupsv2.adapter.GroupSelectedContactsChipsAdapter
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class AddNewGroupV2Sheet(
    val connectionsV2Dao: ConnectionsV2Dao,
    val viewModel: GroupV2VM,
    val isUpdating: Boolean,
    var oldGroup: CeibroConnectionGroupV2? = null,
    var oldGroupContact: List<SyncDBContactsList.CeibroDBContactsLight>? = null
) : BottomSheetDialogFragment() {
    lateinit var binding: FragmentAddNewGroupV2Binding
    var isFirstRun = true

    private var _allLightConnections: MutableLiveData<MutableList<SyncDBContactsList.CeibroDBContactsLight>> =
        MutableLiveData()
    private val allLightConnections: MutableLiveData<MutableList<SyncDBContactsList.CeibroDBContactsLight>> =
        _allLightConnections

    private var originalLightConnections = mutableListOf<SyncDBContactsList.CeibroDBContactsLight>()
    var originalConnections = mutableListOf<SyncDBContactsList.CeibroDBContactsLight>()

    var selectedContacts: MutableLiveData<MutableList<SyncDBContactsList.CeibroDBContactsLight>?> =
        MutableLiveData(mutableListOf())

    var createGroupClickListener: ((groupName: String, contacts: List<String>) -> Unit)? = null
    var updateGroupClickListener: ((item: CeibroConnectionGroupV2, groupName: String, contacts: List<String>, groupNameChanged: Boolean) -> Unit)? =
        null
    var onGroupEdited: ((status: String) -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_new_group_v2,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }

    init {
        getAllConnectionsV2()
    }

    var adapter: BottomSheetAllContactsAdapter = BottomSheetAllContactsAdapter()

    private var chipAdapter: GroupSelectedContactsChipsAdapter = GroupSelectedContactsChipsAdapter()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.closeBtn.setOnClickListener {
            dismiss()
        }


        if (isUpdating) {
            selectedContacts.postValue(oldGroupContact?.toMutableList())

            oldGroup?.name?.let {
                val editableText: Editable = Editable.Factory.getInstance().newEditable(it)

                binding.groupNameText.text = editableText
                binding.saveGroupBtn.text = "Update"
            }
        }

        binding.saveGroupBtn.setOnClickListener {
            val groupName = binding.groupNameText.text.toString().trim()
            val selectedOnes = selectedContacts.value
            if (groupName.isEmpty()) {
                shortToastNow("Group name required")
            } else if (selectedOnes.isNullOrEmpty()) {
                shortToastNow("First select any contact to create group")
            } else {
                val selectedContactIds = selectedOnes.map { it.connectionId }
                if (isUpdating) {
                    oldGroup?.name?.let { name ->
                        oldGroupContact?.let { contact ->
                            if (name == groupName && selectedOnes == contact) {
                                shortToastNow("No data changed to update")
                            } else {
                                oldGroup?.let { item ->
                                    updateGroupClickListener?.invoke(
                                        item,
                                        groupName,
                                        selectedContactIds,
                                        !name.equals(groupName, true)
                                    )
                                }
                            }
                        }
                    }

                } else {
                    createGroupClickListener?.invoke(groupName, selectedContactIds)
                }


            }
        }

        binding.allContactsRV.adapter = adapter
        binding.selectedContactsRV.adapter = chipAdapter

        allLightConnections.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
//                if (searchedContacts) {
//                    searchedContacts = false
//                    val searchQuery = mViewDataBinding.searchBar.text.toString()
//                    viewModel.filterContacts(searchQuery)
//                }
                adapter.setList(it)
                if (isUpdating && isFirstRun) {
                    isFirstRun = false
                    oldGroupContact?.let {contact->
                        adapter.setSelectedList(contact)
                    }

                }
            } else {
                adapter.setList(mutableListOf())
            }
        }
        adapter.itemClickListener =
            { _: View, position: Int, contact: SyncDBContactsList.CeibroDBContactsLight ->
                val originalContacts = originalLightConnections
                val index = originalContacts.indexOf(contact)
                if (index > -1) {
                    originalContacts[index].isChecked = contact.isChecked
                }
                originalLightConnections = originalContacts

                val selectedOnes = selectedContacts.value ?: mutableListOf()
                if (contact.isChecked) {
                    selectedOnes.add(contact)
                    selectedContacts.postValue(selectedOnes)
                } else {
                    val selectedIndex = selectedOnes.indexOf(contact)
                    if (selectedIndex > -1) {
                        selectedOnes.removeAt(selectedIndex)
                    }
                    selectedContacts.postValue(selectedOnes)
                }
            }


        selectedContacts.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                chipAdapter.setList(it)
            } else {
                chipAdapter.setList(mutableListOf())
            }
        }

        chipAdapter.removeItemClickListener =
            { _: View, position: Int, data: SyncDBContactsList.CeibroDBContactsLight ->
                val allContacts = originalLightConnections
                val selectedOnes = selectedContacts.value
                data.isChecked = false
                /// Update All Contacts List


                if (allContacts.isNotEmpty()) {
                    val commonItem = allContacts.find { item1 ->
                        item1.connectionId == data.connectionId
                    }
                    if (commonItem != null) {
                        val index = allContacts.indexOf(commonItem)
                        allContacts[index] =
                            data            //set is used for updating the specific item
                    }

                    originalLightConnections = allContacts

                    val searchQuery = binding.groupSearchBar.query.toString()
                    if (searchQuery.isNotEmpty()) {
                        filterContacts(searchQuery)
                    } else {
                        _allLightConnections.postValue(allContacts)
                    }

//                    if (searchedContacts) {
//                        viewModel.filterContacts(searchQuery)
//                    }
                }

                //selected contacts also updated so that exact list be sent back on done
                if (!selectedOnes.isNullOrEmpty()) {
                    val index = selectedOnes.indexOfFirst { it.connectionId == data.connectionId }
                    if (index > -1) {
                        selectedOnes.removeAt(index)
                    }
                    selectedContacts.postValue(selectedOnes)
                }
            }

        binding.groupSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    filterContacts(query.trim())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    filterContacts(newText.trim())
                }
                return true
            }
        })

    }


    fun getAllConnectionsV2() {
        GlobalScope.launch {
            val connectionsData = connectionsV2Dao.getAll()
            val allContacts = connectionsData.groupDataByFirstLetter().toMutableList()

            val lightContacts = allContacts.toLightDBContacts()

            _allLightConnections.postValue(lightContacts.toMutableList())
            originalLightConnections = lightContacts.toMutableList()
        }
    }

    fun List<AllCeibroConnections.CeibroConnection>.groupDataByFirstLetter(): List<AllCeibroConnections.CeibroConnection> {
        val groupedData = this.groupBy {
            if (it.contactFirstName?.firstOrNull()?.isLetter() == true) {
                it.contactFirstName.first().lowercase()
            } else {
                '#'.toString()
            }
        }.toSortedMap(
            compareBy<String> { it != "#" }
                .then(compareBy { it.lowercase() })
                .then(compareByDescending { it == "#" })
        )

        val sortedItems = mutableListOf<AllCeibroConnections.CeibroConnection>()
        for (mapKey in groupedData.keys) {
            val sortedGroupItems =
                groupedData[mapKey]?.sortedBy { it.contactFirstName?.lowercase() }
                    ?: emptyList()
            sortedItems.addAll(sortedGroupItems)
        }

        return sortedItems
    }


    fun filterContacts(search: String) {
        if (search.isEmpty()) {
            if (originalLightConnections.isNotEmpty()) {
                _allLightConnections.postValue(originalLightConnections)
            } else {
                _allLightConnections.postValue(mutableListOf())
            }
            return
        }
        val filtered = originalLightConnections.filter {
            "${it.contactFirstName.lowercase()} ${it.contactSurName.lowercase()}".contains(
                search,
                true
            ) ||
                    it.phoneNumber.contains(search)
        }
        if (filtered.isNotEmpty())
            _allLightConnections.postValue(filtered.toMutableList())
        else
            _allLightConnections.postValue(mutableListOf())
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
        }
        return dialog
    }
}