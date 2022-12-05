package com.zstronics.ceibro.ui.questioner.createquestion

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentCreateQuestionBinding
import com.zstronics.ceibro.ui.questioner.createquestion.members.FragmentQuestionParticipantsSheet
import dagger.hilt.android.AndroidEntryPoint
import java.text.SimpleDateFormat
import java.util.*

@AndroidEntryPoint
class CreateQuestionFragment :
    BaseNavViewModelFragment<FragmentCreateQuestionBinding, ICreateQuestion.State, CreateQuestionVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: CreateQuestionVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_create_question
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.closeBtn, 111 -> navigateBack()
            R.id.saveBtn -> viewModel.onSave()
            R.id.dueDate -> {

                val datePicker =
                    DatePickerDialog(
                        requireContext(),
                        dateSetListener,
                        // set DatePickerDialog to point to today's date when it loads up
                        cal.get(Calendar.YEAR),
                        cal.get(Calendar.MONTH),
                        cal.get(Calendar.DAY_OF_MONTH)
                    )
                datePicker.datePicker.minDate = System.currentTimeMillis() - 1000
                datePicker.show()
            }
            R.id.showParticipants -> showParticipantsSheet()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        with(viewModel) {
            questions.observe(viewLifecycleOwner) { list ->
                adapter.setList(list)
            }
            adapter.questionCreateListener = this
            mViewDataBinding.questionsRV.adapter = adapter
        }
    }

    var cal: Calendar = Calendar.getInstance()

    private val dateSetListener =
        DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->
            cal.set(Calendar.YEAR, year)
            cal.set(Calendar.MONTH, monthOfYear)
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

    private fun updateDateInView() {
        val myFormat = "MM/dd/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        val formatToSend = "yyyy-MM-dd"
        val sdf1 = SimpleDateFormat(formatToSend, Locale.US)

        viewState.dueDate = sdf1.format(cal.time)

        mViewDataBinding.dueDate.text = sdf.format(cal.time)
    }

    private fun showParticipantsSheet() {
        val fragment = viewModel.chatRoom?.members?.let { FragmentQuestionParticipantsSheet(it) }
        fragment?.onDoneClick = { view, updatedList ->
            viewState.participants.value = updatedList
        }
        fragment?.show(childFragmentManager, "FragmentQuestionParticipantsSheet")
    }
}