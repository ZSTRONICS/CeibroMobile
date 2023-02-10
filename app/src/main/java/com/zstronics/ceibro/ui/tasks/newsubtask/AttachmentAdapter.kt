package com.zstronics.ceibro.ui.tasks.newsubtask

import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.LayoutAttachmentBinding
import com.zstronics.ceibro.ui.attachment.AttachmentTypes
import javax.inject.Inject

class AttachmentAdapter @Inject constructor() :
    RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: NewSubTaskVM.SubtaskAttachment?) -> Unit)? =
        null

    private var list: MutableList<NewSubTaskVM.SubtaskAttachment?> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        return AttachmentViewHolder(
            LayoutAttachmentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    @RequiresApi(Build.VERSION_CODES.O_MR1)
    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: ArrayList<NewSubTaskVM.SubtaskAttachment?>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class AttachmentViewHolder(private val binding: LayoutAttachmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        @RequiresApi(Build.VERSION_CODES.O_MR1)
        fun bind(item: NewSubTaskVM.SubtaskAttachment?) {
            binding.crossView.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            // Load the thumbnail into the ImageView using Glide
            when (item?.attachmentType) {
                AttachmentTypes.Image -> {
                    binding.playButton.visibility = View.GONE
                    Glide.with(binding.attachmentImageView.context)
                        .load(item.attachmentUri)
                        .thumbnail(0.1f)
                        .centerCrop()
                        .into(binding.attachmentImageView)
                }
                AttachmentTypes.Video -> {
                    binding.playButton.visibility = View.VISIBLE
                    val mmr = MediaMetadataRetriever()
                    mmr.setDataSource(binding.attachmentImageView.context, item.attachmentUri)
                    val bm: Bitmap? = mmr.getScaledFrameAtTime(
                        1000,
                        MediaMetadataRetriever.OPTION_NEXT_SYNC,
                        128,
                        128
                    )

                    Glide.with(binding.attachmentImageView.context)
                        .load(bm)
                        .thumbnail(0.1f)
                        .centerCrop()
                        .into(binding.attachmentImageView)
                }
                AttachmentTypes.Doc -> {
                    binding.playButton.visibility = View.GONE
                    Glide.with(binding.attachmentImageView.context)
                        .load(R.drawable.ic_doc)
                        .thumbnail(0.1f)
                        .centerCrop()
                        .into(binding.attachmentImageView)
                }
                AttachmentTypes.Pdf -> {
                    binding.playButton.visibility = View.GONE
                    Glide.with(binding.attachmentImageView.context)
                        .load(R.drawable.ic_pdf)
                        .thumbnail(0.1f)
                        .centerCrop()
                        .into(binding.attachmentImageView)
                }
            }
        }
    }
}