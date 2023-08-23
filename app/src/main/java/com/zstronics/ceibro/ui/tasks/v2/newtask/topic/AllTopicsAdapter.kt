package com.zstronics.ceibro.ui.tasks.v2.newtask.topic

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.LayoutItemConnectionBinding
import com.zstronics.ceibro.databinding.LayoutItemTopicBinding
import java.util.Locale
import javax.inject.Inject

class AllTopicsAdapter @Inject constructor() :
    RecyclerView.Adapter<AllTopicsAdapter.AllTopicsViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: TopicsResponse.TopicData) -> Unit)? =
        null
    var listItems: MutableList<TopicsResponse.TopicData> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllTopicsViewHolder {
        return AllTopicsViewHolder(
            LayoutItemTopicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllTopicsViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<TopicsResponse.TopicData>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllTopicsViewHolder(private val binding: LayoutItemTopicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TopicsResponse.TopicData) {

            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            val context = binding.menuBtn.context

            binding.topicName.text = item.topic.capitalize(Locale.ROOT)
            binding.menuBtn.visibility = View.GONE
        }
    }
}