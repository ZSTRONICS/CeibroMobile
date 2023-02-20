package com.zstronics.ceibro.ui.attachment

import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.databinding.LayoutAttachmentBinding
import javax.inject.Inject

class AttachmentViewAdapter @Inject constructor() :
    RecyclerView.Adapter<AttachmentViewAdapter.AttachmentViewViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: FilesAttachments?) -> Unit)? =
        null

    private var list: MutableList<FilesAttachments?> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewViewHolder {
        return AttachmentViewViewHolder(
            LayoutAttachmentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onBindViewHolder(holder: AttachmentViewViewHolder, position: Int) {
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

    inner class AttachmentViewViewHolder(private val binding: LayoutAttachmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O_MR1)
        fun bind(item: FilesAttachments?) {
            binding.crossView.visibility = View.GONE
            // Load the thumbnail into the ImageView using Glide
            when {
                imageExtensions.contains(item?.fileType) -> {
                    binding.playButton.visibility = View.GONE
                    Glide.with(binding.attachmentImageView.context)
                        .load(item?.fileUrl)
                        .thumbnail(0.1f)
                        .centerCrop()
                        .placeholder(R.drawable.app_logo)
                        .into(binding.attachmentImageView)
                }
                videoExtensions.contains(item?.fileType) -> {
                    binding.playButton.visibility = View.VISIBLE
                    Glide.with(binding.attachmentImageView.context)
                        .load(item?.fileUrl)
                        .thumbnail(0.1f)
                        .centerCrop()
                        .into(binding.attachmentImageView)
                }
                item?.fileType == ".pdf" -> {
                    binding.playButton.visibility = View.GONE
                    Glide.with(binding.attachmentImageView.context)
                        .load(R.drawable.icon_pdf)
                        .into(binding.attachmentImageView)
                }
                documentExtensions.contains(item?.fileType) -> {
                    binding.playButton.visibility = View.GONE
                    Glide.with(binding.attachmentImageView.context)
                        .load(R.drawable.icon_doc)
                        .centerCrop()
                        .into(binding.attachmentImageView)
                }
            }
        }
    }
}