package com.zstronics.ceibro.ui.tasks.v2.taskdetail.adapter

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.zstronics.ceibro.R
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

            if (item.hasComment) {
                binding.imgComment.text = item.comment
            }

            val context = binding.smallImgView.context

            val circularProgressDrawable = CircularProgressDrawable(context)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 30f
            circularProgressDrawable.start()

            val requestOptions = RequestOptions()
                .placeholder(circularProgressDrawable)
                .error(R.drawable.icon_corrupt_file)
                .skipMemoryCache(true)
                .centerCrop()

            Glide.with(context)
                .load(item.fileUrl)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.smallImgView)

            Handler(Looper.getMainLooper()).postDelayed({
                if (binding.imgComment.lineCount > 5) {
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
                if (binding.imgComment.maxLines == 5) {
                    binding.imgComment.maxLines = binding.imgComment.lineCount
                    binding.viewMoreLessLayout.visibility = View.VISIBLE
                    binding.viewMoreBtn.visibility = View.GONE
                    binding.viewLessBtn.visibility = View.VISIBLE
                }
            }

            binding.viewLessBtn.setOnClickListener {
                if (binding.imgComment.maxLines > 5) {
                    binding.imgComment.maxLines = 5
                    binding.viewMoreLessLayout.visibility = View.VISIBLE
                    binding.viewMoreBtn.visibility = View.VISIBLE
                    binding.viewLessBtn.visibility = View.GONE
                }
            }

        }
    }
}