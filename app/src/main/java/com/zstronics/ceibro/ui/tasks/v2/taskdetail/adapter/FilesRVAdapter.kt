package com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.data.database.models.tasks.Files
import com.zstronics.ceibro.databinding.LayoutCeibroFilesBinding
import com.zstronics.ceibro.databinding.LayoutCeibroOnlyImageBinding
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

class FilesRVAdapter @Inject constructor() :
    RecyclerView.Adapter<FilesRVAdapter.FilesViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: Files) -> Unit)? =
        null
    var listItems: MutableList<Files> = mutableListOf()

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

    fun setList(list: List<Files>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class FilesViewHolder(private val binding: LayoutCeibroFilesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Files) {
            binding.clearIcon.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }

            val context = binding.uploadImg.context

            binding.fileName.text = item.fileName
            binding.fileSize.text = item.fileTag

        }
    }
}