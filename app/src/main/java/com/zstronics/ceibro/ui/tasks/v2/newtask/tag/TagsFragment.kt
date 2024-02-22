package com.zstronics.ceibro.ui.tasks.v2.newtask.tag

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.FragmentTagsBinding
import com.zstronics.ceibro.ui.tasks.v2.newtask.tag.adapter.TagsDrawingSectionHeader
import com.zstronics.ceibro.ui.tasks.v2.newtask.tag.adapter.TagsSectionRecyclerView
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton

@AndroidEntryPoint
class TagsFragment :
    BaseNavViewModelFragment<FragmentTagsBinding, ITags.State, TagsVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TagsVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_tags
    override fun toolBarVisibility(): Boolean = false

    var selectedTag = ArrayList<TopicsResponse.TopicData>()

    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                navigateBack()
            }

            R.id.saveTagBtn -> {
                var searchedText = mViewDataBinding.tagSearchBar.query.toString().trim()
                if (searchedText.length > 99) {
                    searchedText = searchedText.substring(0, 100)
                }
                if (searchedText.isNotEmpty()) {
                    viewModel.saveTopic(searchedText) { isSuccess, newTopic ->
                        mViewDataBinding.tagSearchBar.setQuery("", false)
                        mViewDataBinding.tagSearchBar.clearFocus()

                        /*if (isSuccess) {
                            val bundle = Bundle()
                            bundle.putParcelable("topic", newTopic)
                            navigateBackWithResult(Activity.RESULT_OK, bundle)
                        }*/
                    }
                } else {
                    shortToastNow("Nothing to save")
                }
            }

            R.id.tagBackBtn -> {
                navigateBack()
            }

            R.id.saveBtn -> {
                val bundle = Bundle()
                bundle.putParcelableArrayList("tag", selectedTag)
                navigateBackWithResult(Activity.RESULT_OK, bundle)
            }
        }
    }

    private var sectionList: MutableList<TagsDrawingSectionHeader> = mutableListOf()
    private lateinit var tagsSectionRecyclerView: TagsSectionRecyclerView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel.oldSelectedTags.observe(viewLifecycleOwner) {
            selectedTag.clear()
            selectedTag.addAll(it)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)
        mViewDataBinding.saveTagBtn.visibility = View.GONE

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
                    mViewDataBinding.saveTagBtn.visibility =
                        if (query.trim().isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    viewModel.originalAllTopics.filter { it.topic.equals(query.trim(), true) }.map {
                        mViewDataBinding.saveTagBtn.visibility = View.GONE
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
                    mViewDataBinding.saveTagBtn.visibility =
                        if (newText.trim().isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    viewModel.originalAllTopics.filter { it.topic.equals(newText.trim(), true) }
                        .map {
                            mViewDataBinding.saveTagBtn.visibility = View.GONE
                        }
                }
                return true
            }
        })


        /*mViewDataBinding.tagSearchBar.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // The SearchView has gained focus
                mViewDataBinding.tagSearchBar.visibility = View.GONE
            } else {
                // The SearchView has lost focus
                mViewDataBinding.tagSearchBar.visibility = View.VISIBLE
            }
        }*/

        /*val rootView = requireView()
        rootView.viewTreeObserver.addOnPreDrawListener {
            val screenHeight = rootView.rootView.height
            val heightDiff = screenHeight - rootView.height
            val thresholdPercentage = 0.16 // Adjust as needed

            if (heightDiff < screenHeight * thresholdPercentage) {
                // Keyboard is considered closed
                mViewDataBinding.tagBackBtn.visibility = View.VISIBLE
                mViewDataBinding.saveTagBtn.clearFocus()
            } else {
                mViewDataBinding.tagBackBtn.visibility = View.GONE
                // Keyboard is considered open
            }
            true
        }*/

    }

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            println("TopicFragment: OnBackPressedCallback")
            navigateBack()
        }
    }

    override fun onResume() {
        super.onResume()
        mViewDataBinding.saveTagBtn.visibility = View.GONE
        loadTopics(true)
    }

    private fun loadTopics(skeletonVisible: Boolean) {
        mViewDataBinding.topicLogoBackground.visibility = View.GONE
        if (skeletonVisible) {
            mViewDataBinding.alltagsRV.loadSkeleton(R.layout.layout_invitations_box) {
                itemCount(10)
                color(R.color.appLightGrey)
            }

            viewModel.getAllTopics { allTopics ->
                mViewDataBinding.alltagsRV.hideSkeleton()
                val searchQuery = mViewDataBinding.tagSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.filterTopics(searchQuery.trim())
                    mViewDataBinding.saveTagBtn.visibility =
                        if (searchQuery.trim().isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }
            }
        } else {
            viewModel.getAllTopics { allTopics ->
                val searchQuery = mViewDataBinding.tagSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.filterTopics(searchQuery.trim())
                    mViewDataBinding.saveTagBtn.visibility =
                        if (searchQuery.trim().isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }
            }
        }

    }


}