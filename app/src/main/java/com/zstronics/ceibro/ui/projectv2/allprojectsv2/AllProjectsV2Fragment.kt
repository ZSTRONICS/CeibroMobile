package com.zstronics.ceibro.ui.projectv2.allprojectsv2

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.room.Project
import com.zstronics.ceibro.databinding.FragmentAllProjectsV2Binding
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.section.ConnectionsSectionHeader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class AllProjectsV2Fragment(callback: (Int) -> Unit) :
    BaseNavViewModelFragment<FragmentAllProjectsV2Binding, IAllProjectV2.State, AllProjectsV2VM>() {

    var callback: ((Int) -> Unit)? = null
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: AllProjectsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_all_projects_v2
    override fun toolBarVisibility(): Boolean = false

    init {

        this.callback = callback
    }

    @Inject
    lateinit var adapter: AllProjectAdapter


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


        if (list.isNotEmpty()) {
            mViewDataBinding.toolbarHeader.visibility = View.GONE
            mViewDataBinding.projectsRV.visibility = View.VISIBLE
            initRecyclerView(adapter, list)
        } else {
            mViewDataBinding.toolbarHeader.visibility = View.VISIBLE
            mViewDataBinding.projectsRV.visibility = View.GONE
        }

    }

    private fun initRecyclerView(adapter: AllProjectAdapter, list: MutableList<Project>) {
        mViewDataBinding.projectsRV.adapter = adapter
        adapter.setList(list, true)
        adapter.setCallBack {

            when (it) {
                1 -> {
                    callback?.invoke(1)
                }

                2 -> {
                    showDialog()
                }
            }

        }

    }

    private fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.layout_task_hide_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val tvYes: TextView = dialog.findViewById(R.id.tvYes)
        val tvNo: TextView = dialog.findViewById(R.id.tvNo)

        tvYes.setOnClickListener {
            dialog.dismiss()
        }
        tvNo.setOnClickListener {
            dialog.dismiss()
        }
        dialog.show()
    }
}