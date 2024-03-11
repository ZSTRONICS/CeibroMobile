package com.zstronics.ceibro.ui.tasks.v3.bottomsheets


import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.databinding.LayoutTaskSortingBinding

class TaskSortingV3BottomSheet(lastSortingTypeParam: String) : BottomSheetDialogFragment() {
    lateinit var binding: LayoutTaskSortingBinding
    var onChangeSortingType: ((sortingType: String) -> Unit)? = null
    private var lastSortingType = lastSortingTypeParam

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.layout_task_sorting,
            container,
            false
        )
        //set to adjust screen height automatically, when soft keyboard appears on screen
        dialog?.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (lastSortingType.equals("SortByActivity", true)) {
            binding.latestActivitySortBtn.isClickable = false
            binding.unseenSortBtn.isClickable = true
            binding.newTaskSortBtn.isClickable = true
            binding.byProjectSortBtn.isClickable = true
            binding.dueDateSortBtn.isClickable = true

            setDrawableEndWithTint(
                binding.latestActivitySortBtn,
                R.drawable.icon_tick_mark,
                R.color.appBlue
            )
            removeDrawableEnd(binding.unseenSortBtn)
            removeDrawableEnd(binding.newTaskSortBtn)
            removeDrawableEnd(binding.byProjectSortBtn)
            removeDrawableEnd(binding.dueDateSortBtn)

        } else if (lastSortingType.equals("SortByUnseen", true)) {
            binding.unseenSortBtn.isClickable = false
            binding.latestActivitySortBtn.isClickable = true
            binding.newTaskSortBtn.isClickable = true
            binding.byProjectSortBtn.isClickable = true
            binding.dueDateSortBtn.isClickable = true

            setDrawableEndWithTint(
                binding.unseenSortBtn,
                R.drawable.icon_tick_mark,
                R.color.appBlue
            )
            removeDrawableEnd(binding.latestActivitySortBtn)
            removeDrawableEnd(binding.newTaskSortBtn)
            removeDrawableEnd(binding.byProjectSortBtn)
            removeDrawableEnd(binding.dueDateSortBtn)

        } else if (lastSortingType.equals("SortByNewTask", true)) {
            binding.newTaskSortBtn.isClickable = false
            binding.dueDateSortBtn.isClickable = true
            binding.unseenSortBtn.isClickable = true
            binding.latestActivitySortBtn.isClickable = true
            binding.byProjectSortBtn.isClickable = true

            setDrawableEndWithTint(
                binding.newTaskSortBtn,
                R.drawable.icon_tick_mark,
                R.color.appBlue
            )
            removeDrawableEnd(binding.dueDateSortBtn)
            removeDrawableEnd(binding.unseenSortBtn)
            removeDrawableEnd(binding.latestActivitySortBtn)
            removeDrawableEnd(binding.byProjectSortBtn)

        } else if (lastSortingType.equals("SortByProject", true)) {
            binding.byProjectSortBtn.isClickable = false
            binding.dueDateSortBtn.isClickable = true
            binding.unseenSortBtn.isClickable = true
            binding.latestActivitySortBtn.isClickable = true
            binding.newTaskSortBtn.isClickable = true

            setDrawableEndWithTint(
                binding.byProjectSortBtn,
                R.drawable.icon_tick_mark,
                R.color.appBlue
            )
            removeDrawableEnd(binding.dueDateSortBtn)
            removeDrawableEnd(binding.unseenSortBtn)
            removeDrawableEnd(binding.latestActivitySortBtn)
            removeDrawableEnd(binding.newTaskSortBtn)

        } else if (lastSortingType.equals("SortByDueDate", true)) {
            binding.dueDateSortBtn.isClickable = false
            binding.unseenSortBtn.isClickable = true
            binding.newTaskSortBtn.isClickable = true
            binding.latestActivitySortBtn.isClickable = true
            binding.byProjectSortBtn.isClickable = true

            setDrawableEndWithTint(
                binding.dueDateSortBtn,
                R.drawable.icon_tick_mark,
                R.color.appBlue
            )
            removeDrawableEnd(binding.unseenSortBtn)
            removeDrawableEnd(binding.newTaskSortBtn)
            removeDrawableEnd(binding.latestActivitySortBtn)
            removeDrawableEnd(binding.byProjectSortBtn)

        }


        binding.latestActivitySortBtn.setOnClick {
            binding.latestActivitySortBtn.isClickable = false
            binding.unseenSortBtn.isClickable = true
            binding.newTaskSortBtn.isClickable = true
            binding.byProjectSortBtn.isClickable = true
            binding.dueDateSortBtn.isClickable = true

            setDrawableEndWithTint(
                binding.latestActivitySortBtn,
                R.drawable.icon_tick_mark,
                R.color.appBlue
            )

            lastSortingType = "SortByActivity"
            removeDrawableEnd(binding.unseenSortBtn)
            removeDrawableEnd(binding.newTaskSortBtn)
            removeDrawableEnd(binding.byProjectSortBtn)
            removeDrawableEnd(binding.dueDateSortBtn)
        }

        binding.unseenSortBtn.setOnClick {
            binding.unseenSortBtn.isClickable = false
            binding.latestActivitySortBtn.isClickable = true
            binding.newTaskSortBtn.isClickable = true
            binding.byProjectSortBtn.isClickable = true
            binding.dueDateSortBtn.isClickable = true

            setDrawableEndWithTint(
                binding.unseenSortBtn,
                R.drawable.icon_tick_mark,
                R.color.appBlue
            )
            lastSortingType = "SortByUnseen"
            removeDrawableEnd(binding.latestActivitySortBtn)
            removeDrawableEnd(binding.newTaskSortBtn)
            removeDrawableEnd(binding.byProjectSortBtn)
            removeDrawableEnd(binding.dueDateSortBtn)
        }

        binding.newTaskSortBtn.setOnClick {
            binding.newTaskSortBtn.isClickable = false
            binding.dueDateSortBtn.isClickable = true
            binding.unseenSortBtn.isClickable = true
            binding.latestActivitySortBtn.isClickable = true
            binding.byProjectSortBtn.isClickable = true

            setDrawableEndWithTint(
                binding.newTaskSortBtn,
                R.drawable.icon_tick_mark,
                R.color.appBlue
            )
            lastSortingType = "SortByNewTask"

            removeDrawableEnd(binding.dueDateSortBtn)
            removeDrawableEnd(binding.unseenSortBtn)
            removeDrawableEnd(binding.latestActivitySortBtn)
            removeDrawableEnd(binding.byProjectSortBtn)
            onChangeSortingType?.invoke(lastSortingType)
        }
        binding.byProjectSortBtn.setOnClick {
            binding.byProjectSortBtn.isClickable = false
            binding.dueDateSortBtn.isClickable = true
            binding.unseenSortBtn.isClickable = true
            binding.latestActivitySortBtn.isClickable = true
            binding.newTaskSortBtn.isClickable = true

            setDrawableEndWithTint(
                binding.byProjectSortBtn,
                R.drawable.icon_tick_mark,
                R.color.appBlue
            )
            lastSortingType = "SortByProject"
            removeDrawableEnd(binding.dueDateSortBtn)
            removeDrawableEnd(binding.unseenSortBtn)
            removeDrawableEnd(binding.latestActivitySortBtn)
            removeDrawableEnd(binding.newTaskSortBtn)
            onChangeSortingType?.invoke(lastSortingType)
        }

        binding.dueDateSortBtn.setOnClick {
            binding.dueDateSortBtn.isClickable = false
            binding.byProjectSortBtn.isClickable = true
            binding.unseenSortBtn.isClickable = true
            binding.latestActivitySortBtn.isClickable = true
            binding.newTaskSortBtn.isClickable = true

            setDrawableEndWithTint(
                binding.dueDateSortBtn,
                R.drawable.icon_tick_mark,
                R.color.appBlue
            )
            lastSortingType = "SortByDueDate"
            removeDrawableEnd(binding.byProjectSortBtn)
            removeDrawableEnd(binding.unseenSortBtn)
            removeDrawableEnd(binding.latestActivitySortBtn)
            removeDrawableEnd(binding.newTaskSortBtn)
            onChangeSortingType?.invoke(lastSortingType)
        }

        binding.closeBtn.setOnClick {
            dismiss()
        }
    }

    // Function to set drawableEnd and drawableTint programmatically
    @SuppressLint("UseCompatTextViewDrawableApis")
    private fun setDrawableEndWithTint(textView: TextView, drawableResId: Int, tintResId: Int) {
        val drawable = ContextCompat.getDrawable(textView.context, drawableResId)
        drawable?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            textView.setCompoundDrawables(null, null, it, null)
            textView.compoundDrawableTintList =
                ContextCompat.getColorStateList(textView.context, tintResId)
            textView.compoundDrawableTintMode = PorterDuff.Mode.SRC_IN
        }
    }

    // Function to remove drawableEnd
    private fun removeDrawableEnd(textView: TextView) {
        textView.setCompoundDrawables(null, null, null, null)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        if (dialog is BottomSheetDialog) {
//            dialog.behavior.skipCollapsed = true
            dialog.behavior.state = STATE_COLLAPSED
        }
        return dialog

    }
}