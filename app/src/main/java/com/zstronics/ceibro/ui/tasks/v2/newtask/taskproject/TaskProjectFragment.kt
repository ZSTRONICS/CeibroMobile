package com.zstronics.ceibro.ui.tasks.v2.newtask.taskproject

import android.app.Activity
import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.databinding.FragmentTaskProjectBinding
import com.zstronics.ceibro.ui.tasks.v2.newtask.taskproject.adapter.AllProjectsHeaderAdapter
import dagger.hilt.android.AndroidEntryPoint
import koleton.api.hideSkeleton
import koleton.api.loadSkeleton
import javax.inject.Inject

@AndroidEntryPoint
class TaskProjectFragment :
    BaseNavViewModelFragment<FragmentTaskProjectBinding, ITaskProject.State, TaskProjectVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: TaskProjectVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_task_project
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> {
                navigateBack()
            }
            R.id.cancelSearchBtn -> {
                mViewDataBinding.projectSearchBar.setQuery("", false)
                mViewDataBinding.projectSearchBar.clearFocus()
            }
        }
    }


    @Inject
    lateinit var allProjectsHeaderAdapter: AllProjectsHeaderAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.recentProjectLayout.visibility = View.GONE

        mViewDataBinding.recentProjectsRV.isNestedScrollingEnabled = false
        mViewDataBinding.allProjectsRV.isNestedScrollingEnabled = false
        mViewDataBinding.allProjectsRV.adapter = allProjectsHeaderAdapter


        viewModel.allProjects.observe(viewLifecycleOwner) {
            if (it != null) {
                viewModel.groupDataByFirstLetter(it)
            }
        }
        viewModel.allProjectsGrouped.observe(viewLifecycleOwner) {
            if (it != null) {
                allProjectsHeaderAdapter.setList(it)
            }
        }

        allProjectsHeaderAdapter.allProjectItemClickListener = { _: View, position: Int, data: CeibroProjectV2 ->
            val bundle = Bundle()
            bundle.putParcelable("project", data)
            navigateBackWithResult(Activity.RESULT_OK, bundle)
        }


        mViewDataBinding.projectSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.searchProject(query)
                }
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.searchProject(newText)
                }
                return true
            }
        })
    }

    private fun loadProject(skeletonVisible: Boolean) {
        if (skeletonVisible) {
            mViewDataBinding.allProjectsRV.loadSkeleton(R.layout.layout_invitations_box) {
                itemCount(10)
                color(R.color.appLightGrey)
            }

            viewModel.loadProjects {
                mViewDataBinding.allProjectsRV.hideSkeleton()
                val searchQuery = mViewDataBinding.projectSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.searchProject(searchQuery)
                }
            }
        } else {
            viewModel.loadProjects {
                val searchQuery = mViewDataBinding.projectSearchBar.query.toString()
                if (searchQuery.isNotEmpty()) {
                    viewModel.searchProject(searchQuery)
                }
            }
        }
    }


    override fun onResume() {
        super.onResume()
        mViewDataBinding.recentProjectLayout.visibility = View.GONE
        loadProject(true)
    }
}