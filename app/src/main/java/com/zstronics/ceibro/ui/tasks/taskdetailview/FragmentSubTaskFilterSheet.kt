package com.zstronics.ceibro.ui.tasks.taskdetailview

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.databinding.FragmentSubTaskFilterSheetBinding
import com.zstronics.ceibro.ui.tasks.newtask.MemberChipAdapter
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject
import kotlin.collections.ArrayList

class FragmentSubTaskFilterSheet constructor(
    mainProjectId: String,
    projects: MutableList<ProjectsWithMembersResponse.ProjectDetail>?,
    _statusList: ArrayList<String>
) :
    BottomSheetDialogFragment() {
    var allProjects: MutableList<ProjectsWithMembersResponse.ProjectDetail>? = projects
    var statusList: ArrayList<String> = _statusList
    var projectId = mainProjectId
    lateinit var binding: FragmentSubTaskFilterSheetBinding

    private val _statusNames: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())
    val statusNames: LiveData<List<String>> = _statusNames

    private val _assigneeTo: MutableLiveData<ArrayList<Member>?> = MutableLiveData(arrayListOf())
    val assigneeTo: MutableLiveData<ArrayList<Member>?> = _assigneeTo

    private val _projectMembers: MutableLiveData<List<Member>> = MutableLiveData(arrayListOf())
    val projectMembers: LiveData<List<Member>> = _projectMembers

    private val _projectMemberNames: MutableLiveData<List<String>> = MutableLiveData(arrayListOf())
    val projectMemberNames: LiveData<List<String>> = _projectMemberNames


    var selectedStatus = ""
    var selectedDueDate = ""

    @Inject
    lateinit var assigneeChipsAdapter: MemberChipAdapter

    var onConfirmClickListener: ((view: View, projectId: String, selectedStatus: String, selectedDueDate: String, assigneeToMembers: List<Member>?) -> Unit)? =
        null
    var onClearAllClickListener: (() -> Unit)? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_sub_task_filter_sheet,
            container,
            false
        )
//        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        assigneeChipsAdapter = MemberChipAdapter()

        val selectedProject = allProjects?.find { it.id == projectId }
        onProjectSelect(selectedProject)

        _statusNames.postValue(statusList.map { it })

        statusNames.observe(viewLifecycleOwner) {
            val arrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    it
                )
            arrayAdapter.setDropDownViewResource(
                android.R.layout
                    .simple_spinner_dropdown_item
            )

            binding.subtaskFilterStatusSpinner.setAdapter(arrayAdapter)

            selectedStatus = statusList[0]
            binding.subtaskFilterStatusSpinner.setText(selectedStatus, false)
        }
        binding.subtaskFilterStatusSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                selectedStatus = statusList[position]
            }



        projectMemberNames.observe(viewLifecycleOwner) {
            val arrayAdapter =
                ArrayAdapter(
                    requireContext(),
                    android.R.layout.simple_spinner_dropdown_item,
                    it
                )
            arrayAdapter.setDropDownViewResource(
                android.R.layout
                    .simple_spinner_dropdown_item
            )

            binding.subtaskFilterAssignToSpinner.setAdapter(arrayAdapter)
        }
        binding.subtaskFilterAssignToSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                onAssigneeSelect(position)
            }



        assigneeTo.observe(viewLifecycleOwner) {
            if (it != null) {
                assigneeChipsAdapter.setList(it)
            }
        }
        binding.subtaskFilterAssigneeChipsRV.adapter = assigneeChipsAdapter
        assigneeChipsAdapter.itemClickListener = { _: View, position: Int, data: Member ->
            removeAssignee(data)
        }


        binding.subtaskFilterDueDateText.setOnClickListener {
            val datePicker =
                DatePickerDialog(
                    requireContext(),
                    dueDateSetListener,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
                )
//            datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
            datePicker.show()
        }







        binding.closeBtn.setOnClickListener {
            dismiss()
        }
        binding.subtaskFilterClearAllBtn.setOnClickListener {
            onClearAllClickListener?.invoke()
            dismiss()
        }
        binding.confirmFilterBtn.setOnClickListener {
            onConfirmClickListener?.invoke(
                it,
                projectId,
                selectedStatus,
                selectedDueDate,
                assigneeTo.value
            )
            dismiss()
        }

    }

    private fun onProjectSelect(selectedProject: ProjectsWithMembersResponse.ProjectDetail?) {
        val projectMemb = selectedProject?.projectMembers as MutableList

        _projectMembers.value = projectMemb
        _projectMemberNames.value = projectMemb.map { it.firstName + " " + it.surName }

    }

    private fun onAssigneeSelect(position: Int) {
        val member: Member? = projectMembers.value?.get(position)
        val assignees = _assigneeTo.value

        val selectedMember = assignees?.find { it.id == member?.id }

        if (selectedMember != null) {
            assignees.remove(selectedMember)
        } else {
            if (member != null) {
                assignees?.add(member)
            }
        }
        _assigneeTo.value = assignees
    }

    private fun removeAssignee(data: Member) {
        val assignee = _assigneeTo.value
        assignee?.remove(data)
        _assigneeTo.value = assignee
    }

    var cal: Calendar = Calendar.getInstance()

    private val dueDateSetListener =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDueDateInView()
        }

    private fun updateDueDateInView() {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        selectedDueDate = sdf.format(cal.time)
        binding.subtaskFilterDueDateText.setText(selectedDueDate)
    }

}