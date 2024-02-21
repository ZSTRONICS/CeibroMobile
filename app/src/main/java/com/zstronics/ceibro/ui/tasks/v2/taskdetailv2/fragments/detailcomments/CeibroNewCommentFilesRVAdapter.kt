package com.zstronics.ceibro.ui.tasks.v2.taskdetailv2.fragments.detailcomments

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.LayoutCeibroFilesBinding
import com.zstronics.ceibro.databinding.LayoutCeibroOnlyImageBinding
import com.zstronics.ceibro.databinding.LayoutNewCommentFilesBinding
import ee.zstronics.ceibro.camera.AttachmentTypes
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

class CeibroNewCommentFilesRVAdapter @Inject constructor() :
    RecyclerView.Adapter<CeibroNewCommentFilesRVAdapter.CeibroFilesViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: PickedImages) -> Unit)? =
        null
    var listItems: MutableList<PickedImages> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CeibroFilesViewHolder {
        return CeibroFilesViewHolder(
            LayoutNewCommentFilesBinding.inflate(
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

    inner class CeibroFilesViewHolder(private val binding: LayoutNewCommentFilesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PickedImages) {
            binding.clearIcon.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }

            val context = binding.uploadImg.context

            binding.fileName.text = item.fileName
            binding.fileSize.text = item.fileSizeReadAble


            val fileType = item.attachmentType

            if (fileType == AttachmentTypes.Pdf) {
                binding.uploadImg.setImageResource(R.drawable.icon_pdf)
//                binding.uploadImg.scaleType = ImageView.ScaleType.FIT_CENTER
            } else if (fileType == AttachmentTypes.Doc) {
                binding.uploadImg.setImageResource(R.drawable.icon_doc)
//                binding.uploadImg.scaleType = ImageView.ScaleType.FIT_CENTER
            } else {
                binding.uploadImg.setImageResource(R.drawable.icon_document)
//                binding.uploadImg.scaleType = ImageView.ScaleType.FIT_CENTER
            }
        }
    }
}