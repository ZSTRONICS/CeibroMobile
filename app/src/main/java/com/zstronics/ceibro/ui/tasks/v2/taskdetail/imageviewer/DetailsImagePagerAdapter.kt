package com.zstronics.ceibro.ui.tasks.v2.taskdetail.imageviewer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.LocalTaskDetailFiles
import com.zstronics.ceibro.databinding.LayoutImageViewerBinding
import javax.inject.Inject

class DetailsImagePagerAdapter @Inject constructor() :
    RecyclerView.Adapter<DetailsImagePagerAdapter.LocalImagePagerViewHolder>() {
    var itemClickListener: ((view: View, position: Int) -> Unit)? =
        null
    var listItems: MutableList<LocalTaskDetailFiles> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): LocalImagePagerViewHolder {
        return LocalImagePagerViewHolder(
            LayoutImageViewerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: LocalImagePagerViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: MutableList<LocalTaskDetailFiles>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class LocalImagePagerViewHolder(private val binding: LayoutImageViewerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: LocalTaskDetailFiles) {
//            binding.root.setOnClickListener {
//                itemClickListener?.invoke(it, adapterPosition)
//            }
            val context = binding.fullImgView.context

            if (item.comment.isNotEmpty()) {
                binding.imageComment.text = item.comment
                binding.imageComment.visibility = View.VISIBLE
            } else {
                binding.imageComment.text = ""
                binding.imageComment.visibility = View.GONE
            }

            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.setColorSchemeColors(Color.WHITE)
            circularProgressDrawable.start()

            val requestOptions = RequestOptions()
                .placeholder(circularProgressDrawable)
                .error(R.drawable.icon_corrupt_file)
                .skipMemoryCache(true)
                .centerInside()

            Glide.with(context)
                .load(item.fileUrl)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.fullImgView)


        }
    }
}