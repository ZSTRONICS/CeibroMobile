package com.zstronics.ceibro.ui.tasks.v2.taskdetail.imageviewer

import android.graphics.Color
import android.graphics.drawable.Drawable
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
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
    var currentVisibleIndex = 0
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

            Handler(Looper.getMainLooper()).postDelayed({
                if (binding.imageComment.lineCount > 5) {
                    binding.viewMoreLessLayout.visibility = View.VISIBLE
                    binding.viewMoreBtn.visibility = View.VISIBLE
                    binding.viewLessBtn.visibility = View.GONE
                } else {
                    binding.viewMoreLessLayout.visibility = View.GONE
                    binding.viewMoreBtn.visibility = View.GONE
                    binding.viewLessBtn.visibility = View.GONE
                }
            }, 10)

            binding.viewMoreBtn.setOnClickListener {
                if (binding.imageComment.maxLines == 5) {
                    binding.imageComment.maxLines = binding.imageComment.lineCount
                    binding.viewMoreLessLayout.visibility = View.VISIBLE
                    binding.viewMoreBtn.visibility = View.GONE
                    binding.viewLessBtn.visibility = View.VISIBLE
                }
            }

            binding.viewLessBtn.setOnClickListener {
                if (binding.imageComment.maxLines > 5) {
                    binding.imageComment.maxLines = 5
                    binding.viewMoreLessLayout.visibility = View.VISIBLE
                    binding.viewMoreBtn.visibility = View.VISIBLE
                    binding.viewLessBtn.visibility = View.GONE
                }
            }

        }
    }

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager?
                currentVisibleIndex =
                    layoutManager?.findFirstVisibleItemPosition() ?: RecyclerView.NO_POSITION
            }
        })
    }
}