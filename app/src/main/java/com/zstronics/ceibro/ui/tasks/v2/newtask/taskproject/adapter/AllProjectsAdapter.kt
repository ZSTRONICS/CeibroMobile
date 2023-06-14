package com.zstronics.ceibro.ui.tasks.v2.newtask.taskproject.adapter

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
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponseV2
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.LayoutItemConnectionBinding
import com.zstronics.ceibro.databinding.LayoutItemTopicBinding
import java.util.Locale
import javax.inject.Inject

class AllProjectsAdapter @Inject constructor() :
    RecyclerView.Adapter<AllProjectsAdapter.AllProjectsViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: AllProjectsResponseV2.ProjectsV2) -> Unit)? =
        null
    var listItems: MutableList<AllProjectsResponseV2.ProjectsV2> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllProjectsViewHolder {
        return AllProjectsViewHolder(
            LayoutItemTopicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllProjectsViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<AllProjectsResponseV2.ProjectsV2>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllProjectsViewHolder(private val binding: LayoutItemTopicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllProjectsResponseV2.ProjectsV2) {

            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            val context = binding.menuBtn.context

            binding.topicName.text = item.title.capitalize(Locale.ROOT)
            binding.menuBtn.visibility = View.VISIBLE
        }
    }
}