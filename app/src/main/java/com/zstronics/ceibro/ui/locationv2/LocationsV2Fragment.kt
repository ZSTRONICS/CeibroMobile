package com.zstronics.ceibro.ui.locationv2

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.extensions.isVisible
import com.zstronics.ceibro.base.extensions.showKeyboard
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentLocatinsV2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationsV2Fragment :
    BaseNavViewModelFragment<FragmentLocatinsV2Binding, ILocations.State, LocationsV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: LocationsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_locatins_v2
    private var layoutPressed = ""
    private var isKeyboardShown = false
    private val llTo = "llTo"
    private val llFrom = "llFrom"
    private val llHidden = "llHidden"
    private val spinnerItems = arrayOf("Item 1", "Item 2", "Item 3")

    override fun toolBarVisibility(): Boolean = false


    override fun onClick(id: Int) {
        when (id) {
            R.id.projectFilterBtn -> {

                viewState.isFilterVisible.value?.let { currentValue ->
                    viewState.isFilterVisible.value = !currentValue
                    updateCompoundDrawable()
                }


            }

            R.id.tvTo -> {
                layoutPressed = llTo
                if (isKeyboardShown) {
                    manageLayoutsVisibilityWithKeyboardShown()
                } else {
                    manageLayoutsVisibilityWithKeyboardHidden()
                }
            }

            R.id.tvfrom -> {
                layoutPressed = llFrom
                if (isKeyboardShown) {
                    manageLayoutsVisibilityWithKeyboardShown()
                } else {
                    manageLayoutsVisibilityWithKeyboardHidden()
                }
            }

            R.id.tvHidden -> {
                layoutPressed = llHidden
                if (isKeyboardShown) {
                    manageLayoutsVisibilityWithKeyboardShown()
                } else {
                    manageLayoutsVisibilityWithKeyboardHidden()
                }
            }

            R.id.cancelSearch -> {

                isKeyboardShown = false
                mViewDataBinding.projectSearchBar.hideKeyboard()
                mViewDataBinding.projectsSearchCard.visibility = View.GONE
                mViewDataBinding.projectSearchBtn.visibility = View.VISIBLE
                mViewDataBinding.projectSearchBar.setQuery("", false)
            }

            R.id.projectSearchBtn -> {
                isKeyboardShown = true
                showKeyboard()
                checkLayoutsVisibility()
                mViewDataBinding.projectSearchBar.requestFocus()
                mViewDataBinding.projectsSearchCard.visibility = View.VISIBLE
                mViewDataBinding.projectSearchBtn.visibility = View.GONE
            }

        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, spinnerItems)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        mViewDataBinding.spinner.adapter = adapter
        mViewDataBinding.spinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    val selectedItem = spinnerItems[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
    }

    private fun manageLayoutsVisibilityWithKeyboardShown() {

        if (layoutPressed == llTo) {
            if (mViewDataBinding.llTo.isVisible()) {
                mViewDataBinding.llTo.visibility = View.GONE
                return
            } else {
                mViewDataBinding.llTo.visibility = View.VISIBLE
            }
            mViewDataBinding.llFrom.visibility = View.GONE
            mViewDataBinding.llHidden.visibility = View.GONE
        } else if (layoutPressed == llFrom) {
            if (mViewDataBinding.llFrom.isVisible()) {
                mViewDataBinding.llFrom.visibility = View.GONE
                return
            } else {
                mViewDataBinding.llFrom.visibility = View.VISIBLE
            }
            mViewDataBinding.llTo.visibility = View.GONE
            mViewDataBinding.llHidden.visibility = View.GONE
        } else if (layoutPressed == llHidden) {
            if (mViewDataBinding.llHidden.isVisible()) {
                mViewDataBinding.llHidden.visibility = View.GONE
                return
            } else {
                mViewDataBinding.llHidden.visibility = View.VISIBLE
            }

            mViewDataBinding.llTo.visibility = View.GONE
            mViewDataBinding.llFrom.visibility = View.GONE
        }

    }

    private fun manageLayoutsVisibilityWithKeyboardHidden() {
        if (layoutPressed == llTo) {

            if (mViewDataBinding.llTo.isVisible()) {
                mViewDataBinding.llTo.visibility = View.GONE
                return
            }
            mViewDataBinding.llTo.visibility = View.VISIBLE
            if (mViewDataBinding.llHidden.visibility == View.VISIBLE && mViewDataBinding.llFrom.visibility == View.VISIBLE)
                mViewDataBinding.llHidden.visibility = View.GONE
        } else if (layoutPressed == llFrom) {
            if (mViewDataBinding.llFrom.isVisible()) {
                mViewDataBinding.llFrom.visibility = View.GONE
                return
            }

            mViewDataBinding.llFrom.visibility = View.VISIBLE
            if (mViewDataBinding.llTo.visibility == View.VISIBLE && mViewDataBinding.llFrom.visibility == View.VISIBLE) {
                mViewDataBinding.llHidden.visibility = View.GONE
            }
        } else if (layoutPressed == llHidden) {

            if (mViewDataBinding.llHidden.isVisible()) {
                mViewDataBinding.llHidden.visibility = View.GONE
                return
            }
            mViewDataBinding.llHidden.visibility = View.VISIBLE
            if (mViewDataBinding.llTo.visibility == View.VISIBLE && mViewDataBinding.llHidden.visibility == View.VISIBLE) {
                mViewDataBinding.llFrom.visibility = View.GONE
            }
        } else {
            return
        }
    }

    private fun checkLayoutsVisibility() {
        if (mViewDataBinding.llTo.isVisible()) {
            mViewDataBinding.llFrom.visibility = View.GONE
            mViewDataBinding.llHidden.visibility = View.GONE
        } else if (mViewDataBinding.llFrom.isVisible() && mViewDataBinding.llHidden.isVisible())
            mViewDataBinding.llHidden.visibility = View.GONE
    }

    private fun updateCompoundDrawable() {
        val drawableResId =
            if (viewState.isFilterVisible.value!!) R.drawable.icon_search else R.drawable.icon_back
        val drawable = ContextCompat.getDrawable(mViewDataBinding.root.context, drawableResId)

        mViewDataBinding.filer.setCompoundDrawablesWithIntrinsicBounds(
            null,
            drawable,
            null,
            null
        )
    }
}
