package com.zstronics.ceibro.ui.projectv2

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.viewModels
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.hideKeyboard
import com.zstronics.ceibro.base.extensions.showKeyboard
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentProjectsV2Binding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProjectsV2Fragment :
    BaseNavViewModelFragment<FragmentProjectsV2Binding, IProjectsV2.State, ProjectsV2VM>() {
    val list= ArrayList<String>()

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

            R.id.tvNewProject -> {
                navigate(R.id.newProjectV2Fragment)
            }

            R.id.cl_new -> {

               navigate(R.id.newProjectV2Fragment)
            }

            R.id.tvCancel -> {

                viewModel.viewState.searchProjectText.value = ""
                mViewDataBinding.projectSearchBar.hideKeyboard()
                mViewDataBinding.projectsSearchCard.visibility = View.GONE
                mViewDataBinding.connectionImgCard.visibility = View.VISIBLE
            }

            R.id.connectionImgCard -> {

                showKeyboard()
                mViewDataBinding.projectSearchBar.requestFocus()
                mViewDataBinding.projectsSearchCard.visibility = View.VISIBLE
                mViewDataBinding.connectionImgCard.visibility = View.GONE

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val fragmentManager: FragmentManager = childFragmentManager

        list.add("All Projects")
        list.add("Hidden Projects")
        val adapter = ProjectTabLayoutAdapter(fragmentManager,lifecycle) {
            navigate(R.id.projectDetailV2Fragment)
        }
        mViewDataBinding.viewPager.adapter = adapter


        TabLayoutMediator(mViewDataBinding.tabLayout, mViewDataBinding.viewPager) { tab, position ->
            tab.text = list[position]
        }.attach()


        mViewDataBinding.projectSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {

                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.viewState.searchProjectText.value = newText
                }
                return true
            }
        })

    }


}