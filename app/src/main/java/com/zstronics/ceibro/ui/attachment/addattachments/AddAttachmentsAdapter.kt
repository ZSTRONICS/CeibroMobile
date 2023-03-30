package com.zstronics.ceibro.ui.attachment.addattachments

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.base.extensions.gone
import com.zstronics.ceibro.base.extensions.visible
import com.zstronics.ceibro.databinding.LayoutAddAttachmentBinding
import com.zstronics.ceibro.ui.attachment.AttachmentTypes
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment
import javax.inject.Inject

class AddAttachmentsAdapter @Inject constructor() :
    RecyclerView.Adapter<AddAttachmentsAdapter.AddAttachmentsViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: SubtaskAttachment?) -> Unit)? =
        null
    var onEditPhoto: ((view: View, position: Int, data: SubtaskAttachment?) -> Unit)? =
        null
    private var list: MutableList<SubtaskAttachment?> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AddAttachmentsViewHolder {
        return AddAttachmentsViewHolder(
            LayoutAddAttachmentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onBindViewHolder(holder: AddAttachmentsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: ArrayList<SubtaskAttachment?>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class AddAttachmentsViewHolder(private val binding: LayoutAddAttachmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: SubtaskAttachment?) {
            if (item?.attachmentType == AttachmentTypes.Image) {
                binding.editLayout.visible()
                binding.editLayout.setOnClickListener {
                    onEditPhoto?.invoke(it, absoluteAdapterPosition, item)
                }
            } else {
                binding.editLayout.gone()
            }
            binding.crossView.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            binding.crossView.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            binding.attachmentTitle.text = item?.fileName
            binding.attachmentSize.text = item?.fileSizeReadAble
        }
    }
}