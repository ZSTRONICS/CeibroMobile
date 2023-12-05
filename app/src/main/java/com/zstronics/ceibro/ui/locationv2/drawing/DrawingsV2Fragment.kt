package com.zstronics.ceibro.ui.locationv2.drawing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentDrawingsV2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DrawingsV2Fragment :
    BaseNavViewModelFragment<FragmentDrawingsV2Binding, IDrawingV2.State, DrawingsV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: DrawingsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_drawings_v2
    override fun toolBarVisibility(): Boolean = false

    override fun onClick(id: Int) {
        when (id) {
            R.id.projectFilterBtn -> {
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val groupData = listOf("Group 1", "Group 2")
        val childData = mapOf(
            "Group 1" to listOf("Child 1.1", "Child 1.2", "Child 1.3"),
            "Group 2" to listOf("Child 2.1", "Child 2.2", "Child 2.3")
        )
        val expandableListAdapter =
            ExpandableListAdapter(mViewDataBinding.root.context, groupData, childData)
        mViewDataBinding.expandableListView.setAdapter(expandableListAdapter)
    }

}