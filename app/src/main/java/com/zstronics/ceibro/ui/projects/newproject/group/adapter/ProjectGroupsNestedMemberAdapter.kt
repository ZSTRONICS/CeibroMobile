package com.zstronics.ceibro.ui.projects.newproject.group.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.databinding.LayoutProjectGroupNestedMemberBinding
import javax.inject.Inject

class ProjectGroupsNestedMemberAdapter @Inject constructor() :
    RecyclerView.Adapter<ProjectGroupsNestedMemberAdapter.ProjectGroupsNestedMemberViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: Member) -> Unit)? =
        null

    private var list: MutableList<Member> = mutableListOf()
    var simpleChildItemClickListener: ((view: View, position: Int, data: Member) -> Unit)? =
        null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProjectGroupsNestedMemberViewHolder {
        return ProjectGroupsNestedMemberViewHolder(
            LayoutProjectGroupNestedMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onBindViewHolder(holder: ProjectGroupsNestedMemberViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<Member>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class ProjectGroupsNestedMemberViewHolder(private val binding: LayoutProjectGroupNestedMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Member) {
            binding.memberName.text = "${item.firstName} ${item.surName}"
        }
    }
}