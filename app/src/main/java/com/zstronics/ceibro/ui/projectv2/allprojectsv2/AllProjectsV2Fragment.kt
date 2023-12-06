package com.zstronics.ceibro.ui.projectv2.allprojectsv2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.databinding.FragmentAllProjectsV2Binding
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class AllProjectsV2Fragment :
    BaseNavViewModelFragment<FragmentAllProjectsV2Binding, IAllProjectV2.State, AllProjectsV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: AllProjectsV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_all_projects_v2
    override fun toolBarVisibility(): Boolean = false
    var sharedViewModel: SharedViewModel? = null
    var searchingProject = false
    override fun onClick(id: Int) {
        when (id) {

            R.id.cl_AddNewProject -> {
                navigate(R.id.newProjectV2Fragment)
            }

        }
    }

    lateinit var sectionedAdapter: AllProjectsAdapterSectionRecycler
    private var sectionList: MutableList<ProjectsSectionHeader> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        mViewDataBinding.emptyProjectListHeader.visibility = View.GONE
        mViewDataBinding.projectsRV.visibility = View.VISIBLE

        sectionList.add(
            0,
            ProjectsSectionHeader(mutableListOf(), getString(R.string.favorite_projects))
        )
        sectionList.add(
            1,
            ProjectsSectionHeader(mutableListOf(), getString(R.string.recent_projects))
        )
        sectionList.add(
            2,
            ProjectsSectionHeader(mutableListOf(), getString(R.string.all_projects))
        )

        sectionedAdapter = AllProjectsAdapterSectionRecycler(requireContext(), sectionList)

        sectionedAdapter.setCallBack { view, position, ceibroProjectV2, tag ->
            when (tag) {
                "detail" -> {
                    CookiesManager.projectNameForDetails = ceibroProjectV2.title
                    CookiesManager.projectDataForDetails = ceibroProjectV2
                    navigate(R.id.projectDetailV2Fragment)
                }

                "hide" -> {
                    showHideTaskDialog(requireContext(), ceibroProjectV2)
                }

                "favorite" -> {
                    viewModel.updateFavoriteProjectStatus(
                        !(ceibroProjectV2.isFavoriteByMe),
                        ceibroProjectV2._id
                    ) {

                    }
                }
            }
        }
        val linearLayoutManager = LinearLayoutManager(requireContext())
        mViewDataBinding.projectsRV.layoutManager = linearLayoutManager
        mViewDataBinding.projectsRV.setHasFixedSize(true)
        mViewDataBinding.projectsRV.adapter = sectionedAdapter


        viewModel.allFavoriteProjects.observe(viewLifecycleOwner) {

            if (it.isNotEmpty()) {
                sectionList.removeAt(0)
//                sectionList[0] = ProjectsSectionHeader(it, getString(R.string.all_projects))
                sectionList.add(
                    0, ProjectsSectionHeader(it, getString(R.string.favorite_projects))
                )
                sectionedAdapter.insertNewSection(
                    ProjectsSectionHeader(
                        it,
                        getString(R.string.favorite_projects)
                    ), 0
                )
                sectionedAdapter.notifyDataSetChanged()

            } else {
                sectionList.removeAt(0)
                sectionList.add(
                    0,
                    ProjectsSectionHeader(mutableListOf(), getString(R.string.favorite_projects))
                )
                sectionedAdapter.insertNewSection(
                    ProjectsSectionHeader(
                        mutableListOf(),
                        getString(R.string.favorite_projects)
                    ), 0
                )
                sectionedAdapter.notifyDataSetChanged()
            }
        }

        viewModel.allRecentProjects.observe(viewLifecycleOwner) {

            if (it.isNotEmpty()) {
                sectionList.removeAt(1)
//                sectionList[1] = ProjectsSectionHeader(it, getString(R.string.all_projects))
                sectionList.add(
                    1, ProjectsSectionHeader(it, getString(R.string.recent_projects))
                )
                sectionedAdapter.insertNewSection(
                    ProjectsSectionHeader(
                        it,
                        getString(R.string.recent_projects)
                    ), 1
                )

                sectionedAdapter.notifyDataSetChanged()

            } else {
                sectionList.removeAt(1)
                sectionList.add(
                    1,
                    ProjectsSectionHeader(mutableListOf(), getString(R.string.recent_projects))
                )
                sectionedAdapter.insertNewSection(
                    ProjectsSectionHeader(
                        mutableListOf(),
                        getString(R.string.recent_projects)
                    ), 1
                )
                sectionedAdapter.notifyDataSetChanged()
            }
        }


        viewModel.allProjects.observe(viewLifecycleOwner) {

            if (it.isNotEmpty()) {
                sectionList.removeAt(2)
//                sectionList[1] = ProjectsSectionHeader(it, getString(R.string.all_projects))
                sectionList.add(
                    2, ProjectsSectionHeader(it, getString(R.string.all_projects))
                )
                sectionedAdapter.insertNewSection(
                    ProjectsSectionHeader(
                        it,
                        getString(R.string.all_projects)
                    ), 2
                )

                sectionedAdapter.notifyDataSetChanged()
//                if (!searchingProject) {
                mViewDataBinding.emptyProjectListHeader.visibility = View.GONE
                mViewDataBinding.projectsRV.visibility = View.VISIBLE
//                }

            } else {
                sectionList.removeAt(2)
                sectionList.add(
                    2,
                    ProjectsSectionHeader(mutableListOf(), getString(R.string.all_projects))
                )
                sectionedAdapter.insertNewSection(
                    ProjectsSectionHeader(
                        mutableListOf(),
                        getString(R.string.all_projects)
                    ), 2
                )
                sectionedAdapter.notifyDataSetChanged()
                if (!searchingProject) {
                    mViewDataBinding.emptyProjectListHeader.visibility = View.VISIBLE
                    mViewDataBinding.projectsRV.visibility = View.GONE
                }
            }
        }

        sharedViewModel?.projectSearchQuery?.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                searchingProject = true
            }
            viewModel.filterFavoriteProjects(it)
            viewModel.filterRecentProjects(it)
            viewModel.filterAllProjects(it)
        }
    }


    private fun showHideTaskDialog(
        context: Context,
        second: CeibroProjectV2
    ) {
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_custom_dialog, null)

        val builder: AlertDialog.Builder = AlertDialog.Builder(context).setView(view)
        val alertDialog = builder.create()

        val yesBtn = view.findViewById<Button>(R.id.yesBtn)
        val noBtn = view.findViewById<Button>(R.id.noBtn)
        val dialogText = view.findViewById<TextView>(R.id.dialog_text)
        dialogText.text = context.resources.getString(R.string.do_you_want_to_hide_this_project)
        alertDialog.window?.setBackgroundDrawable(null)
        alertDialog.show()

        yesBtn.setOnClickListener {
            alertDialog.dismiss()
            viewModel.hideProject(!(second.isHiddenByMe), second._id) {

            }
        }

        noBtn.setOnClickListener {
            alertDialog.dismiss()
        }
    }


    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)
    }

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onRefreshProjectsData(event: LocalEvents.RefreshProjectsData?) {
        loadProjects()
    }

    fun loadProjects() {
        viewModel.reloadData()
    }

}