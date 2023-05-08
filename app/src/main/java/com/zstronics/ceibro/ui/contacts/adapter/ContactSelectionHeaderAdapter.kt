package com.zstronics.ceibro.ui.contacts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.databinding.LayoutItemConnectionHeaderBinding
import com.zstronics.ceibro.ui.contacts.ContactsSelectionVM
import javax.inject.Inject

class ContactSelectionHeaderAdapter @Inject constructor() :
    RecyclerView.Adapter<ContactSelectionHeaderAdapter.ContactSelectionHeaderViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: SyncContactsRequest.CeibroContactLight) -> Unit)? =
        null
    var listItems: MutableList<ContactsSelectionVM.ContactSelectionGroup> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ContactSelectionHeaderViewHolder {
        return ContactSelectionHeaderViewHolder(
            LayoutItemConnectionHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ContactSelectionHeaderViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<ContactsSelectionVM.ContactSelectionGroup>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class ContactSelectionHeaderViewHolder(private val binding: LayoutItemConnectionHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ContactsSelectionVM.ContactSelectionGroup) {
            binding.headerTitle.text = item.sectionLetter.toString()
            val adapter = ContactsSelectionAdapter()
            adapter.setList(item.items)
            binding.connectionRV.adapter = adapter
            adapter.itemClickListener =
                { it: View, position: Int, data: SyncContactsRequest.CeibroContactLight ->
                    itemClickListener?.invoke(it, absoluteAdapterPosition, data)
                    val oldItem = listItems
                    oldItem[absoluteAdapterPosition].items = adapter.dataList
                }
        }
    }
}