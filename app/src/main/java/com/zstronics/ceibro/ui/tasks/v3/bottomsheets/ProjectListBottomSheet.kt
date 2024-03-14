package com.zstronics.ceibro.ui.tasks.v3.bottomsheets


import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SearchView
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.databinding.FragmentProjectListBinding
import com.zstronics.ceibro.ui.locationv2.locationproject.LocationProjectsSectionHeader
import com.zstronics.ceibro.ui.tasks.v3.TasksParentTabV3VM
import com.zstronics.ceibro.ui.tasks.v3.bottomsheets.adapters.TaskProjectFilterAdapterSectionRecycler

class ProjectListBottomSheet(
    val viewModel: TasksParentTabV3VM,
    val callback: (ArrayList<CeibroProjectV2>) -> Unit
) :
    BottomSheetDialogFragment() {
    lateinit var mViewDataBinding: FragmentProjectListBinding
    private var searchingProject = false

    var selectedProjects = ArrayList<CeibroProjectV2>()
    private lateinit var sectionedAdapter: TaskProjectFilterAdapterSectionRecycler
    private var sectionList: MutableList<LocationProjectsSectionHeader> = mutableListOf()
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mViewDataBinding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_project_list,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return mViewDataBinding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        this.selectedProjects.clear()

        viewModel.selectedProjectsForFilter.forEach {
            selectedProjects.add(it.copy()) // Creates a new instance using the copy method
        }



        mViewDataBinding.backBtn.setOnClick {
            dismiss()
            mViewDataBinding.locationProjectSearchBar.setQuery("", true)
            viewModel.filterFavoriteProjects("")
            viewModel.filterAllProjects("")
        }

        mViewDataBinding.tvClearAll.setOnClickListener {
            mViewDataBinding.locationProjectSearchBar.setQuery("", true)
            viewModel.filterFavoriteProjects("")
            viewModel.filterAllProjects("")
            selectedProjects.clear()
            viewModel.selectedProjectsForFilter = selectedProjects
            sectionedAdapter.setSelectedList(selectedProjects)
            callback.invoke(selectedProjects)
            dismiss()
        }
        mViewDataBinding.btnApply.setOnClickListener {
            mViewDataBinding.locationProjectSearchBar.setQuery("", true)
            viewModel.filterFavoriteProjects("")
            viewModel.filterAllProjects("")
            viewModel.selectedProjectsForFilter = selectedProjects
            callback.invoke(selectedProjects)
            dismiss()
        }

        mViewDataBinding.projectsRV.visibility = View.VISIBLE

        sectionList.add(
            0,
            LocationProjectsSectionHeader(mutableListOf(), getString(R.string.favorite_projects))
        )
        sectionList.add(
            1,
            LocationProjectsSectionHeader(mutableListOf(), getString(R.string.all_projects))
        )

        sectionedAdapter = TaskProjectFilterAdapterSectionRecycler(requireContext(), sectionList)


        sectionedAdapter.setCallBack { ceibroProjectV2, tag ->
            if (tag) {
                selectedProjects.add(ceibroProjectV2)
            } else {
                selectedProjects.remove(ceibroProjectV2)
            }
        }


        sectionedAdapter.setSelectedList(selectedProjects)

//        val linearLayoutManager = LinearLayoutManager(requireContext())
////        mViewDataBinding.projectsRV.removeAllViews()
//        mViewDataBinding.projectsRV.layoutManager = linearLayoutManager
//        mViewDataBinding.projectsRV.setHasFixedSize(true)
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
                    LocationProjectsSectionHeader(
                        mutableListOf(),
                        getString(R.string.favorite_projects)
                    )
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
                    mViewDataBinding.projectsRV.visibility = View.GONE
                }
            }
        }


        mViewDataBinding.locationProjectSearchBar.setOnQueryTextListener(object :
            SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (query != null) {
                    viewModel.filterFavoriteProjects(query.trim())
                    viewModel.filterAllProjects(query.trim())
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText != null) {
                    viewModel.filterFavoriteProjects(newText.trim())
                    viewModel.filterAllProjects(newText.trim())
                }
                return true
            }
        })

    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
            dialog.behavior.skipCollapsed = false
            dialog.behavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }
        return dialog
    }

    override fun onDestroyView() {
        mViewDataBinding.locationProjectSearchBar.setQuery("", true)
        viewModel.filterFavoriteProjects("")
        viewModel.filterAllProjects("")
        super.onDestroyView()
    }
}