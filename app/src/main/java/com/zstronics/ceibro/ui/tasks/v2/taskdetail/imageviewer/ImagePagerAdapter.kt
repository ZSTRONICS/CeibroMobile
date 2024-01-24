package com.zstronics.ceibro.ui.tasks.v2.taskdetail.imageviewer

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.TaskFiles
import com.zstronics.ceibro.databinding.LayoutImageViewerBinding
import javax.inject.Inject

class ImagePagerAdapter @Inject constructor() :
    RecyclerView.Adapter<ImagePagerAdapter.ImagePagerViewHolder>() {
    var itemClickListener: ((view: View, position: Int) -> Unit)? =
        null
    var listItems: MutableList<TaskFiles> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ImagePagerViewHolder {
        return ImagePagerViewHolder(
            LayoutImageViewerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ImagePagerViewHolder, position: Int) {
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

    inner class ImagePagerViewHolder(private val binding: LayoutImageViewerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TaskFiles) {
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
            circularProgressDrawable.setColorSchemeColors(Color.BLACK)
            circularProgressDrawable.start()

            val requestOptions = RequestOptions()
                .placeholder(circularProgressDrawable)
                .error(R.drawable.icon_corrupt_file)
                .skipMemoryCache(true)
                .centerInside()

            Glide.with(context)
                .load(item.fileUrl)
                .apply(requestOptions)
                .listener(object : RequestListener<Drawable> {
                    override fun onLoadFailed(
                        e: GlideException?,
                        model: Any?,
                        target: Target<Drawable>?,
                        isFirstResource: Boolean
                    ): Boolean {
                        circularProgressDrawable.stop()
                        return false
                    }

                    override fun onResourceReady(
                        resource: Drawable?,
                        model: Any?,
                        target: Target<Drawable>?,
                        dataSource: DataSource?,
                        isFirstResource: Boolean
                    ): Boolean {
                        circularProgressDrawable.stop()
                        return false
                    }
                })
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.fullImgView)
        }
    }
}