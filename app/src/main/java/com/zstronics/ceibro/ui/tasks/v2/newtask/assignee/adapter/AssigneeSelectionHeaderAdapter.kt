package com.zstronics.ceibro.ui.tasks.v2.newtask.assignee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.databinding.LayoutItemAssigneeHeaderBinding
import com.zstronics.ceibro.databinding.LayoutItemConnectionHeaderBinding
import com.zstronics.ceibro.ui.contacts.ContactsSelectionVM
import com.zstronics.ceibro.ui.tasks.v2.newtask.assignee.AssigneeVM
import javax.inject.Inject

class AssigneeSelectionHeaderAdapter @Inject constructor() :
    RecyclerView.Adapter<AssigneeSelectionHeaderAdapter.AssigneeSelectionHeaderViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: AllCeibroConnections.CeibroConnection) -> Unit)? =
        null
    var listItems: MutableList<AssigneeVM.AssigneeConnectionGroup> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AssigneeSelectionHeaderViewHolder {
        return AssigneeSelectionHeaderViewHolder(
            LayoutItemAssigneeHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AssigneeSelectionHeaderViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<AssigneeVM.AssigneeConnectionGroup>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class AssigneeSelectionHeaderViewHolder(private val binding: LayoutItemAssigneeHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AssigneeVM.AssigneeConnectionGroup) {
            binding.headerTitle.text = item.sectionLetter.toString()
            val adapter = AssigneeSelectionAdapter()
            adapter.setList(item.items)
            binding.connectionRV.adapter = adapter
            adapter.itemClickListener =
                { it: View, position: Int, data: AllCeibroConnections.CeibroConnection ->
                    itemClickListener?.invoke(it, absoluteAdapterPosition, data)
                    val oldItem = listItems
                    oldItem[absoluteAdapterPosition].items = adapter.dataList
                }
        }
    }
}