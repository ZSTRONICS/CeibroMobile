package com.zstronics.ceibro.ui.tasks.v3.bottomsheets.groups

import android.content.Context
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.SearchView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.CeibroConnectionGroupV2
import com.zstronics.ceibro.databinding.FragmentSelectGroupV2Binding
import com.zstronics.ceibro.ui.groupsv2.adapter.GroupV2Adapter
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import javax.inject.Inject


@AndroidEntryPoint
class SelectGroupFiltersV2Fragment(
    private val selectedGroupsList: ArrayList<CeibroConnectionGroupV2>
) :
    BaseNavViewModelFragment<FragmentSelectGroupV2Binding, IGroupFiltersV2.State, GroupFiltersVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: GroupFiltersVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_select_group_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                navigateBack()
            }

            R.id.tvClearAll -> {
                selectedGroup.clear()
                adapter.selectAllGroups(selectedGroup)
                ceibroConnectionGroupV2?.invoke(selectedGroup)
            }

            R.id.btnApply -> {
                ceibroConnectionGroupV2?.invoke(selectedGroup)
            }


            R.id.groupSearchClearBtn -> {
                mViewDataBinding.groupSearchBar.setQuery("", true)
                mViewDataBinding.groupSearchBar.clearFocus()
                mViewDataBinding.groupSearchBar.hideKeyboard()
                mViewDataBinding.groupSearchClearBtn.visibility = View.GONE
            }
        }
    }


    @Inject
    lateinit var adapter: GroupV2Adapter

    private var ceibroConnectionGroupV2: ((ArrayList<CeibroConnectionGroupV2>) -> Unit)? = null
    fun setGroupsCallBack(ceibroConnectionGroups: (ArrayList<CeibroConnectionGroupV2>) -> Unit) {
        ceibroConnectionGroupV2 = ceibroConnectionGroups
    }

    private var selectedGroup: ArrayList<CeibroConnectionGroupV2> = arrayListOf()
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mViewDataBinding.groupsRV.adapter = adapter

        selectedGroup = selectedGroupsList
        adapter.selectAllGroups(selectedGroup)

        viewModel.connectionGroups.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter.setList(it, true)
            } else {
                adapter.setList(mutableListOf(), true)
            }
        }
        viewModel.filteredGroups.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                adapter.setList(it, true)
            } else {
                adapter.setList(mutableListOf(), true)
            }
        }

        adapter.itemClickListener = { list ->

            selectedGroup.clear()
            selectedGroup.addAll(list)
        }
        adapter.deleteClickListener = { item ->
            viewModel.deleteConnectionGroup(item._id) {
                val allOriginalGroups = viewModel.originalConnectionGroups
                val groupFound = allOriginalGroups.find { it._id == item._id }
                if (groupFound != null) {
                    val index = allOriginalGroups.indexOf(groupFound)
                    allOriginalGroups.removeAt(index)
                    viewModel.originalConnectionGroups = allOriginalGroups
                }

                val adapterItemFound = adapter.groupListItems.find { it._id == item._id }
                if (adapterItemFound != null) {
                    val index1 = adapter.groupListItems.indexOf(adapterItemFound)
                    adapter.groupListItems.removeAt(index1)
                    adapter.notifyItemRemoved(index1)
                }
            }
        }
        adapter.renameClickListener = { item ->
            // openUpdateGroupSheet(item)
        }


        mViewDataBinding.groupSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.filterGroups(query)
                }
                if (!query.isNullOrEmpty()) {
                    mViewDataBinding.groupSearchClearBtn.visibility = View.VISIBLE
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.filterGroups(newText)
                }
                if (!newText.isNullOrEmpty()) {
                    mViewDataBinding.groupSearchClearBtn.visibility = View.VISIBLE
                }
                return true
            }
        })

        mViewDataBinding.groupMenuBtn.isEnabled = false
        val newColor = ContextCompat.getColor(requireContext(), R.color.appGrey3)
        mViewDataBinding.groupMenuBtn.setColorFilter(
            newColor,
            PorterDuff.Mode.SRC_IN
        )

        mViewDataBinding.selectionHeader.visibility = View.VISIBLE
        viewState.setAddTaskButtonVisibility.postValue(false)

    }

    override fun onStart() {
        super.onStart()
        try {
            EventBus.getDefault().register(this)
        } catch (_: Exception) {
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    private fun selectPopupWindow(
        v: View,
        selectGroupCallBack: (type: String) -> Unit
    ): PopupWindow {
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.groupv2_menu_dialog, null)

        val popupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true


        val selectGroup: TextView = view.findViewById(R.id.selectGroup)

        selectGroup.setOnClickListener {
            popupWindow.dismiss()
            selectGroupCallBack.invoke("select")
        }

        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height) {
            popupWindow.showAsDropDown(v, -200, -170)
        } else {
            popupWindow.showAsDropDown(v, -205, -60)
        }

        return popupWindow
    }


}