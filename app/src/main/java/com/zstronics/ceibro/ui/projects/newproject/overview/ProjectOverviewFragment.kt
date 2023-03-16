package com.zstronics.ceibro.ui.projects.newproject.overview

import android.Manifest
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.databinding.FragmentProjectOverviewBinding
import com.zstronics.ceibro.extensions.openFilePicker
import com.zstronics.ceibro.ui.projects.newproject.overview.addnewstatus.AddNewStatusSheet
import com.zstronics.ceibro.ui.projects.newproject.overview.ownersheet.OwnerSelectionSheet
import com.zstronics.ceibro.ui.projects.newproject.overview.ownersheet.ProjectStateHandler
import com.zstronics.ceibro.ui.tasks.newtask.MemberChipAdapter
import dagger.hilt.android.AndroidEntryPoint
import okhttp3.internal.immutableListOf
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class ProjectOverviewFragment(
    private val projectStateHandler: ProjectStateHandler,
    private val projectLive: MutableLiveData<AllProjectsResponse.Projects>,
    private val allConnections: LiveData<ArrayList<MyConnection>>
) :
    BaseNavViewModelFragment<FragmentProjectOverviewBinding, IProjectOverview.State, ProjectOverviewVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectOverviewVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_project_overview
    override fun toolBarVisibility(): Boolean = true
    override fun getToolBarTitle() =
        projectLive.value?.title ?: getString(R.string.new_projects_title)

    override fun hasOptionMenu(): Boolean = false
    private lateinit var assigneeChipsAdapter: MemberChipAdapter
    var cal: Calendar = Calendar.getInstance()

    private val dueDateSetListener =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            val formatToSend = "dd-MM-yyyy"
            val sdf1 = SimpleDateFormat(formatToSend, Locale.US)

            viewState.dueDate.value = sdf1.format(cal.time)
        }

    override fun onClick(id: Int) {
        when (id) {
            R.id.createProjectBtn -> {
//                val projectData = viewModel.getMockedProject()
//                projectStateHandler.onProjectCreated(projectData?.createProject)
//                viewState.project.postValue(projectData?.createProject)
                with(viewState) {
                    var checkPass = 0
                    if (projectPhoto.value != null) {
                        checkPass++
                    }

                    if (!projectTitle.value.isNullOrEmpty()) {
                        checkPass++
                    }

                    if (!location.value.isNullOrEmpty()) {
                        checkPass++
                    }

                    if (!description.value.isNullOrEmpty()) {
                        checkPass++
                    }
                    if (!dueDate.value.isNullOrEmpty()) {
                        checkPass++
                    }

                    if (!status.value.isNullOrEmpty()) {
                        checkPass++
                    }
                    if (checkPass == 6) {
                        viewModel.createProject(requireContext(), projectStateHandler)
                    } else {
                        if (projectPhoto.value == null) {
                            showToast("Attach photo")
                        } else if (projectTitle.value.isNullOrEmpty()) {
                            showToast("Project title required")
                        } else if (location.value.isNullOrEmpty()) {
                            showToast("Project location required")
                        } else if (description.value.isNullOrEmpty()) {
                            showToast("Project description required")
                        } else if (dueDate.value.isNullOrEmpty()) {
                            showToast("Project dueDate required")
                        } else if (status.value.isNullOrEmpty()) {
                            showToast("Project status required")
                        }
                    }
                }
            }
            R.id.projectPhoto -> {
                checkPermission(
                    immutableListOf(
                        Manifest.permission.CAMERA,
                    )
                ) {
                    chooseFile(
                        arrayOf(
                            "image/png",
                            "image/jpg",
                            "image/jpeg",
                            "image/*"
                        )
                    )
                }
            }
            R.id.dueDateText -> {
                val datePicker =
                    DatePickerDialog(
                        requireContext(),
                        dueDateSetListener,
                        // set DatePickerDialog to point to today's date when it loads up
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
                datePicker.show()
            }
            R.id.statusText -> showStatusSheet()
            R.id.projectOwner -> showOwnersSelectionSheet()
            R.id.cancelButton -> navigateBack()
        }
    }

    private fun chooseFile(mimeTypes: Array<String>) {
        requireActivity().openFilePicker(
            getString(R.string.add_project_photo), mimeTypes,
            completionHandler = fileCompletionHandler
        )
    }

    private var fileCompletionHandler: ((resultCode: Int, data: Intent?) -> Unit)? = { _, intent ->
        intent?.let { intentData ->
            viewState.projectPhoto.value = intentData.data
        }
    }

    private fun showStatusSheet() {
        val fragment = ProjectStatusViewSheet(viewModel.projectStatuses)

        fragment.onDelete = { position ->
            /// do edit the status
            viewModel.deleteStatus(position)
        }

        fragment.onSelect = { status ->
            viewState.status.value = status
        }
        fragment.onAddNew = {
            val addNewStatusSheet = AddNewStatusSheet("")

            addNewStatusSheet.onAdd = { status ->
                viewModel.addStatus(status)
            }
            addNewStatusSheet.show(childFragmentManager, "AddNewStatusSheet")
        }
        fragment.onEditStatus = { position, status ->
            val addNewStatusSheet = AddNewStatusSheet(status)

            addNewStatusSheet.onEdited = { updatedStatus ->
                viewModel.updateStatus(position, updatedStatus)
            }
            addNewStatusSheet.show(childFragmentManager, "AddNewStatusSheet")
        }

        fragment.show(childFragmentManager, "ProjectStatusViewSheet")
    }

    private fun showOwnersSelectionSheet() {
        val fragment = OwnerSelectionSheet(
            allConnections.value,
            viewModel.sessionManager,
            viewModel.owners
        )
        fragment.onSelect = { member ->
            viewModel.addOrRemoveOwner(member)
        }
        fragment.show(childFragmentManager, "OwnerSelectionSheet")
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.owners.observe(viewLifecycleOwner) {
            if (it.isEmpty()) {
                mViewDataBinding.projectOwner.setText("No Owner Selected")
            } else {
                mViewDataBinding.projectOwner.setText("${it.size} Owner(s) selected")
            }
        }
        if (projectLive.value != null) {
            viewState.projectCreated.postValue(true)
            viewModel.project = projectLive.value
        }
        projectLive.observe(viewLifecycleOwner) {
            viewState.dueDate.postValue(it.dueDate)
            viewState.status.postValue(it.publishStatus)
            viewState.projectTitle.postValue(it.title)
            viewState.location.postValue(it.location)
            viewState.description.postValue(it.description)
            viewModel.addAllStatus(it.extraStatus)
            viewModel.setSelectedOwners(it.owner)
        }

        assigneeChipsAdapter = MemberChipAdapter()

        assigneeChipsAdapter.itemClickListener = { _: View, position: Int, data: Member ->
            viewModel.removeOwner(data)
        }

        viewModel.ownersMemberList.observe(viewLifecycleOwner) {
            if (it != null) {
                assigneeChipsAdapter.setList(it)
            }
        }
        mViewDataBinding.membersChipsRV.adapter = assigneeChipsAdapter
    }
}