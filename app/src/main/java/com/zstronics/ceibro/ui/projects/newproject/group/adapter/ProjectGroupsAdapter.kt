package com.zstronics.ceibro.ui.projects.newproject.group.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.extensions.gone
import com.zstronics.ceibro.base.extensions.isGone
import com.zstronics.ceibro.base.extensions.visible
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
            binding.titleTV.text = item.name
            val adapter = ProjectGroupsNestedMemberAdapter()
            adapter.setList(item.members)
            binding.memberRV.adapter = adapter

            if (item.isDefaultGroup) {
                binding.optionMenu.visibility = View.GONE
            }

            binding.optionMenu.setOnClickListener {
                simpleChildItemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            binding.root.setOnClick {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
                if (binding.expandView.isGone()) {
                    binding.expandedImageView.setImageResource(R.drawable.icon_navigate_down)
                    binding.expandView.visible()
                } else {
                    binding.expandedImageView.setImageResource(R.drawable.icon_navigate_next)
                    binding.expandView.gone()
                }
            }
        }
    }
}