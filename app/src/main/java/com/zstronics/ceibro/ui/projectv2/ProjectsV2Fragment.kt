package com.zstronics.ceibro.ui.projectv2

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.tabs.TabLayoutMediator
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.extensions.showKeyboard
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentProjectsV2Binding
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProjectsV2Fragment :
    BaseNavViewModelFragment<FragmentProjectsV2Binding, IProjectsV2.State, ProjectsV2VM>() {
    val list = ArrayList<String>()
    var sharedViewModel: SharedViewModel? = null
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_projects_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.cl_AddNewProject -> {
                navigate(R.id.newProjectV2Fragment)
            }

            R.id.cl_new -> {
                navigate(R.id.newProjectV2Fragment)
            }

            R.id.cancelSearch -> {
                mViewDataBinding.projectSearchBar.hideKeyboard()
                mViewDataBinding.projectsSearchCard.visibility = View.GONE
                mViewDataBinding.projectSearchBtn.visibility = View.VISIBLE
                mViewDataBinding.projectSearchBar.setQuery("", false)
                sharedViewModel?.projectSearchQuery?.postValue("")
            }

            R.id.projectSearchBtn -> {
                showKeyboard()
                mViewDataBinding.projectSearchBar.requestFocus()
                mViewDataBinding.projectsSearchCard.visibility = View.VISIBLE
                mViewDataBinding.projectSearchBtn.visibility = View.GONE

            }
        }
    }

    override fun onPause() {
        super.onPause()
        mViewDataBinding.projectSearchBar.setQuery("", false)
        sharedViewModel?.projectSearchQuery?.postValue("")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        list.add(getString(R.string.all_projects))
        list.add(getString(R.string.hidden_projects))
        val adapter = ProjectTabLayoutAdapter(requireActivity())
        mViewDataBinding.viewPager.adapter = adapter


        TabLayoutMediator(mViewDataBinding.tabLayout, mViewDataBinding.viewPager) { tab, position ->
            tab.text = list[position]
        }.attach()


        mViewDataBinding.projectSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    sharedViewModel?.projectSearchQuery?.postValue(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    sharedViewModel?.projectSearchQuery?.postValue(newText)
                }
                return true
            }
        })
    }


}