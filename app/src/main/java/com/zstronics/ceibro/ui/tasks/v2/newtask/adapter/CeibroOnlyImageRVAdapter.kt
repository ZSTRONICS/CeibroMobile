package com.zstronics.ceibro.ui.tasks.v2.newtask.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.databinding.LayoutCeibroOnlyImageBinding
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

class CeibroOnlyImageRVAdapter @Inject constructor() :
    RecyclerView.Adapter<CeibroOnlyImageRVAdapter.CeibroOnlyImageViewHolder>() {
    var itemClickListener: ((view: View, position: Int) -> Unit)? =
        null
    var listItems: MutableList<PickedImages> = mutableListOf()
    private var selectedItemPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CeibroOnlyImageViewHolder {
        return CeibroOnlyImageViewHolder(
            LayoutCeibroOnlyImageBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CeibroOnlyImageViewHolder, position: Int) {
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

    inner class CeibroOnlyImageViewHolder(private val binding: LayoutCeibroOnlyImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PickedImages) {
            binding.root.setOnClickListener {

            }

            val context = binding.smallImgView.context

            Glide.with(context)
                .load(item.fileUri)
                .into(binding.smallImgView)


//            if (adapterPosition == selectedItemPosition) {
//                binding.parentCard.background = context.resources.getDrawable(R.drawable.card_outline)
//            } else {
//                binding.parentCard.background = null
//            }

        }
    }
}