package com.zstronics.ceibro.ui.tasks.v2.newtask.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.databinding.LayoutCeibroFilesBinding
import com.zstronics.ceibro.databinding.LayoutCeibroOnlyImageBinding
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

class CeibroFilesRVAdapter @Inject constructor() :
    RecyclerView.Adapter<CeibroFilesRVAdapter.CeibroFilesViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: PickedImages) -> Unit)? =
        null
    var listItems: MutableList<PickedImages> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CeibroFilesViewHolder {
        return CeibroFilesViewHolder(
            LayoutCeibroFilesBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CeibroFilesViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<PickedImages>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class CeibroFilesViewHolder(private val binding: LayoutCeibroFilesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PickedImages) {
            binding.clearIcon.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }

            val context = binding.uploadImg.context

            binding.fileName.text = item.fileName
            binding.fileSize.text = item.fileSizeReadAble

        }
    }
}