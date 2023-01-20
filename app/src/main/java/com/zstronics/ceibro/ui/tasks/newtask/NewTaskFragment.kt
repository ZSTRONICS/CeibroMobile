package com.zstronics.ceibro.ui.tasks.newtask

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.databinding.FragmentNewTaskBinding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NewTaskFragment :
    BaseNavViewModelFragment<FragmentNewTaskBinding, INewTask.State, NewTaskVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: NewTaskVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_new_task
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            1 -> navigateBack()
            R.id.closeBtn, R.id.newTaskCancelBtn -> navigateBack()
            R.id.newTaskSaveAsDraftBtn -> viewModel.createNewTask(TaskStatus.DRAFT.name.lowercase())
            R.id.newTaskCreateBtn -> viewModel.createNewTask(TaskStatus.NEW.name.lowercase())
            R.id.newTaskDueDateText -> {
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
            R.id.newTaskStartDateText -> {
                val datePicker =
                    DatePickerDialog(
                        requireContext(),
                        startDateSetListener,
                        // set DatePickerDialog to point to today's date when it loads up
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
                datePicker.show()
            }
            R.id.newTaskAdvanceOptionBtn -> {
                if (mViewDataBinding.newTaskAdvanceOptionLayout.visibility == View.GONE) {
                    mViewDataBinding.newTaskAdvanceOptionLayout.visibility = View.VISIBLE
                    mViewDataBinding.newTaskAdvanceOptionBtnImg.setImageResource(R.drawable.icon_navigate_down)
                } else {
                    mViewDataBinding.newTaskAdvanceOptionLayout.visibility = View.GONE
                    mViewDataBinding.newTaskAdvanceOptionBtnImg.setImageResource(R.drawable.icon_navigate_next)
                }

            }
        }
    }

    @Inject
    lateinit var adminsChipsAdapter: MemberChipAdapter

    @Inject
    lateinit var assigneeChipsAdapter: MemberChipAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.projectNames.observe(viewLifecycleOwner) {
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

            mViewDataBinding.newTaskProjectSpinner.setAdapter(arrayAdapter)
        }

        viewModel.projectMemberNames.observe(viewLifecycleOwner) {
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

            mViewDataBinding.newTaskAdminsSpinner.setAdapter(arrayAdapter)
            mViewDataBinding.newTaskAssignToSpinner.setAdapter(arrayAdapter)
        }

        mViewDataBinding.newTaskProjectSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.onProjectSelect(position)
            }

        mViewDataBinding.newTaskAdminsSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.onAdminSelect(position)
            }

        mViewDataBinding.newTaskAssignToSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.onAssigneeSelect(position)
            }

        viewModel.taskAdmins.observe(viewLifecycleOwner) {
            adminsChipsAdapter.setList(it)
        }

        viewModel.taskAssignee.observe(viewLifecycleOwner) {
            assigneeChipsAdapter.setList(it)
        }

        mViewDataBinding.newTaskAdminsChipsRV.adapter = adminsChipsAdapter

        mViewDataBinding.newTaskAssigneeChipsRV.adapter = assigneeChipsAdapter

        adminsChipsAdapter.itemClickListener = { _: View, position: Int, data: Member ->
            viewModel.removeAdmin(data)
        }

        assigneeChipsAdapter.itemClickListener = { _: View, position: Int, data: Member ->
            viewModel.removeAssignee(data)
        }
    }

    var cal: Calendar = Calendar.getInstance()

    private val dueDateSetListener =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDueDateInView()
        }
    private val startDateSetListener =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateStartDateInView()
        }

    private fun updateDueDateInView() {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        val formatToSend = "dd-MM-yyyy"
        val sdf1 = SimpleDateFormat(formatToSend, Locale.US)

        viewState.dueDate = sdf1.format(cal.time)

        mViewDataBinding.newTaskDueDateText.setText(sdf.format(cal.time))
    }

    private fun updateStartDateInView() {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        val formatToSend = "dd-MM-yyyy"
        val sdf1 = SimpleDateFormat(formatToSend, Locale.US)

        viewState.startDate = sdf1.format(cal.time)

        mViewDataBinding.newTaskStartDateText.setText(sdf.format(cal.time))
    }
}