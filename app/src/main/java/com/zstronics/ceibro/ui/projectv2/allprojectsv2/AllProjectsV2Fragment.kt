package com.zstronics.ceibro.ui.projectv2.allprojectsv2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.room.Project
import com.zstronics.ceibro.databinding.FragmentAllProjectsV2Binding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AllProjectsV2Fragment :
    BaseNavViewModelFragment<FragmentAllProjectsV2Binding, IAllProjectV2.State, AllProjectsV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: AllProjectsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_all_projects_v2
    override fun toolBarVisibility(): Boolean = false
    @Inject
    lateinit var adapter: AllProjectAdapter

    private var list: MutableList<Project> = mutableListOf()
    override fun onClick(id: Int) {

        when (id) {

            R.id.cl_AddNewProject -> {
                navigate(R.id.newProjectV2Fragment)
            }

            R.id.tvNewProject -> {
                navigate(R.id.newProjectV2Fragment)
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        list.add(Project("123","Try again and again"))
        list.add(Project("123","Try again and again"))
        list.add(Project("123","Try again and again"))
        list.add(Project("123","Try again and again"))
        list.add(Project("123","Try again and again"))
        list.add(Project("123","Try again and again"))
        list.add(Project("123","Try again and again"))
        list.add(Project("123","Try again and again"))
        list.add(Project("123","Try again and again"))
        list.add(Project("123","Try again and again"))
        list.add(Project("123","Try again and again"))

        initRecyclerView(adapter,list)
    }
    private fun initRecyclerView(adapter: AllProjectAdapter, list: MutableList<Project>) {
        mViewDataBinding.projectsRV.adapter = adapter
        adapter.setList(list)

    }
}