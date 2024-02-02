package com.zstronics.ceibro.ui.inbox

import android.app.Dialog
import android.graphics.PorterDuff
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_COLLAPSED
import com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.data.database.models.tasks.CeibroTaskV2
import com.zstronics.ceibro.data.repos.task.TaskRootStateTags
import com.zstronics.ceibro.databinding.FragmentTaskInfoBinding
import com.zstronics.ceibro.databinding.LayoutInboxSortingBinding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus
import com.zstronics.ceibro.utils.DateUtils

class InboxSortingBottomSheet(lastSortingTypeParam: String) : BottomSheetDialogFragment() {
    lateinit var binding: LayoutInboxSortingBinding
    var onChangeSortingType: ((sortingType: String) -> Unit)? = null
    private var lastSortingType = lastSortingTypeParam

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.layout_inbox_sorting,
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
            binding.unreadSortBtn.isClickable = true
            binding.dueDateSortBtn.isClickable = true

            setDrawableEndWithTint(binding.latestActivitySortBtn, R.drawable.icon_tick_mark, R. color.appBlue)
            removeDrawableEnd(binding.unreadSortBtn)
            removeDrawableEnd(binding.dueDateSortBtn)

        } else if (lastSortingType.equals("SortByUnread", true)) {
            binding.latestActivitySortBtn.isClickable = true
            binding.unreadSortBtn.isClickable = false
            binding.dueDateSortBtn.isClickable = true

            removeDrawableEnd(binding.latestActivitySortBtn)
            setDrawableEndWithTint(binding.unreadSortBtn, R.drawable.icon_tick_mark, R. color.appBlue)
            removeDrawableEnd(binding.dueDateSortBtn)

        } else if (lastSortingType.equals("SortByDueDate", true)) {
            binding.latestActivitySortBtn.isClickable = true
            binding.unreadSortBtn.isClickable = true
            binding.dueDateSortBtn.isClickable = false

            removeDrawableEnd(binding.latestActivitySortBtn)
            removeDrawableEnd(binding.unreadSortBtn)
            setDrawableEndWithTint(binding.dueDateSortBtn, R.drawable.icon_tick_mark, R. color.appBlue)

        } else {
            removeDrawableEnd(binding.latestActivitySortBtn)
            removeDrawableEnd(binding.unreadSortBtn)
            removeDrawableEnd(binding.dueDateSortBtn)
        }



        binding.latestActivitySortBtn.setOnClick {
            binding.latestActivitySortBtn.isClickable = false
            binding.unreadSortBtn.isClickable = true
            binding.dueDateSortBtn.isClickable = true

            lastSortingType = "SortByActivity"
            setDrawableEndWithTint(binding.latestActivitySortBtn, R.drawable.icon_tick_mark, R. color.appBlue)
            removeDrawableEnd(binding.unreadSortBtn)
            removeDrawableEnd(binding.dueDateSortBtn)
            onChangeSortingType?.invoke(lastSortingType)
        }

        binding.unreadSortBtn.setOnClick {
            binding.latestActivitySortBtn.isClickable = true
            binding.unreadSortBtn.isClickable = false
            binding.dueDateSortBtn.isClickable = true

            lastSortingType = "SortByUnread"
            removeDrawableEnd(binding.latestActivitySortBtn)
            setDrawableEndWithTint(binding.unreadSortBtn, R.drawable.icon_tick_mark, R. color.appBlue)
            removeDrawableEnd(binding.dueDateSortBtn)
            onChangeSortingType?.invoke(lastSortingType)
        }

        binding.dueDateSortBtn.setOnClick {
            binding.latestActivitySortBtn.isClickable = true
            binding.unreadSortBtn.isClickable = true
            binding.dueDateSortBtn.isClickable = false

            lastSortingType = "SortByDueDate"
            removeDrawableEnd(binding.latestActivitySortBtn)
            removeDrawableEnd(binding.unreadSortBtn)
            setDrawableEndWithTint(binding.dueDateSortBtn, R.drawable.icon_tick_mark, R. color.appBlue)
            onChangeSortingType?.invoke(lastSortingType)
        }

        binding.closeBtn.setOnClick {
            dismiss()
        }
    }

    // Function to set drawableEnd and drawableTint programmatically
    private fun setDrawableEndWithTint(textView: TextView, drawableResId: Int, tintResId: Int) {
        val drawable = ContextCompat.getDrawable(textView.context, drawableResId)
        drawable?.let {
            it.setBounds(0, 0, it.intrinsicWidth, it.intrinsicHeight)
            textView.setCompoundDrawables(null, null, it, null)
            textView.compoundDrawableTintList = ContextCompat.getColorStateList(textView.context, tintResId)
            textView.compoundDrawableTintMode = PorterDuff.Mode.SRC_IN
        }
    }

    // Function to remove drawableEnd
    fun removeDrawableEnd(textView: TextView) {
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