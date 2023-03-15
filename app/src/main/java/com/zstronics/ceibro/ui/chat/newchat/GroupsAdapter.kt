package com.zstronics.ceibro.ui.chat.newchat

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.databinding.LayoutItemGroupBinding
import javax.inject.Inject

class GroupsAdapter @Inject constructor() : RecyclerView.Adapter<GroupsAdapter.GroupsViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: ProjectsWithMembersResponse.ProjectDetail.Group) -> Unit)? =
        null

    var dataList: ArrayList<ProjectsWithMembersResponse.ProjectDetail.Group> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupsViewHolder {
        return GroupsViewHolder(
            LayoutItemGroupBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: GroupsViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setList(list: List<ProjectsWithMembersResponse.ProjectDetail.Group>) {
        this.dataList.clear()
        this.dataList.addAll(list)
        notifyDataSetChanged()
    }

    inner class GroupsViewHolder(private val binding: LayoutItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: ProjectsWithMembersResponse.ProjectDetail.Group) {
            binding.group = item
            binding.appCompatCheckBox.isChecked = item.isChecked
            binding.root.setOnClickListener {
                item.isChecked = !item.isChecked
                dataList[absoluteAdapterPosition].isChecked = item.isChecked
                notifyItemChanged(absoluteAdapterPosition)
            }
        }
    }
}