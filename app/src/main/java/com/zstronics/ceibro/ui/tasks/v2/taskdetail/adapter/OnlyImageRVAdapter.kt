package com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.data.database.models.tasks.Files
import com.zstronics.ceibro.databinding.LayoutCeibroOnlyImageBinding
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

class OnlyImageRVAdapter @Inject constructor() :
    RecyclerView.Adapter<OnlyImageRVAdapter.OnlyImageViewHolder>() {
    var itemClickListener: ((view: View, position: Int) -> Unit)? =
        null
    var listItems: MutableList<Files> = mutableListOf()
    private var selectedItemPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): OnlyImageViewHolder {
        return OnlyImageViewHolder(
            LayoutCeibroOnlyImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OnlyImageViewHolder, position: Int) {
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

    inner class OnlyImageViewHolder(private val binding: LayoutCeibroOnlyImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Files) {
            binding.root.setOnClickListener {

            }

            val context = binding.smallImgView.context

            Glide.with(context)
                .load(item.fileUrl)
                .into(binding.smallImgView)


//            if (adapterPosition == selectedItemPosition) {
//                binding.parentCard.background = context.resources.getDrawable(R.drawable.card_outline)
//            } else {
//                binding.parentCard.background = null
//            }

        }
    }
}