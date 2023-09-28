package com.zstronics.ceibro.ui.tasks.v2.newtask.adapter

import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.databinding.LayoutCeibroImageWithCommentBinding
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

class CeibroImageWithCommentRVAdapter @Inject constructor() :
    RecyclerView.Adapter<CeibroImageWithCommentRVAdapter.CeibroImageWithCommentViewHolder>() {
    var openImageClickListener: ((view: View, position: Int, data: PickedImages) -> Unit)? =
        null
    var textClickListener: ((view: View, position: Int, data: PickedImages) -> Unit)? =
        null
    var listItems: MutableList<PickedImages> = mutableListOf()
    private var selectedItemPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CeibroImageWithCommentViewHolder {
        return CeibroImageWithCommentViewHolder(
            LayoutCeibroImageWithCommentBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CeibroImageWithCommentViewHolder, position: Int) {
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

    inner class CeibroImageWithCommentViewHolder(private val binding: LayoutCeibroImageWithCommentBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PickedImages) {
            binding.imgComment.setOnClickListener {
                textClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            binding.smallImgView.setOnClickListener {
                openImageClickListener?.invoke(it, absoluteAdapterPosition, item)
            }

            val context = binding.smallImgView.context

            Glide.with(context)
                .load(item.fileUri)
                .into(binding.smallImgView)

            binding.imgComment.movementMethod = ScrollingMovementMethod()
            binding.imgComment.text = item.comment

//            if (adapterPosition == selectedItemPosition) {
//                binding.parentCard.background = context.resources.getDrawable(R.drawable.card_outline)
//            } else {
//                binding.parentCard.background = null
//            }

        }
    }
}