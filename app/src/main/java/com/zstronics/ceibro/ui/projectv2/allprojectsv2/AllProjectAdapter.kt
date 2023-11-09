package com.zstronics.ceibro.ui.projectv2.allprojectsv2


import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.chat.room.Project
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutProjectItemListBinding
import javax.inject.Inject

class AllProjectAdapter @Inject constructor(val sessionManager: SessionManager) :
    RecyclerView.Adapter<AllProjectAdapter.AllProjectViewHolder>() {

    private var list: MutableList<Project> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllProjectViewHolder {
        return AllProjectViewHolder(
            LayoutProjectItemListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllProjectViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: MutableList<Project>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllProjectViewHolder(private val binding: LayoutProjectItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Project) {
            val context = binding.root.context

            binding.tvDate.text = "23.09.2023"
            binding.projectName.text = item.title
            binding.userCompany.text = "Ceibro limiteds"
            binding.userName.text = "Rebel"

        }
    }
}