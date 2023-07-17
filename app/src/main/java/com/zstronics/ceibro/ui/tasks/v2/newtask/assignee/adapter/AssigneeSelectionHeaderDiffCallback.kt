package com.zstronics.ceibro.ui.tasks.v2.newtask.assignee.adapter

import androidx.recyclerview.widget.DiffUtil
import com.zstronics.ceibro.ui.tasks.v2.newtask.assignee.AssigneeVM

class AssigneeSelectionHeaderDiffCallback(
    private val oldList: MutableList<AssigneeVM.AssigneeConnectionGroup>,
    private val newList: List<AssigneeVM.AssigneeConnectionGroup>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].sectionLetter == newList[newItemPosition].sectionLetter
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldItems = oldList[oldItemPosition].items
        val newItems = newList[newItemPosition].items

        return oldItems == newItems
    }
}
