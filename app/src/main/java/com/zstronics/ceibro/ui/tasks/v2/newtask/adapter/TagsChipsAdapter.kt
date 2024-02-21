package com.zstronics.ceibro.ui.tasks.v2.newtask.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.LayoutItemAssigneeChipBinding
import com.zstronics.ceibro.databinding.LayoutItemTagChipBindingBinding
import javax.inject.Inject

class TagsChipsAdapter @Inject constructor() :
    RecyclerView.Adapter<TagsChipsAdapter.AssigneeChipsViewHolder>() {
    var removeItemClickListener: ((view: View, position: Int, data: AllCeibroConnections.CeibroConnection) -> Unit)? =
        null
    var dataList: MutableList<AllCeibroConnections.CeibroConnection> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssigneeChipsViewHolder {
        return AssigneeChipsViewHolder(
            LayoutItemTagChipBindingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AssigneeChipsViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setList(list: List<AllCeibroConnections.CeibroConnection>) {
        this.dataList.clear()
        this.dataList.addAll(list)
        notifyDataSetChanged()
    }

    inner class AssigneeChipsViewHolder(private val binding: LayoutItemTagChipBindingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllCeibroConnections.CeibroConnection) {

            binding.removeBtn.setOnClickListener {
                val oldList = dataList
                oldList.removeAt(absoluteAdapterPosition)
                dataList = oldList
                notifyDataSetChanged()
                removeItemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            if (item.userCeibroData?.profilePic.isNullOrEmpty()) {
                binding.contactInitials.visibility = View.VISIBLE
                var initials = ""
                if (item.contactFirstName?.isNotEmpty() == true) {
                    initials += item.contactFirstName[0].uppercaseChar()
                }
                if (item.contactSurName?.isNotEmpty() == true) {
                    initials += item.contactSurName[0].uppercaseChar()
                }

                binding.contactInitials.text = initials
            }
        }
    }
}