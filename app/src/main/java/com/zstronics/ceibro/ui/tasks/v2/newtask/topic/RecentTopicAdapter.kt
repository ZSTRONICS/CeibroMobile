package com.zstronics.ceibro.ui.tasks.v2.newtask.topic

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.LayoutItemTopicBinding
import javax.inject.Inject

class RecentTopicAdapter @Inject constructor() :
    RecyclerView.Adapter<RecentTopicAdapter.RecentTopicViewHolder>() {
    var recentTopicItemClickListener: ((view: View, position: Int, data: TopicsResponse.TopicData) -> Unit)? =
        null
    var listItems: MutableList<TopicsResponse.TopicData> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentTopicViewHolder {
        return RecentTopicViewHolder(
            LayoutItemTopicBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecentTopicViewHolder, position: Int) {
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

    inner class RecentTopicViewHolder(private val binding: LayoutItemTopicBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TopicsResponse.TopicData) {
            binding.root.setOnClickListener {
                recentTopicItemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            val context = binding.menuBtn.context

            binding.topicName.text = item.topic
            binding.menuBtn.visibility = View.GONE
        }
    }
}