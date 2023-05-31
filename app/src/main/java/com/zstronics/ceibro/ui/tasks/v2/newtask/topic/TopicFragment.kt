package com.zstronics.ceibro.ui.tasks.v2.newtask.topic

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.FragmentTopicBinding
import com.zstronics.ceibro.ui.dashboard.myconnectionsv2.CeibroConnectionsHeaderAdapter
import com.zstronics.ceibro.ui.dashboard.myconnectionsv2.MyConnectionV2Fragment
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
            R.id.cancelBtn -> {
                mViewDataBinding.topicSearchBar.setQuery("", false)
                mViewDataBinding.topicSearchBar.clearFocus()
            }
            R.id.saveTopicLayout -> {
                val searchedText = mViewDataBinding.topicSearchBar.query.toString()
                //API call in progress
            }
        }
    }


    @Inject
    lateinit var recentTopicAdapter: RecentTopicAdapter
    @Inject
    lateinit var allTopicsHeaderAdapter: AllTopicsHeaderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.saveTopicLayout.visibility = View.GONE
        mViewDataBinding.recentTopicLayout.visibility = View.GONE

        mViewDataBinding.recentTopicRV.isNestedScrollingEnabled = false
        mViewDataBinding.allTopicsRV.isNestedScrollingEnabled = false
        mViewDataBinding.recentTopicRV.adapter = recentTopicAdapter
        mViewDataBinding.allTopicsRV.adapter = allTopicsHeaderAdapter

        viewModel.allTopics.observe(viewLifecycleOwner) {
            if (it != null) {
                println("All Topics: $it")
                viewModel.groupDataByFirstLetter(it)
            }
        }
        viewModel.allTopicsGrouped.observe(viewLifecycleOwner) {
            if (it != null) {
                allTopicsHeaderAdapter.setList(it)
            }
        }

        viewModel.recentTopics.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) {
                mViewDataBinding.recentTopicLayout.visibility = View.VISIBLE
                recentTopicAdapter.setList(it)
            } else {
                mViewDataBinding.recentTopicLayout.visibility = View.GONE
            }
        }


        mViewDataBinding.topicSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.filterTopics(query)
                    mViewDataBinding.saveTopicLayout.visibility =
                        if (query.isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    mViewDataBinding.saveTopicNameText.text = "\"$query\""
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.filterTopics(newText)
                    mViewDataBinding.saveTopicLayout.visibility =
                        if (newText.isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    mViewDataBinding.saveTopicNameText.text = "\"$newText\""
                }
                return true
            }

        })
    }

    private fun loadTopics(skeletonVisible: Boolean) {
        if (skeletonVisible) {
            mViewDataBinding.allTopicsRV.loadSkeleton(R.layout.layout_invitations_box) {
                itemCount(10)
                color(R.color.appLightGrey)
            }

            viewModel.getAllTopics {
                mViewDataBinding.allTopicsRV.hideSkeleton()
                val searchQuery = mViewDataBinding.topicSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.filterTopics(searchQuery)
                    mViewDataBinding.saveTopicLayout.visibility =
                        if (searchQuery.isNotEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    mViewDataBinding.saveTopicNameText.text = "\"$searchQuery\""
                }
            }
        } else {
            viewModel.getAllTopics { }
        }
    }


    override fun onResume() {
        super.onResume()
        mViewDataBinding.saveTopicLayout.visibility = View.GONE
        mViewDataBinding.recentTopicLayout.visibility = View.GONE
        loadTopics(true)
    }
}