package com.zstronics.ceibro.ui.locationv2

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.extensions.isVisible
import com.zstronics.ceibro.base.extensions.showKeyboard
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentLocationsV2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocationsV2Fragment :
    BaseNavViewModelFragment<FragmentLocationsV2Binding, ILocations.State, LocationsV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: LocationsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_locations_v2
    private var layoutPressed = ""
    private var isKeyboardShown = false
    private val spinnerItems = arrayOf("Floor", "Kitchen", "Garden")

    override fun toolBarVisibility(): Boolean = false
    companion object{

        private const val llTo = "llTo"
        private const val llFrom = "llFrom"
        private const val llHidden = "llHidden"
    }

    override fun onClick(id: Int) {
        when (id) {
            R.id.projectFilterBtn -> {

                viewState.isFilterVisible.value?.let { currentValue ->
                    val updateFlag = !currentValue
                    viewState.isFilterVisible.value = updateFlag
                    updateCompoundDrawable(
                        mViewDataBinding.filter,
                        mViewDataBinding.ivFilter,
                        updateFlag
                    )
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
                showKeyboard()
                isKeyboardShown = true
                Handler(Looper.getMainLooper()).postDelayed({
                    checkLayoutsVisibility()
                }, 100)
                mViewDataBinding.projectSearchBar.requestFocus()
                mViewDataBinding.projectsSearchCard.visibility = View.VISIBLE
                mViewDataBinding.projectSearchBtn.visibility = View.INVISIBLE
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
                  //  val selectedItem = spinnerItems[position]
                }

                override fun onNothingSelected(parent: AdapterView<*>?) {

                }
            }
        val rootView = requireView()
        rootView.viewTreeObserver.addOnPreDrawListener {
            val screenHeight = rootView.rootView.height
            val heightDiff = screenHeight - rootView.height
            val thresholdPercentage = 0.30 // Adjust as needed

            if (heightDiff >= screenHeight * thresholdPercentage) {
                isKeyboardShown = true
                checkLayoutsVisibility()
            } else {
                isKeyboardShown = false
            }

            true
        }
    }

    private fun manageLayoutsVisibilityWithKeyboardShown() {

        if (layoutPressed == llTo) {
            if (mViewDataBinding.llTo.isVisible()) {
                mViewDataBinding.llTo.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvTo)
                return
            } else {
                mViewDataBinding.llTo.visibility = View.VISIBLE
                updateLayoutCompoundDrawable(true, mViewDataBinding.tvTo)
            }
            mViewDataBinding.llFrom.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvfrom)
            mViewDataBinding.llHidden.visibility = View.GONE
        } else if (layoutPressed == llFrom) {
            if (mViewDataBinding.llFrom.isVisible()) {
                mViewDataBinding.llFrom.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvfrom)
                return
            } else {
                mViewDataBinding.llFrom.visibility = View.VISIBLE
                updateLayoutCompoundDrawable(true, mViewDataBinding.tvfrom)
            }
            mViewDataBinding.llTo.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvTo)
            mViewDataBinding.llHidden.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
        } else if (layoutPressed == llHidden) {
            if (mViewDataBinding.llHidden.isVisible()) {
                mViewDataBinding.llHidden.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
                return
            } else {
                mViewDataBinding.llHidden.visibility = View.VISIBLE
                updateLayoutCompoundDrawable(true, mViewDataBinding.tvHidden)
            }

            mViewDataBinding.llTo.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvTo)
            mViewDataBinding.llFrom.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvfrom)
        }

    }

    private fun manageLayoutsVisibilityWithKeyboardHidden() {
        if (layoutPressed == llTo) {
            if (mViewDataBinding.llTo.isVisible()) {
                mViewDataBinding.llTo.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvTo)
                return
            }
            mViewDataBinding.llTo.visibility = View.VISIBLE
            updateLayoutCompoundDrawable(true, mViewDataBinding.tvTo)
            if (mViewDataBinding.llHidden.visibility == View.VISIBLE && mViewDataBinding.llFrom.visibility == View.VISIBLE)
                mViewDataBinding.llHidden.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
        } else if (layoutPressed == llFrom) {
            if (mViewDataBinding.llFrom.isVisible()) {
                mViewDataBinding.llFrom.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvfrom)
                return
            }

            mViewDataBinding.llFrom.visibility = View.VISIBLE
            updateLayoutCompoundDrawable(true, mViewDataBinding.tvfrom)
            if (mViewDataBinding.llTo.visibility == View.VISIBLE && mViewDataBinding.llFrom.visibility == View.VISIBLE) {
                mViewDataBinding.llHidden.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
            }
        } else if (layoutPressed == llHidden) {

            if (mViewDataBinding.llHidden.isVisible()) {
                mViewDataBinding.llHidden.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
                return
            }
            mViewDataBinding.llHidden.visibility = View.VISIBLE
            updateLayoutCompoundDrawable(true, mViewDataBinding.tvHidden)
            if (mViewDataBinding.llTo.visibility == View.VISIBLE && mViewDataBinding.llHidden.visibility == View.VISIBLE) {
                mViewDataBinding.llFrom.visibility = View.GONE
                updateLayoutCompoundDrawable(false, mViewDataBinding.tvfrom)
            }
        } else {
            return
        }
    }

    private fun checkLayoutsVisibility() {
        if (mViewDataBinding.llTo.isVisible()) {
            mViewDataBinding.llFrom.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvfrom)
            mViewDataBinding.llHidden.visibility = View.GONE
            updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
        } else if (mViewDataBinding.llFrom.isVisible() && mViewDataBinding.llHidden.isVisible())
            mViewDataBinding.llHidden.visibility = View.GONE
        updateLayoutCompoundDrawable(false, mViewDataBinding.tvHidden)
    }

    private fun updateCompoundDrawable(filter: TextView, ivFilter: ImageView, flag: Boolean) {
        val drawableResId = if (flag) R.drawable.icon_filter_blue else R.drawable.ic_cross_blue
        if (flag) filter.visibility = View.VISIBLE else filter.visibility = View.GONE
        ivFilter.setImageResource(drawableResId)
    }

    private fun updateLayoutCompoundDrawable(tag: Boolean, filter: TextView) {
        val drawableResId = if (!tag) R.drawable.icon_drop_down else R.drawable.arrow_drop_up

        val drawable = ContextCompat.getDrawable(mViewDataBinding.root.context, drawableResId)

        filter.setCompoundDrawablesWithIntrinsicBounds(
            null,
            null,
            drawable,
            null
        )
    }
}
