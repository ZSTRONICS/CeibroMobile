package com.zstronics.ceibro.ui.tasks.v2.newtask.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.LayoutItemTagChipBindingBinding
import java.util.ArrayList
import javax.inject.Inject

class TagsChipsAdapter @Inject constructor() :
    RecyclerView.Adapter<TagsChipsAdapter.AssigneeChipsViewHolder>() {

    var removeItemClickListener: (( data: TopicsResponse.TopicData) -> Unit)? =
        null

    var dataList: MutableList<TopicsResponse.TopicData> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssigneeChipsViewHolder {
        return AssigneeChipsViewHolder(
            LayoutItemTagChipBindingBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AssigneeChipsViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setList(list: ArrayList<TopicsResponse.TopicData>) {
        this.dataList.clear()
        this.dataList.addAll(list)
        notifyDataSetChanged()
    }

    inner class AssigneeChipsViewHolder(private val binding: LayoutItemTagChipBindingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: TopicsResponse.TopicData) {

            binding.removeBtn.setOnClickListener {
                val oldList = dataList
                oldList.removeAt(absoluteAdapterPosition)
                dataList = oldList

                removeItemClickListener?.invoke(item)
                notifyDataSetChanged()
            }

            binding.contactInitials.text = item.topic
        }
    }
}