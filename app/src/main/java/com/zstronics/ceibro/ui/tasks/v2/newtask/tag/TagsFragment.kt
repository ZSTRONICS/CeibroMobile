package com.zstronics.ceibro.ui.tasks.v2.newtask.tag

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentTagsBinding
import com.zstronics.ceibro.ui.tasks.v2.newtask.tag.adapter.TagsDrawingSectionHeader
import com.zstronics.ceibro.ui.tasks.v2.newtask.tag.adapter.TagsSectionRecyclerView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TagsFragment :
    BaseNavViewModelFragment<FragmentTagsBinding, ITags.State, TagsVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TagsVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_tags
    override fun toolBarVisibility(): Boolean = false

    private var sectionList: MutableList<TagsDrawingSectionHeader> = mutableListOf()

    lateinit var tagsSectionRecyclerView: TagsSectionRecyclerView

    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                navigateBack()
            }

            R.id.tagBackBtn -> {
                navigateBack()
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val list = mutableListOf("tag1", "tag2", "tag3", "tag4", "tag5")


        sectionList.add(
            0,
            TagsDrawingSectionHeader(
                list,
                "Recently Used Tags"
            )
        )
        sectionList.add(
            1,
            TagsDrawingSectionHeader(
                list,
                "All Tags"
            )
        )

        tagsSectionRecyclerView = TagsSectionRecyclerView(requireContext(), sectionList)

        viewModel.myGroupData.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                sectionList.removeAt(1)
                sectionList.add(1, TagsDrawingSectionHeader(list, "All Tags"))

                tagsSectionRecyclerView.insertNewSection(
                    TagsDrawingSectionHeader(
                        list,
                        getString(R.string.my_groups)
                    ), 1
                )
                tagsSectionRecyclerView.notifyDataSetChanged()
            }
        }
        mViewDataBinding.alltagsRV.adapter = tagsSectionRecyclerView


    }

}