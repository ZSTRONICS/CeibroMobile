package com.zstronics.ceibro.ui.tasks.v3.bottomsheets


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.FragmentTagsSheetBinding
import com.zstronics.ceibro.ui.tasks.v2.newtask.tag.adapter.TagsDrawingSectionHeader
import com.zstronics.ceibro.ui.tasks.v2.newtask.tag.adapter.TagsSectionRecyclerView
import com.zstronics.ceibro.ui.tasks.v3.TasksParentTabV3VM

class TagsBottomSheet(val viewModel: TasksParentTabV3VM, val callback: (String) -> Unit) :
    BottomSheetDialogFragment() {
    lateinit var mViewDataBinding: FragmentTagsSheetBinding
    private var searchingProject = false

    var selectedTag = ArrayList<TopicsResponse.TopicData>()
    private var sectionList: MutableList<TagsDrawingSectionHeader> = mutableListOf()
    private lateinit var tagsSectionRecyclerView: TagsSectionRecyclerView
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mViewDataBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_tags_sheet,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return mViewDataBinding.root
    }

    init {
        loadTopics()
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        mViewDataBinding.tvClearAll.setOnClickListener {
            mViewDataBinding.tagSearchBar.setQuery(null, true)
            viewModel.filterFavoriteProjects("")
            viewModel.filterAllProjects("")
        }
        mViewDataBinding.btnApply.setOnClickListener {

            callback.invoke(selectedTag.size.toString())
            dismiss()
        }


        viewModel.oldSelectedTags.observe(viewLifecycleOwner) {
            selectedTag.clear()
            selectedTag.addAll(it)
        }

        sectionList.add(
            0,
            TagsDrawingSectionHeader(
                emptyList(),
                "Recently Used Tags"
            )
        )
        sectionList.add(
            1,
            TagsDrawingSectionHeader(
                emptyList(),
                "All Tags"
            )
        )

        tagsSectionRecyclerView = TagsSectionRecyclerView(requireContext(), sectionList)
        tagsSectionRecyclerView.itemClickListener =
            { flag: Boolean, view: View, position: Int, data: TopicsResponse.TopicData ->

                if (flag) {
                    selectedTag.add(data)
                } else {
                    selectedTag.remove(data)
                }
            }
        mViewDataBinding.alltagsRV.adapter = tagsSectionRecyclerView

        viewModel.recentTopics.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                sectionList.removeAt(0)
                sectionList.add(0, TagsDrawingSectionHeader(it, "Recently Used Tags"))

                tagsSectionRecyclerView.insertNewSection(
                    TagsDrawingSectionHeader(
                        it,
                        getString(R.string.recently_used_tagged)
                    ), 0
                )
                tagsSectionRecyclerView.setData(viewModel.oldSelectedTags.value ?: mutableListOf())
                tagsSectionRecyclerView.notifyDataSetChanged()
            } else {
                sectionList.removeAt(0)
                sectionList.add(0, TagsDrawingSectionHeader(emptyList(), "Recently Used Tags"))

                tagsSectionRecyclerView.insertNewSection(
                    TagsDrawingSectionHeader(
                        emptyList(),
                        getString(R.string.recently_used_tagged)
                    ), 0
                )
                tagsSectionRecyclerView.setData(viewModel.oldSelectedTags.value ?: mutableListOf())
                tagsSectionRecyclerView.notifyDataSetChanged()
            }

        }

        viewModel.allTopics.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                sectionList.removeAt(1)
                sectionList.add(1, TagsDrawingSectionHeader(it, "All Tags"))

                tagsSectionRecyclerView.insertNewSection(
                    TagsDrawingSectionHeader(
                        it,
                        getString(R.string.all_tags)
                    ), 1
                )
                tagsSectionRecyclerView.notifyDataSetChanged()
            } else {
                sectionList.removeAt(1)
                sectionList.add(1, TagsDrawingSectionHeader(emptyList(), "All Tags"))

                tagsSectionRecyclerView.insertNewSection(
                    TagsDrawingSectionHeader(
                        emptyList(),
                        getString(R.string.all_tags)
                    ), 1
                )
                tagsSectionRecyclerView.notifyDataSetChanged()
            }

        }

        mViewDataBinding.tagSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.filterTopics(query.trim())

                    viewModel.originalAllTopics.filter { it.topic.equals(query.trim(), true) }.map {

                    }
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    if (newText.trim().length > 100) {
                        shortToastNow("Tag max length is 100 characters")
                        mViewDataBinding.tagSearchBar.setQuery(
                            newText.trim().substring(0, 100),
                            false
                        )
                        return true
                    }
                    viewModel.filterTopics(newText.trim())

                }
                return true
            }
        })


    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = true
        }
        return dialog
    }


    private fun loadTopics() {
        viewModel.getAllTopics {
        }
    }

}