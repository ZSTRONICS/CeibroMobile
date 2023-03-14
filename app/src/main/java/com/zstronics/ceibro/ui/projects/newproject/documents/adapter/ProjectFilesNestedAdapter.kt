package com.zstronics.ceibro.ui.projects.newproject.documents.adapter

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.databinding.LayoutProjectFileBinding
import javax.inject.Inject

class ProjectFilesNestedAdapter @Inject constructor() :
    RecyclerView.Adapter<ProjectFilesNestedAdapter.ProjectFilesNestedViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: FilesAttachments) -> Unit)? =
        null

    private var list: MutableList<FilesAttachments> = mutableListOf()
    var simpleChildItemClickListener: ((view: View, position: Int, data: FilesAttachments) -> Unit)? =
        null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProjectFilesNestedViewHolder {
        return ProjectFilesNestedViewHolder(
            LayoutProjectFileBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onBindViewHolder(holder: ProjectFilesNestedViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<FilesAttachments>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class ProjectFilesNestedViewHolder(private val binding: LayoutProjectFileBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: FilesAttachments) {
            binding.fileNameTV.text = item.fileName
        }
    }
}