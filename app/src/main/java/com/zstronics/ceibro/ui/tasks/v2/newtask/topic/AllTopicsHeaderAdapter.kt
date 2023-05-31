package com.zstronics.ceibro.ui.tasks.v2.newtask.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.LayoutItemConnectionHeaderBinding
import com.zstronics.ceibro.databinding.LayoutItemTopicHeaderBinding
import javax.inject.Inject

class AllTopicsHeaderAdapter @Inject constructor() :
    RecyclerView.Adapter<AllTopicsHeaderAdapter.AllTopicsHeaderViewHolder>() {
    var allTopicItemClickListener: ((view: View, position: Int, data: TopicsResponse.TopicData) -> Unit)? =
        null
    var listItems: MutableList<TopicVM.CeibroTopicGroup> = mutableListOf()
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllTopicsHeaderViewHolder {
        return AllTopicsHeaderViewHolder(
            LayoutItemTopicHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllTopicsHeaderViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<TopicVM.CeibroTopicGroup>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllTopicsHeaderViewHolder(private val binding: LayoutItemTopicHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TopicVM.CeibroTopicGroup) {

            binding.headerTitle.text = item.sectionLetter.toString()
            val adapter = AllTopicsAdapter()
            adapter.setList(item.items)
            binding.topicRV.adapter = adapter
            adapter.itemClickListener =
                { it: View, position: Int, data: TopicsResponse.TopicData ->
                    allTopicItemClickListener?.invoke(it, absoluteAdapterPosition, data)
                }
        }
    }
}