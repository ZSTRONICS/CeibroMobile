package com.zstronics.ceibro.ui.tasks.newsubtask

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
import com.zstronics.ceibro.databinding.FragmentNewSubTaskBinding
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment
import com.zstronics.ceibro.ui.tasks.newtask.MemberChipAdapter
import com.zstronics.ceibro.ui.tasks.subtask.SubTaskStatus
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
            R.id.closeBtn, 1, R.id.newSubTaskCancelBtn, 3 -> navigateBack()
            R.id.newSubTaskSaveAsDraftBtn -> {
                viewModel.createNewSubTask(
                    SubTaskStatus.DRAFT.name.lowercase(),
                    requireContext()
                ) {
                    if (viewModel.fileUriList.value?.isNotEmpty() == true) {
                        createNotification(
                            it,
                            "Subtask",
                            "Uploading files for Subtask",
                            isOngoing = true,
                            indeterminate = true
                        )
                    }
                }
            }
            R.id.newSubTaskSaveAndAssignBtn -> {
                viewModel.createNewSubTask(
                    SubTaskStatus.ASSIGNED.name.lowercase(),
                    requireContext()
                ) {
                    if (viewModel.fileUriList.value?.isNotEmpty() == true) {
                        createNotification(
                            it,
                            "Subtask",
                            "Uploading files for Subtask",
                            isOngoing = true,
                            indeterminate = true
                        )
                    }
                }
            }
            R.id.updateSubTaskAsDraftBtn -> viewModel.updateDraftSubTask(
                viewModel.subtaskId,
                SubTaskStatus.DRAFT.name.lowercase()
            )
            R.id.updateSubTaskSaveAndAssignBtn -> viewModel.updateDraftSubTask(
                viewModel.subtaskId,
                SubTaskStatus.ASSIGNED.name.lowercase()
            )
            R.id.updateSubTaskBtn -> viewModel.updateAssignedSubTask(viewModel.subtaskId)
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
            R.id.newSubTaskAttachmentBtn -> pickAttachment(true)
        }
    }

    @Inject
    lateinit var assigneeChipsAdapter: MemberChipAdapter

    @Inject
    lateinit var viewersChipsAdapter: MemberChipAdapter

    @Inject
    lateinit var attachmentAdapter: AttachmentAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.task.observe(viewLifecycleOwner) { item ->
        }

        mViewDataBinding.newSubTaskSaveAsDraftBtn.visibility = View.VISIBLE
        mViewDataBinding.newSubTaskSaveAndAssignBtn.visibility = View.VISIBLE
        mViewDataBinding.updateSubTaskAsDraftBtn.visibility = View.GONE
        mViewDataBinding.updateSubTaskSaveAndAssignBtn.visibility = View.GONE
        mViewDataBinding.updateSubTaskBtn.visibility = View.GONE

        viewModel.subtask.observe(viewLifecycleOwner) { item ->
            if (!viewModel.isNewSubTask) {        // If not a new task, then its in edit mode
                mViewDataBinding.subtaskHeading.text =
                    requireContext().getString(R.string.update_subtask_heading)

                viewState.dueDate = item.dueDate
                mViewDataBinding.newSubTaskDueDateText.setText(item.dueDate)
                viewState.subtaskTitle.value = item.title
                viewState.description.value = item.description

                mViewDataBinding.newSubTaskSaveAsDraftBtn.visibility = View.GONE
                mViewDataBinding.newSubTaskSaveAndAssignBtn.visibility = View.GONE

                val userState =
                    item.state?.find { it.userId == viewModel.user?.id }?.userState?.uppercase()
                        ?: TaskStatus.DRAFT.name

                if (userState.uppercase() == SubTaskStatus.DRAFT.name) {
                    mViewDataBinding.updateSubTaskAsDraftBtn.visibility = View.VISIBLE
                    mViewDataBinding.updateSubTaskSaveAndAssignBtn.visibility = View.VISIBLE
                    mViewDataBinding.updateSubTaskBtn.visibility = View.GONE
                } else {
                    mViewDataBinding.updateSubTaskAsDraftBtn.visibility = View.GONE
                    mViewDataBinding.updateSubTaskSaveAndAssignBtn.visibility = View.GONE
                    mViewDataBinding.updateSubTaskBtn.visibility = View.VISIBLE

                    mViewDataBinding.newSubTaskTitleText.isEnabled = false
                    mViewDataBinding.newSubTaskTitleText.isFocusable = false

                    assigneeChipsAdapter.itemClickListener = null

                    mViewDataBinding.newSubTaskDueDateText.isEnabled = false
                    mViewDataBinding.newSubTaskDueDateText.isFocusable = false

                    mViewDataBinding.newSubTaskAssignToSpinner.dropDownHeight = 0
                    mViewDataBinding.newSubTaskAssignToSpinner.isEnabled = false
                    mViewDataBinding.newSubTaskAssignToSpinner.isFocusable = false
                }

            }


        }

        viewModel.taskAssignee.observe(viewLifecycleOwner) {
            assigneeChipsAdapter.setList(it)
        }

//        viewModel.viewers.observe(viewLifecycleOwner) {
//            viewersChipsAdapter.setList(it)
//        }

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
//            mViewDataBinding.newSubTaskViewerSpinner.setAdapter(arrayAdapter)
        }


        mViewDataBinding.newSubTaskAssignToSpinner.onItemClickListener =
            AdapterView.OnItemClickListener { _, _, position, _ ->
                viewModel.onAssigneeSelect(position)
            }

        mViewDataBinding.assigneeChipsRV.adapter = assigneeChipsAdapter
        assigneeChipsAdapter.itemClickListener = { _: View, position: Int, data: Member ->
            viewModel.removeAssignee(data)
        }


//        mViewDataBinding.newSubTaskViewerSpinner.onItemClickListener =
//            AdapterView.OnItemClickListener { _, _, position, _ ->
//                viewModel.onViewerSelect(position)
//            }

//        mViewDataBinding.viewersChipsRV.adapter = viewersChipsAdapter
//        viewersChipsAdapter.itemClickListener = { _: View, position: Int, data: Member ->
//            viewModel.removeViewer(data)
//        }

        mViewDataBinding.attachmentRecyclerView.adapter = attachmentAdapter

        viewModel.fileUriList.observe(viewLifecycleOwner) { list ->
            attachmentAdapter.setList(list)
        }
        attachmentAdapter.itemClickListener =
            { _: View, position: Int, data: SubtaskAttachment? ->
                viewModel.removeFile(position)
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

        mViewDataBinding.newSubTaskDueDateText.setText(sdf.format(cal.time))
    }

    private fun updateStartDateInView() {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        val formatToSend = "dd-MM-yyyy"
        val sdf1 = SimpleDateFormat(formatToSend, Locale.US)

        viewState.startDate = sdf1.format(cal.time)

        mViewDataBinding.newSubTaskStartDateText.setText(sdf.format(cal.time))
    }
}