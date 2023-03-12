package com.zstronics.ceibro.ui.projects.newproject.members.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.projects.member.GetProjectMemberResponse
import com.zstronics.ceibro.databinding.LayoutProjectMemberBinding
import javax.inject.Inject

class ProjectMembersAdapter @Inject constructor() :
    RecyclerView.Adapter<ProjectMembersAdapter.ProjectMembersViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: GetProjectMemberResponse.ProjectMember) -> Unit)? =
        null

    private var list: MutableList<GetProjectMemberResponse.ProjectMember> = mutableListOf()
    var simpleChildItemClickListener: ((view: View, position: Int, data: GetProjectMemberResponse.ProjectMember) -> Unit)? =
        null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectMembersViewHolder {
        return ProjectMembersViewHolder(
            LayoutProjectMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onBindViewHolder(holder: ProjectMembersViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<GetProjectMemberResponse.ProjectMember>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class ProjectMembersViewHolder(private val binding: LayoutProjectMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: GetProjectMemberResponse.ProjectMember) {
            binding.memberName.text = "${item.user?.firstName} ${item.user?.surName}"
            binding.optionMenu.setOnClickListener {
                simpleChildItemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
        }
    }
}