package com.zstronics.ceibro.ui.tasks.v2.newtask

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.MotionEvent
import android.view.View
import androidx.fragment.app.viewModels
import com.google.android.material.textfield.TextInputLayout
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentNewTaskV2Binding
import com.zstronics.ceibro.ui.tasks.v2.tasktome.TaskToMeFragment
import dagger.hilt.android.AndroidEntryPoint
import ee.zstronics.ceibro.camera.CeibroCameraActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


@AndroidEntryPoint
class NewTaskV2Fragment :
    BaseNavViewModelFragment<FragmentNewTaskV2Binding, INewTaskV2.State, NewTaskV2VM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: NewTaskV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_new_task_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
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

            R.id.newTaskPhotoBtn -> {
                val ceibroCamera = Intent(
                    requireContext(),
                    CeibroCameraActivity::class.java
                )
                startActivity(ceibroCamera)
            }

            R.id.newTaskAttachBtn -> {
                if (viewState.isAttachLayoutOpen.value == true) {
                    viewState.isAttachLayoutOpen.value = false
                    mViewDataBinding.newTaskAttachmentLayout.animate()
                        .translationY(mViewDataBinding.newTaskAttachmentLayout.height.toFloat())
                        .setDuration(350)
                        .withEndAction {
                            mViewDataBinding.newTaskAttachmentLayout.visibility = View.GONE
                        }
                        .start()
                } else {
                    viewState.isAttachLayoutOpen.value = true
                    mViewDataBinding.newTaskAttachmentLayout.visibility = View.VISIBLE
                    mViewDataBinding.newTaskAttachmentLayout.animate()
                        .translationY(0f)
                        .setDuration(350)
                        .start()
                }
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mViewDataBinding.newTaskTopicField.endIconMode = TextInputLayout.END_ICON_CUSTOM
        mViewDataBinding.newTaskTopicField.setEndIconDrawable(R.drawable.icon_close_round_grey)
        mViewDataBinding.newTaskTopicField.isEndIconVisible = false
        mViewDataBinding.newTaskTopicField.setEndIconOnClickListener {
            mViewDataBinding.newTaskTopicField.editText?.setText("")
        }
        mViewDataBinding.newTaskTopicField.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mViewDataBinding.newTaskTopicField.isEndIconVisible = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        mViewDataBinding.newTaskAssignToField.endIconMode = TextInputLayout.END_ICON_CUSTOM
        mViewDataBinding.newTaskAssignToField.setEndIconDrawable(R.drawable.icon_close_round_grey)
        mViewDataBinding.newTaskAssignToField.isEndIconVisible = false
        mViewDataBinding.newTaskAssignToField.setEndIconOnClickListener {
            mViewDataBinding.newTaskAssignToField.editText?.setText("")
        }
        mViewDataBinding.newTaskAssignToField.editText?.addTextChangedListener(object :
            TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mViewDataBinding.newTaskAssignToField.isEndIconVisible = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        mViewDataBinding.newTaskProjectField.endIconMode = TextInputLayout.END_ICON_CUSTOM
        mViewDataBinding.newTaskProjectField.setEndIconDrawable(R.drawable.icon_close_round_grey)
        mViewDataBinding.newTaskProjectField.isEndIconVisible = false
        mViewDataBinding.newTaskProjectField.setEndIconOnClickListener {
            mViewDataBinding.newTaskProjectField.editText?.setText("")
        }
        mViewDataBinding.newTaskProjectField.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mViewDataBinding.newTaskProjectField.isEndIconVisible = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        mViewDataBinding.newTaskDueDateField.endIconMode = TextInputLayout.END_ICON_CUSTOM
        mViewDataBinding.newTaskDueDateField.setEndIconDrawable(R.drawable.icon_close_round_grey)
        mViewDataBinding.newTaskDueDateField.isEndIconVisible = false
        mViewDataBinding.newTaskDueDateField.setEndIconOnClickListener {
            mViewDataBinding.newTaskDueDateField.editText?.setText("")
        }
        mViewDataBinding.newTaskDueDateField.editText?.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mViewDataBinding.newTaskDueDateField.isEndIconVisible = !s.isNullOrEmpty()
            }

            override fun afterTextChanged(s: Editable?) {}
        })



        mViewDataBinding.newTaskDoneReqSwitch.setOnCheckedChangeListener { buttonView, isChecked ->
            viewState.isDoneReqAllowed.value = isChecked
        }
        val handler = Handler()
        handler.postDelayed(Runnable {
            mViewDataBinding.newTaskAttachmentLayout.animate()
                .translationY(mViewDataBinding.newTaskAttachmentLayout.height.toFloat())
                .setDuration(20)
                .withEndAction { mViewDataBinding.newTaskAttachmentLayout.visibility = View.GONE }
                .start()
        }, 20)

        mViewDataBinding.newTaskDescriptionText.setOnTouchListener { view, event ->
            view.parent.requestDisallowInterceptTouchEvent(true)
            if ((event.action and MotionEvent.ACTION_MASK) == MotionEvent.ACTION_UP) {
                view.parent.requestDisallowInterceptTouchEvent(false)
            }
            return@setOnTouchListener false
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


    private fun updateDueDateInView() {
        val myFormat = "dd-MM-yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)

        val formatToSend = "dd-MM-yyyy"
        val sdf1 = SimpleDateFormat(formatToSend, Locale.US)

        viewState.dueDate = sdf1.format(cal.time)

        mViewDataBinding.newTaskDueDateText.setText(sdf.format(cal.time))
    }

}