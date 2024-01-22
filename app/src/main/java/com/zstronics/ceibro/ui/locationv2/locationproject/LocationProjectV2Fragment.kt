package com.zstronics.ceibro.ui.locationv2.locationproject

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
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.databinding.FragmentLocationProjectsV2Binding
import com.zstronics.ceibro.ui.dashboard.SharedViewModel
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

@AndroidEntryPoint
class LocationProjectV2Fragment :
    BaseNavViewModelFragment<FragmentLocationProjectsV2Binding, ILocationProjectV2.State, LocationProjectV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: LocationProjectV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_location_projects_v2
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

    lateinit var sectionedAdapter: LocationProjectAdapterSectionRecycler
    private var sectionList: MutableList<LocationProjectsSectionHeader> = mutableListOf()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        mViewDataBinding.emptyProjectListHeader.visibility = View.GONE
        mViewDataBinding.projectsRV.visibility = View.VISIBLE

        sectionList.add(
            0,
            LocationProjectsSectionHeader(mutableListOf(), getString(R.string.favorite_projects))
        )
        sectionList.add(
            1,
            LocationProjectsSectionHeader(mutableListOf(), getString(R.string.all_projects))
        )

        sectionedAdapter = LocationProjectAdapterSectionRecycler(requireContext(), sectionList)


        sectionedAdapter.setCallBack { view, position, ceibroProjectV2, tag ->
            when (tag) {
                "detail" -> {
                    CeibroApplication.CookiesManager.locationProjectNameForDetails = ceibroProjectV2.title
                    CeibroApplication.CookiesManager.locationProjectDataForDetails = ceibroProjectV2
                    EventBus.getDefault().post(LocalEvents.LoadDrawingFragmentInLocation())
//                    navigate(R.id.projectDetailV2Fragment)
                }
            }
        }
        val linearLayoutManager = LinearLayoutManager(requireContext())
//        mViewDataBinding.projectsRV.removeAllViews()
        mViewDataBinding.projectsRV.layoutManager = linearLayoutManager
        mViewDataBinding.projectsRV.setHasFixedSize(true)
        mViewDataBinding.projectsRV.adapter = sectionedAdapter


        viewModel.allFavoriteProjects.observe(viewLifecycleOwner) {

            if (it.isNotEmpty()) {
                sectionList.removeAt(0)
//                sectionList[0] = LocationProjectsSectionHeader(it, getString(R.string.all_projects))
                sectionList.add(
                    0, LocationProjectsSectionHeader(it, getString(R.string.favorite_projects))
                )
                sectionedAdapter.insertNewSection(
                    LocationProjectsSectionHeader(
                        it,
                        getString(R.string.favorite_projects)
                    ), 0
                )
                sectionedAdapter.notifyDataSetChanged()

            } else {
                sectionList.removeAt(0)
                sectionList.add(
                    0,
                    LocationProjectsSectionHeader(mutableListOf(), getString(R.string.favorite_projects))
                )
                sectionedAdapter.insertNewSection(
                    LocationProjectsSectionHeader(
                        mutableListOf(),
                        getString(R.string.favorite_projects)
                    ), 0
                )
                sectionedAdapter.notifyDataSetChanged()
            }
        }


        viewModel.allProjects.observe(viewLifecycleOwner) {

            if (it.isNotEmpty()) {
                sectionList.removeAt(1)
//                sectionList[1] = LocationProjectsSectionHeader(it, getString(R.string.all_projects))
                sectionList.add(
                    1, LocationProjectsSectionHeader(it, getString(R.string.all_projects))
                )
                sectionedAdapter.insertNewSection(
                    LocationProjectsSectionHeader(
                        it,
                        getString(R.string.all_projects)
                    ), 1
                )

                sectionedAdapter.notifyDataSetChanged()
//                if (!searchingProject) {
                mViewDataBinding.emptyProjectListHeader.visibility = View.GONE
                mViewDataBinding.projectsRV.visibility = View.VISIBLE
//                }

            } else {
                sectionList.removeAt(1)
                sectionList.add(
                    1,
                    LocationProjectsSectionHeader(mutableListOf(), getString(R.string.all_projects))
                )
                sectionedAdapter.insertNewSection(
                    LocationProjectsSectionHeader(
                        mutableListOf(),
                        getString(R.string.all_projects)
                    ), 1
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

    override fun onResume() {
        super.onResume()
//        loadProjects()
    }

}