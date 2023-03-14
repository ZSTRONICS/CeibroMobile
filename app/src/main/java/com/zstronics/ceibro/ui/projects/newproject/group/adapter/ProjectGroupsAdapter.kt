package com.zstronics.ceibro.ui.projects.newproject.group.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.projects.group.ProjectGroup
import com.zstronics.ceibro.databinding.LayoutProjectGroupsBinding
import javax.inject.Inject

class ProjectGroupsAdapter @Inject constructor() :
    RecyclerView.Adapter<ProjectGroupsAdapter.ProjectGroupViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: ProjectGroup) -> Unit)? =
        null

    private var list: MutableList<ProjectGroup> = mutableListOf()
    var simpleChildItemClickListener: ((view: View, position: Int, data: ProjectGroup) -> Unit)? =
        null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectGroupViewHolder {
        return ProjectGroupViewHolder(
            LayoutProjectGroupsBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onBindViewHolder(holder: ProjectGroupViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<ProjectGroup>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class ProjectGroupViewHolder(private val binding: LayoutProjectGroupsBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProjectGroup) {
            binding.groupTitle.text = item.name
            binding.optionMenu.setOnClickListener {
                simpleChildItemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
        }
    }
}