package com.zstronics.ceibro.ui.projectv2.hiddenprojectv2

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.chat.room.Project
import com.zstronics.ceibro.databinding.FragmentHiddenProjectsV2Binding
import com.zstronics.ceibro.ui.projectv2.allprojectsv2.AllProjectAdapter
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.section.ConnectionsSectionHeader
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class HiddenProjectsV2Fragment(callback: (Int) -> Unit) :
    BaseNavViewModelFragment<FragmentHiddenProjectsV2Binding, IHiddenProjectV2.State, HiddenProjectsV2VM>() {

    var callback: ((Int) -> Unit)? = null
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: HiddenProjectsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_hidden_projects_v2
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
        adapter.setList(list, false)
        adapter.setCallBack {

            when (it) {
                1 -> {
                    callback?.invoke(1)
                }

                2 -> {
                    showHideTaskDialog(requireContext())
                }
            }

        }

    }

    private fun showHideTaskDialog(context: Context) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_custom_dialog, null)

        val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(view)
        val alertDialog = builder.create()

        val yesBtn = view.findViewById<Button>(R.id.yesBtn)
        val noBtn = view.findViewById<Button>(R.id.noBtn)
        val dialogText = view.findViewById<TextView>(R.id.dialog_text)
        dialogText.text = context.resources.getString(R.string.do_you_want_to_un_hide_this_project)
        alertDialog.window?.setBackgroundDrawable(null)
        alertDialog.show()

        yesBtn.setOnClickListener {
            alertDialog.dismiss()
        }

        noBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }
}