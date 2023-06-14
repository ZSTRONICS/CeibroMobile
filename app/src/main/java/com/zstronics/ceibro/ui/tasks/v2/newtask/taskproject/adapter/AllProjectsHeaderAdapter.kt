package com.zstronics.ceibro.ui.tasks.v2.newtask.taskproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponseV2
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.LayoutItemConnectionHeaderBinding
import com.zstronics.ceibro.databinding.LayoutItemTopicHeaderBinding
import com.zstronics.ceibro.ui.tasks.v2.newtask.taskproject.TaskProjectVM
import javax.inject.Inject

class AllProjectsHeaderAdapter @Inject constructor() :
    RecyclerView.Adapter<AllProjectsHeaderAdapter.AllProjectsHeaderViewHolder>() {
    var allProjectItemClickListener: ((view: View, position: Int, data: AllProjectsResponseV2.ProjectsV2) -> Unit)? =
        null
    var listItems: MutableList<TaskProjectVM.CeibroProjectGroup> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllProjectsHeaderViewHolder {
        return AllProjectsHeaderViewHolder(
            LayoutItemTopicHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllProjectsHeaderViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<TaskProjectVM.CeibroProjectGroup>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllProjectsHeaderViewHolder(private val binding: LayoutItemTopicHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TaskProjectVM.CeibroProjectGroup) {

            binding.headerTitle.text = item.sectionLetter.toString()
            val adapter = AllProjectsAdapter()
            adapter.setList(item.items)
            binding.topicRV.adapter = adapter
            adapter.itemClickListener =
                { it: View, position: Int, data: AllProjectsResponseV2.ProjectsV2 ->
                    allProjectItemClickListener?.invoke(it, absoluteAdapterPosition, data)
                }
        }
    }
}