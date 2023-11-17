package com.zstronics.ceibro.ui.tasks.v2.newtask.topic

import android.app.Activity
import android.content.Context.INPUT_METHOD_SERVICE
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.FragmentTopicBinding
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import javax.inject.Inject


@AndroidEntryPoint
class TopicFragment :
    BaseNavViewModelFragment<FragmentTopicBinding, ITopic.State, TopicVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TopicVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_topic
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                navigateBack()
            }

            R.id.saveTopicBtn -> {
                var searchedText = mViewDataBinding.topicSearchBar.query.toString().trim()
                if(searchedText.length > 99){
                    searchedText = searchedText.substring(0,100)
                }
                if (searchedText.isNotEmpty()) {
                    viewModel.saveTopic(searchedText) { isSuccess, newTopic ->
                        mViewDataBinding.topicSearchBar.setQuery("", false)
                        mViewDataBinding.topicSearchBar.clearFocus()

                        if (isSuccess) {
                            val bundle = Bundle()
                            bundle.putParcelable("topic", newTopic)
                            navigateBackWithResult(Activity.RESULT_OK, bundle)
                        }
                    }
                } else {
                    shortToastNow("Nothing to save")
                }
            }

            R.id.topicSearchBtn -> {
                activateSearchView()
            }
        }
    }


    @Inject
    lateinit var recentTopicAdapter: RecentTopicAdapter

    @Inject
    lateinit var allTopicsHeaderAdapter: AllTopicsHeaderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        mViewDataBinding.saveTopicBtn.visibility = View.GONE
        mViewDataBinding.recentTopicLayout.visibility = View.GONE
        mViewDataBinding.allTopicsRV.visibility = View.VISIBLE
        mViewDataBinding.topicInfoLayout.visibility = View.GONE
        mViewDataBinding.topicLogoBackground.visibility = View.GONE

        mViewDataBinding.recentTopicRV.isNestedScrollingEnabled = false
        mViewDataBinding.allTopicsRV.isNestedScrollingEnabled = false
        mViewDataBinding.recentTopicRV.adapter = recentTopicAdapter
        mViewDataBinding.allTopicsRV.adapter = allTopicsHeaderAdapter


        viewModel.allTopics.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                viewModel.groupDataByFirstLetter(it)
                mViewDataBinding.allTopicsRV.visibility = View.VISIBLE
            }
            if (it.isNullOrEmpty() && viewModel.recentTopics.value.isNullOrEmpty()) {
                mViewDataBinding.allTopicsRV.visibility = View.GONE
                mViewDataBinding.recentTopicLayout.visibility = View.GONE
                mViewDataBinding.topicInfoLayout.visibility = View.VISIBLE
                mViewDataBinding.topicLogoBackground.visibility = View.VISIBLE
            } else {
                mViewDataBinding.topicInfoLayout.visibility = View.GONE
                mViewDataBinding.topicLogoBackground.visibility = View.GONE
            }
        }
        viewModel.allTopicsGrouped.observe(viewLifecycleOwner) {
            if (it != null) {
                allTopicsHeaderAdapter.setList(it)
            }
        }
        viewModel.recentTopics.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                recentTopicAdapter.setList(it)
                mViewDataBinding.recentTopicLayout.visibility = View.VISIBLE
            }
            if (it.isNullOrEmpty() && viewModel.allTopics.value.isNullOrEmpty()) {
                mViewDataBinding.allTopicsRV.visibility = View.GONE
                mViewDataBinding.recentTopicLayout.visibility = View.GONE
                mViewDataBinding.topicInfoLayout.visibility = View.VISIBLE
                mViewDataBinding.topicLogoBackground.visibility = View.VISIBLE
            } else {
                mViewDataBinding.topicInfoLayout.visibility = View.GONE
                mViewDataBinding.topicLogoBackground.visibility = View.GONE
            }
        }

        recentTopicAdapter.recentTopicItemClickListener =
            { _: View, position: Int, data: TopicsResponse.TopicData ->
                val bundle = Bundle()
                bundle.putParcelable("topic", data)
                navigateBackWithResult(Activity.RESULT_OK, bundle)
            }
        allTopicsHeaderAdapter.allTopicItemClickListener =
            { _: View, position: Int, data: TopicsResponse.TopicData ->
                val bundle = Bundle()
                bundle.putParcelable("topic", data)
                navigateBackWithResult(Activity.RESULT_OK, bundle)
            }


        mViewDataBinding.topicSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.filterTopics(query.trim())
                    mViewDataBinding.saveTopicBtn.visibility =
                        if (query.trim().isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    viewModel.originalAllTopics.filter { it.topic.equals(query.trim(), true) }.map {
                        mViewDataBinding.saveTopicBtn.visibility = View.GONE
                    }
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    if(newText.trim().length > 100){
                        shortToastNow("Topic max length is 100 characters")
                        mViewDataBinding.topicSearchBar.setQuery(newText.trim().substring(0,100), false)
                        return true
                    }
                    viewModel.filterTopics(newText.trim())
                    mViewDataBinding.saveTopicBtn.visibility =
                        if (newText.trim().isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    viewModel.originalAllTopics.filter { it.topic.equals(newText.trim(), true) }
                        .map {
                            mViewDataBinding.saveTopicBtn.visibility = View.GONE
                        }
                }
                return true
            }
        })

        mViewDataBinding.topicSearchBar.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                // The SearchView has gained focus
                mViewDataBinding.topicSearchBtn.visibility = View.GONE
            } else {
                // The SearchView has lost focus
                mViewDataBinding.topicSearchBtn.visibility = View.VISIBLE
            }
        }

    }

    private fun loadTopics(skeletonVisible: Boolean) {
        mViewDataBinding.topicInfoLayout.visibility = View.GONE
        mViewDataBinding.topicLogoBackground.visibility = View.GONE
        if (skeletonVisible) {
            mViewDataBinding.allTopicsRV.loadSkeleton(R.layout.layout_invitations_box) {
                itemCount(10)
                color(R.color.appLightGrey)
            }

            viewModel.getAllTopics { allTopics ->
                mViewDataBinding.allTopicsRV.hideSkeleton()
                val searchQuery = mViewDataBinding.topicSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.filterTopics(searchQuery.trim())
                    mViewDataBinding.saveTopicBtn.visibility =
                        if (searchQuery.trim().isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }
//                if (allTopics?.allTopics.isNullOrEmpty() && allTopics?.recentTopics.isNullOrEmpty()) {
//                    mViewDataBinding.allTopicsRV.visibility = View.GONE
//                    mViewDataBinding.recentTopicLayout.visibility = View.GONE
//                    mViewDataBinding.topicInfoLayout.visibility = View.VISIBLE
//                } else {
////                    mViewDataBinding.allTopicsRV.visibility = View.GONE
//                    mViewDataBinding.topicInfoLayout.visibility = View.GONE
//                }
//                if (!allTopics?.recentTopics.isNullOrEmpty()) {
//                    mViewDataBinding.recentTopicLayout.visibility = View.VISIBLE
//                    mViewDataBinding.topicInfoLayout.visibility = View.GONE
//                } else {
//                    mViewDataBinding.recentTopicLayout.visibility = View.GONE
//                    mViewDataBinding.topicInfoLayout.visibility = View.VISIBLE
//                }
            }
        } else {
            viewModel.getAllTopics { allTopics ->
                val searchQuery = mViewDataBinding.topicSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.filterTopics(searchQuery.trim())
                    mViewDataBinding.saveTopicBtn.visibility =
                        if (searchQuery.trim().isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                }
//                if (!allTopics?.allTopics.isNullOrEmpty()) {
//                    mViewDataBinding.allTopicsRV.visibility = View.VISIBLE
//                    mViewDataBinding.topicInfoLayout.visibility = View.GONE
//                } else {
//                    mViewDataBinding.allTopicsRV.visibility = View.GONE
//                    mViewDataBinding.topicInfoLayout.visibility = View.VISIBLE
//                }
//                if (!allTopics?.recentTopics.isNullOrEmpty()) {
//                    mViewDataBinding.recentTopicLayout.visibility = View.VISIBLE
//                    mViewDataBinding.topicInfoLayout.visibility = View.GONE
//                } else {
//                    mViewDataBinding.recentTopicLayout.visibility = View.GONE
//                    mViewDataBinding.topicInfoLayout.visibility = View.VISIBLE
//                }
            }
        }


        val rootView = requireView()

        rootView.viewTreeObserver.addOnPreDrawListener {
            val screenHeight = rootView.rootView.height
            val heightDiff = screenHeight - rootView.height
            val thresholdPercentage = 0.16 // Adjust as needed

            if (heightDiff < screenHeight * thresholdPercentage) {
                // Keyboard is considered closed
                mViewDataBinding.topicSearchBtn.visibility = View.VISIBLE
                mViewDataBinding.topicSearchBtn.clearFocus()
            } else {
                mViewDataBinding.topicSearchBtn.visibility = View.GONE
                // Keyboard is considered open
            }
            true
        }
    }

    val callback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            println("TopicFragment: OnBackPressedCallback")
            navigateBack()
        }
    }

    override fun onResume() {
        super.onResume()
        mViewDataBinding.saveTopicBtn.visibility = View.GONE
        mViewDataBinding.recentTopicLayout.visibility = View.GONE
        loadTopics(true)
    }

    private fun activateSearchView() {
        // Set focus on the SearchView
        mViewDataBinding.topicSearchBtn.visibility = View.GONE
        mViewDataBinding.topicSearchBar.requestFocus()

        // Open the keyboard
        val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(mViewDataBinding.topicSearchBar, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun deactivateSearchView() {
        // close the keyboard
        val imm = requireContext().getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(mViewDataBinding.topicSearchBar.windowToken, 0)

        mViewDataBinding.topicSearchBar.clearFocus()
        mViewDataBinding.topicSearchBtn.visibility = View.VISIBLE
    }

}