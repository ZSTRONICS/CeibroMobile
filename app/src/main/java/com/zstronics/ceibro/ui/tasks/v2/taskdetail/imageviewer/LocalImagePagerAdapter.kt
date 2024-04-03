package com.zstronics.ceibro.ui.tasks.v2.taskdetail.imageviewer

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.zstronics.ceibro.R
import com.zstronics.ceibro.databinding.LayoutImageViewerBinding
import ee.zstronics.ceibro.camera.PickedImages
import javax.inject.Inject

class LocalImagePagerAdapter @Inject constructor() :
    RecyclerView.Adapter<LocalImagePagerAdapter.LocalImagePagerViewHolder>() {
    var itemClickListener: ((view: View, position: Int) -> Unit)? =
        null
    var listItems: MutableList<PickedImages> = mutableListOf()
    var currentVisibleIndex = 0
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

    fun setList(list: List<PickedImages>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class LocalImagePagerViewHolder(private val binding: LayoutImageViewerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PickedImages) {
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
                .load(item.fileUri)
                .apply(requestOptions)
                .transition(DrawableTransitionOptions.withCrossFade())
                .into(binding.fullImgView)


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