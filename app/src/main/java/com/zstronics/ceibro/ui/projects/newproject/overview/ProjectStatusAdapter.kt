package com.zstronics.ceibro.ui.projects.newproject.overview

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.databinding.LayoutProjectStatusBinding
import javax.inject.Inject

class ProjectStatusAdapter @Inject constructor() :
    RecyclerView.Adapter<ProjectStatusAdapter.ProjectStatusViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: ProjectOverviewVM.ProjectStatus) -> Unit)? =
        null

    private var list: MutableList<ProjectOverviewVM.ProjectStatus> = mutableListOf()
    var simpleChildItemClickListener: ((view: View, position: Int, data: ProjectOverviewVM.ProjectStatus) -> Unit)? =
        null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectStatusViewHolder {
        return ProjectStatusViewHolder(
            LayoutProjectStatusBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onBindViewHolder(holder: ProjectStatusViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<ProjectOverviewVM.ProjectStatus>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class ProjectStatusViewHolder(private val binding: LayoutProjectStatusBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProjectOverviewVM.ProjectStatus) {
            binding.statusTitle.text = item.status
            binding.optionMenu.setOnClickListener {
                simpleChildItemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
        }
    }
}