package com.zstronics.ceibro.ui.tasks.newsubtask

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.databinding.LayoutAttachmentBinding
import javax.inject.Inject

class AttachmentAdapter @Inject constructor() :
    RecyclerView.Adapter<AttachmentAdapter.AttachmentViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: Uri?) -> Unit)? = null

    private var list: MutableList<Uri?> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AttachmentViewHolder {
        return AttachmentViewHolder(
            LayoutAttachmentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AttachmentViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: ArrayList<Uri?>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class AttachmentViewHolder(private val binding: LayoutAttachmentBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Uri?) {
            binding.crossView.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            // Load the thumbnail into the ImageView using Glide
            Glide.with(binding.attachmentImageView.context)
                .load(item)
                .thumbnail(0.1f)
                .centerCrop()
                .into(binding.attachmentImageView)
        }
    }
}