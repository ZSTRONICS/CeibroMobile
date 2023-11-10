package com.zstronics.ceibro.ui.projectv2.hiddenprojectv2

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.room.Project
import com.zstronics.ceibro.databinding.FragmentAllProjectsV2Binding
import com.zstronics.ceibro.databinding.FragmentHiddenProjectsV2Binding
import com.zstronics.ceibro.ui.projectv2.allprojectsv2.AllProjectAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.section.ConnectionsSectionHeader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HiddenProjectsV2Fragment(callback: (Int) -> Unit) :
    BaseNavViewModelFragment<FragmentHiddenProjectsV2Binding, IHiddenProjectV2.State, HiddenProjectsV2VM>() {

    var callback: ((Int) -> Unit)?=null
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: HiddenProjectsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_hidden_projects_v2
    override fun toolBarVisibility(): Boolean = false

    init {

        this.callback=callback
    }
    @Inject
    lateinit var adapter: AllProjectAdapter
//
    // lateinit var adapter: AllProjectsAdapterSectionRecycler


    private var sectionList: MutableList<ConnectionsSectionHeader> = mutableListOf()

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



        sectionList.add(
            0,
            ConnectionsSectionHeader(mutableListOf(), getString(R.string.recent_connections))
        )
        sectionList.add(
            1,
            ConnectionsSectionHeader(mutableListOf(), getString(R.string.all_connections))
        )
        //adapter = AllProjectsAdapterSectionRecycler(requireContext(), sectionList)


        mViewDataBinding.projectsRV.adapter = adapter

        list.add(Project("123", "Try again and again"))
        list.add(Project("123", "Try again and again"))
        list.add(Project("123", "Try again and again"))
        list.add(Project("123", "Try again and again"))
        list.add(Project("123", "Try again and again"))
        list.add(Project("123", "Try again and again"))
        list.add(Project("123", "Try again and again"))
        list.add(Project("123", "Try again and again"))
        list.add(Project("123", "Try again and again"))
        list.add(Project("123", "Try again and again"))
        list.add(Project("123", "Try again and again"))

        initRecyclerView(adapter, list)
    }

    private fun initRecyclerView(adapter: AllProjectAdapter, list: MutableList<Project>) {
        mViewDataBinding.projectsRV.adapter = adapter
        adapter.setList(list,false)
        adapter.setCallBack {
            callback?.invoke(1)
        }

    }
//    fun setCallBack(callback: (Int) -> Unit){
//        this.callback=callback
//    }
}