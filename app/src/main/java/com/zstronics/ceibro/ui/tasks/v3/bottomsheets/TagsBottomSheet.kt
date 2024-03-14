package com.zstronics.ceibro.ui.tasks.v3.bottomsheets


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.FragmentTagsSheetBinding
import com.zstronics.ceibro.ui.tasks.v2.newtask.tag.adapter.TagsDrawingSectionHeader
import com.zstronics.ceibro.ui.tasks.v2.newtask.tag.adapter.TagsSectionRecyclerView
import com.zstronics.ceibro.ui.tasks.v3.TasksParentTabV3VM

class TagsBottomSheet(val viewModel: TasksParentTabV3VM, val callback: (ArrayList<TopicsResponse.TopicData>) -> Unit) :
    BottomSheetDialogFragment() {
    lateinit var mViewDataBinding: FragmentTagsSheetBinding
    private var searchingProject = false

    var selectedTags = ArrayList<TopicsResponse.TopicData>()
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



        mViewDataBinding.backBtn.setOnClick {
            dismiss()
        }
        mViewDataBinding.tvClearAll.setOnClickListener {
            mViewDataBinding.tagSearchBar.setQuery(null, true)
            viewModel.filterTopics("")
            selectedTags.clear()
            viewModel.selectedTagsForFilter=selectedTags
            tagsSectionRecyclerView.setData(viewModel.oldSelectedTags.value)
            callback.invoke(selectedTags)
            dismiss()

        }
        mViewDataBinding.btnApply.setOnClickListener {

            viewModel.selectedTagsForFilter=selectedTags
            callback.invoke(selectedTags)
            dismiss()
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

        selectedTags.clear()
        viewModel.selectedTagsForFilter.forEach {
            selectedTags.add(it.copy()) // Creates a new instance using the copy method
        }



        tagsSectionRecyclerView.setData(viewModel.selectedTagsForFilter)
        tagsSectionRecyclerView.itemClickListener =
            { flag: Boolean, view: View, position: Int, data: TopicsResponse.TopicData ->

                if (flag) {
                    selectedTags.add(data)
                } else {
                    selectedTags.remove(data)
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
            dialog.behavior.skipCollapsed = false
            dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        return dialog
    }


    private fun loadTopics() {
        viewModel.getAllTopics {
        }
    }

}