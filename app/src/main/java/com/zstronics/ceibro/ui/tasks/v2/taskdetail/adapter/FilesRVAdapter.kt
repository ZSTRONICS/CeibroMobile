package com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.databinding.LayoutCeibroFilesBinding
import javax.inject.Inject

class FilesRVAdapter @Inject constructor() :
    RecyclerView.Adapter<FilesRVAdapter.FilesViewHolder>() {
    var fileClickListener: ((view: View, position: Int, data: TaskFiles) -> Unit)? =
        null
    var listItems: MutableList<TaskFiles> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): FilesViewHolder {
        return FilesViewHolder(
            LayoutCeibroFilesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: FilesViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<TaskFiles>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class FilesViewHolder(private val binding: LayoutCeibroFilesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskFiles) {
            binding.root.setOnClickListener {
                fileClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            binding.mainLayout.setOnClickListener {
                fileClickListener?.invoke(it, absoluteAdapterPosition, item)
            }

            binding.fileName.setOnClickListener {
                fileClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            binding.fileSize.setOnClickListener {
                fileClickListener?.invoke(it, absoluteAdapterPosition, item)
            }

            val context = binding.uploadImg.context

            binding.fileName.text = item.fileName
            binding.fileSize.text = "File size: unknown"
            binding.clearIcon.visibility = View.GONE

        }
    }
}