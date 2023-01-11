package com.zstronics.ceibro.ui.tasks.newsubtask

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentNewSubTaskBinding
import com.zstronics.ceibro.databinding.FragmentNewTaskBinding
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

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
            R.id.closeBtn -> navigateBack()
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
            R.id.newTaskAdvanceOptionBtn -> {
                if (mViewDataBinding.newSubTaskAdvanceOptionLayout.visibility == View.GONE) {
                    mViewDataBinding.newSubTaskAdvanceOptionLayout.visibility = View.VISIBLE
                    mViewDataBinding.newSubTaskAdvanceOptionBtnImg.setImageResource(R.drawable.icon_navigate_down)
                }
                else {
                    mViewDataBinding.newSubTaskAdvanceOptionLayout.visibility = View.GONE
                    mViewDataBinding.newSubTaskAdvanceOptionBtnImg.setImageResource(R.drawable.icon_navigate_next)
                }

            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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