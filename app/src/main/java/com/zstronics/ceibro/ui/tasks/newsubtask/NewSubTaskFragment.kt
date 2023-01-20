package com.zstronics.ceibro.ui.tasks.newsubtask

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.databinding.FragmentNewSubTaskBinding
import com.zstronics.ceibro.databinding.FragmentNewTaskBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import com.zstronics.ceibro.ui.tasks.newtask.MemberChipAdapter
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@AndroidEntryPoint
class NewSubTaskFragment :
    BaseNavViewModelFragment<FragmentNewSubTaskBinding, INewSubTask.State, NewSubTaskVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: NewSubTaskVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_new_sub_task
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn, 1 -> navigateBack()
            R.id.newSubTaskSaveAsDraftBtn -> viewModel.createNewSubTask(TaskStatus.DRAFT.name.lowercase())
            R.id.newSubTaskSaveAndAssignBtn -> viewModel.createNewSubTask(TaskStatus.ASSIGNED.name.lowercase())
            R.id.newSubTaskDueDateText -> {
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
            R.id.newSubTaskStartDateText -> {
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
            R.id.newSubTaskAdvanceOptionBtn -> {
                if (mViewDataBinding.newSubTaskAdvanceOptionLayout.visibility == View.GONE) {
                    mViewDataBinding.newSubTaskAdvanceOptionLayout.visibility = View.VISIBLE
                    mViewDataBinding.newSubTaskAdvanceOptionBtnImg.setImageResource(R.drawable.icon_navigate_down)
                } else {
                    mViewDataBinding.newSubTaskAdvanceOptionLayout.visibility = View.GONE
                    mViewDataBinding.newSubTaskAdvanceOptionBtnImg.setImageResource(R.drawable.icon_navigate_next)
                }

            }
        }
    }

    @Inject
    lateinit var assigneeChipsAdapter: MemberChipAdapter

    @Inject
    lateinit var viewersChipsAdapter: MemberChipAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.task.observe(viewLifecycleOwner) { item ->
        }

        viewModel.taskAssignee.observe(viewLifecycleOwner) {
            assigneeChipsAdapter.setList(it)
        }

        viewModel.viewers.observe(viewLifecycleOwner) {
            viewersChipsAdapter.setList(it)
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

            mViewDataBinding.newSubTaskAssignToSpinner.setAdapter(arrayAdapter)
            mViewDataBinding.newSubTaskViewerSpinner.setAdapter(arrayAdapter)
        }


        mViewDataBinding.newSubTaskAssignToSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.onAssigneeSelect(position)
            }

        mViewDataBinding.newSubTaskViewerSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.onViewerSelect(position)
            }

        mViewDataBinding.assigneeChipsRV.adapter = assigneeChipsAdapter
        assigneeChipsAdapter.itemClickListener = { _: View, position: Int, data: Member ->
            viewModel.removeAssignee(data)
        }

        mViewDataBinding.viewersChipsRV.adapter = viewersChipsAdapter
        viewersChipsAdapter.itemClickListener = { _: View, position: Int, data: Member ->
            viewModel.removeViewer(data)
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
        val myFormat = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        val formatToSend = "yyyy-MM-dd"
        val sdf1 = SimpleDateFormat(formatToSend, Locale.US)

        viewState.dueDate = sdf1.format(cal.time)

        mViewDataBinding.newSubTaskDueDateText.setText(sdf.format(cal.time))
    }

    private fun updateStartDateInView() {
        val myFormat = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        val formatToSend = "yyyy-MM-dd"
        val sdf1 = SimpleDateFormat(formatToSend, Locale.US)

        viewState.startDate = sdf1.format(cal.time)

        mViewDataBinding.newSubTaskStartDateText.setText(sdf.format(cal.time))
    }
}