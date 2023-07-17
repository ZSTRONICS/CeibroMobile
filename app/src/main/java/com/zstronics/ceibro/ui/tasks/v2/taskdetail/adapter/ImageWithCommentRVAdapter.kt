package com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter

import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.databinding.LayoutCeibroImageWithCommentBinding
import javax.inject.Inject

class ImageWithCommentRVAdapter @Inject constructor() :
    RecyclerView.Adapter<ImageWithCommentRVAdapter.ImageWithCommentViewHolder>() {
    var itemClickListener: ((view: View, position: Int) -> Unit)? =
        null
    var openImageClickListener: ((view: View, position: Int, fileUrl: String) -> Unit)? =
        null
    var listItems: MutableList<TaskFiles> = mutableListOf()
    private var selectedItemPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImageWithCommentViewHolder {
        return ImageWithCommentViewHolder(
            LayoutCeibroImageWithCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ImageWithCommentViewHolder, position: Int) {
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

    inner class ImageWithCommentViewHolder(private val binding: LayoutCeibroImageWithCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskFiles) {
            binding.smallImgView.setOnClickListener {
                openImageClickListener?.invoke(it, absoluteAdapterPosition, item.fileUrl)
            }

            val context = binding.smallImgView.context

            Glide.with(context)
                .load(item.fileUrl)
                .into(binding.smallImgView)

            if (item.hasComment) {
                binding.imgComment.movementMethod = ScrollingMovementMethod()
                binding.imgComment.text = item.comment
            }

        }
    }
}