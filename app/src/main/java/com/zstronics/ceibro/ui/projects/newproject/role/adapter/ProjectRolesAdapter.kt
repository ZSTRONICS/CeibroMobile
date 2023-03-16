package com.zstronics.ceibro.ui.projects.newproject.role.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.extensions.*
import com.zstronics.ceibro.data.repos.projects.role.ProjectRolesResponse
import com.zstronics.ceibro.databinding.LayoutProjectRoleBinding
import javax.inject.Inject

class ProjectRolesAdapter @Inject constructor() :
    RecyclerView.Adapter<ProjectRolesAdapter.ProjectRolesViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: ProjectRolesResponse.ProjectRole) -> Unit)? =
        null

    private var list: MutableList<ProjectRolesResponse.ProjectRole> = mutableListOf()
    var simpleChildItemClickListener: ((view: View, position: Int, data: ProjectRolesResponse.ProjectRole) -> Unit)? =
        null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectRolesViewHolder {
        return ProjectRolesViewHolder(
            LayoutProjectRoleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onBindViewHolder(holder: ProjectRolesViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<ProjectRolesResponse.ProjectRole>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class ProjectRolesViewHolder(private val binding: LayoutProjectRoleBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProjectRolesResponse.ProjectRole) {
            binding.role = item

            if (item.isDefaultRole) {
                binding.optionMenu.visibility = View.GONE
            }

            binding.optionMenu.setOnClick {
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