package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.LayoutItemAssigneeHeaderBinding
import com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.ForwardVM
import javax.inject.Inject

class ForwardSelectionHeaderAdapter @Inject constructor() :
    RecyclerView.Adapter<ForwardSelectionHeaderAdapter.ForwardSelectionHeaderViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: AllCeibroConnections.CeibroConnection) -> Unit)? =
        null
    var listItems: MutableList<ForwardVM.ForwardConnectionGroup> = mutableListOf()
    var oldContacts: ArrayList<String> = arrayListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ForwardSelectionHeaderViewHolder {
        return ForwardSelectionHeaderViewHolder(
            LayoutItemAssigneeHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ForwardSelectionHeaderViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(
        list: List<ForwardVM.ForwardConnectionGroup>,
        oldSelectedContacts: ArrayList<String>
    ) {
        this.listItems.clear()
        this.listItems.addAll(list)
        this.oldContacts.clear()
        this.oldContacts.addAll(oldSelectedContacts)
        notifyDataSetChanged()
    }

    inner class ForwardSelectionHeaderViewHolder(private val binding: LayoutItemAssigneeHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ForwardVM.ForwardConnectionGroup) {
            binding.headerTitle.text = item.sectionLetter.toString()
            val adapter = ForwardSelectionAdapter(oldContacts)
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