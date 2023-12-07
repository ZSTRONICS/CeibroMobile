package com.zstronics.ceibro.ui.locationv2.drawing

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
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

    private lateinit var sectionedAdapter: AllDrawingsAdapterSectionRecycler
    private var sectionList: MutableList<DrawingSectionHeader> = mutableListOf()

    override fun onClick(id: Int) {
        when (id) {
            R.id.projectFilterBtn -> {
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val stringListData =
            StringListData(listOf("Group 1", "Group 2", "Group 3", "Group 4", "Group 5"))

        sectionList.add(
            0,
            DrawingSectionHeader(
                listOf(stringListData, stringListData, stringListData, stringListData),
                getString(R.string.favorite_projects)
            )
        )
        sectionList.add(
            1,
            DrawingSectionHeader(
                listOf(stringListData, stringListData, stringListData, stringListData),
                getString(R.string.recently_used)
            )
        )


        sectionedAdapter = AllDrawingsAdapterSectionRecycler(requireContext(), sectionList)
        val linearLayoutManager = LinearLayoutManager(requireContext())
        mViewDataBinding.drawingsRV.layoutManager = linearLayoutManager
        mViewDataBinding.drawingsRV.setHasFixedSize(true)
        mViewDataBinding.drawingsRV.adapter = sectionedAdapter


    }
}

data class StringListData(
    val stringList: List<String>
)


