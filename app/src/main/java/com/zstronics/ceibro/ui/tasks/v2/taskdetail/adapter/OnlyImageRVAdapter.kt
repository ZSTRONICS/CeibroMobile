package com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.databinding.LayoutCeibroOnlyImageBinding
import javax.inject.Inject

class OnlyImageRVAdapter @Inject constructor() :
    RecyclerView.Adapter<OnlyImageRVAdapter.OnlyImageViewHolder>() {
    var openImageClickListener: ((view: View, position: Int, fileUrl: String) -> Unit)? =
        null
    var listItems: MutableList<TaskFiles> = mutableListOf()
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

    fun setList(list: List<TaskFiles>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class OnlyImageViewHolder(private val binding: LayoutCeibroOnlyImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskFiles) {
            binding.root.setOnClickListener {
                openImageClickListener?.invoke(it, absoluteAdapterPosition, item.fileUrl)
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