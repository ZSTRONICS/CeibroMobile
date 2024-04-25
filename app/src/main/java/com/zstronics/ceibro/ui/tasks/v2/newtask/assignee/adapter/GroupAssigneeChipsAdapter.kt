package com.zstronics.ceibro.ui.tasks.v2.newtask.assignee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.TaskMemberDetail
import com.zstronics.ceibro.databinding.LayoutItemAssigneeChipBinding
import javax.inject.Inject

class GroupAssigneeChipsAdapter @Inject constructor() :
    RecyclerView.Adapter<GroupAssigneeChipsAdapter.AssigneeChipsViewHolder>() {
    var removeItemClickListener: ((view: View, position: Int, data: TaskMemberDetail) -> Unit)? =
        null
    var dataList: MutableList<TaskMemberDetail> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssigneeChipsViewHolder {
        return AssigneeChipsViewHolder(
            LayoutItemAssigneeChipBinding.inflate(
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

    fun setList(list: List<TaskMemberDetail>) {
        this.dataList.clear()
        this.dataList.addAll(list)
        notifyDataSetChanged()
    }

    inner class AssigneeChipsViewHolder(private val binding: LayoutItemAssigneeChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskMemberDetail) {

            binding.removeBtn.setOnClickListener {
                val oldList = dataList
                oldList.removeAt(absoluteAdapterPosition)
                dataList = oldList
                notifyDataSetChanged()
                removeItemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }


            if (item.profilePic.isNullOrEmpty()) {
                binding.contactInitials.visibility = View.VISIBLE
                binding.contactImage.visibility = View.GONE
                var initials = ""
                if (item.firstName.isNotEmpty() == true) {
                    initials += item.firstName[0].uppercaseChar()
                }
                if (item.surName.isNotEmpty() == true) {
                    initials += item.surName[0].uppercaseChar()
                }

                binding.contactInitials.text = initials
            } else {
                binding.contactInitials.visibility = View.GONE
                binding.contactImage.visibility = View.VISIBLE

                Glide.with(binding.contactImage.context)
                    .load(item.profilePic)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.profile_img)
                    .into(binding.contactImage)
            }
        }
    }
}