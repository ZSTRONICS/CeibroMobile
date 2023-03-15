package com.zstronics.ceibro.ui.projects.newproject.documents.adapter

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
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.data.repos.projects.documents.CreateProjectFolderResponse
import com.zstronics.ceibro.databinding.LayoutProjectFolderBinding
import javax.inject.Inject

class ProjectFoldersAdapter @Inject constructor() :
    RecyclerView.Adapter<ProjectFoldersAdapter.ProjectFoldersViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: CreateProjectFolderResponse.ProjectFolder) -> Unit)? =
        null
    var onFolderExpand: ((view: View, position: Int, data: CreateProjectFolderResponse.ProjectFolder) -> Unit)? =
        null

    private var list: MutableList<CreateProjectFolderResponse.ProjectFolder> = mutableListOf()
    var simpleChildItemClickListener: ((view: View, position: Int, data: CreateProjectFolderResponse.ProjectFolder) -> Unit)? =
        null
    var shoFileMenu: ((view: View, position: Int, data: FilesAttachments) -> Unit)? =
        null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProjectFoldersViewHolder {
        return ProjectFoldersViewHolder(
            LayoutProjectFolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onBindViewHolder(holder: ProjectFoldersViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<CreateProjectFolderResponse.ProjectFolder>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class ProjectFoldersViewHolder(private val binding: LayoutProjectFolderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CreateProjectFolderResponse.ProjectFolder) {
            binding.titleTV.text = item.name

            val filesAdapter = ProjectFilesNestedAdapter()
            filesAdapter.simpleChildItemClickListener =
                { childView: View, position: Int, data: FilesAttachments ->
                    shoFileMenu?.invoke(childView, position, data)
                }

            item.files?.let { filesAdapter.setList(it) }
            binding.nestedFilesRV.adapter = filesAdapter

            binding.optionMenu.setOnClickListener {
                simpleChildItemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            binding.root.setOnClick {
                onFolderExpand?.invoke(it, absoluteAdapterPosition, item)
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