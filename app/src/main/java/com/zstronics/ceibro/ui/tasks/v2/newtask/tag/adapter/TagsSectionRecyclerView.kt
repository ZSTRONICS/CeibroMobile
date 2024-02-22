package com.zstronics.ceibro.ui.tasks.v2.newtask.tag.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import com.zstronics.ceibro.data.repos.task.models.TopicsResponse
import com.zstronics.ceibro.databinding.LayoutItemHeaderBinding
import com.zstronics.ceibro.databinding.LayoutTagItemListingBinding

class TagsSectionRecyclerView(
    val context: Context,
    sectionList: MutableList<TagsDrawingSectionHeader>
) : SectionRecyclerViewAdapter<
        TagsDrawingSectionHeader,
        TopicsResponse.TopicData,
        TagsSectionRecyclerView.ConnectionsSectionViewHolder,
        TagsSectionRecyclerView.ConnectionsChildViewHolder>(
    context,
    sectionList
) {

    var itemClickListener: ((flag: Boolean, view: View, position: Int, data: TopicsResponse.TopicData) -> Unit)? =
        null
    var oldTags: ArrayList<TopicsResponse.TopicData> = arrayListOf()
    override fun onCreateSectionViewHolder(
        sectionViewGroup: ViewGroup?,
        viewType: Int
    ): ConnectionsSectionViewHolder {
        return ConnectionsSectionViewHolder(
            LayoutItemHeaderBinding.inflate(
                LayoutInflater.from(context),
                sectionViewGroup,
                false
            )
        )
    }

    override fun onBindSectionViewHolder(
        connectionsSectionViewHolder: ConnectionsSectionViewHolder?,
        sectionPosition: Int,
        connectionsSectionHeader: TagsDrawingSectionHeader?
    ) {
        connectionsSectionViewHolder?.bind(connectionsSectionHeader)
    }

    override fun onCreateChildViewHolder(
        childViewGroup: ViewGroup?,
        viewType: Int
    ): ConnectionsChildViewHolder {
        return ConnectionsChildViewHolder(
            LayoutTagItemListingBinding.inflate(
                LayoutInflater.from(context),
                childViewGroup,
                false
            )
        )
    }


    override fun onBindChildViewHolder(
        holder: ConnectionsChildViewHolder?,
        sectionPosition: Int,
        childPostitoin: Int,
        p3: TopicsResponse.TopicData
    ) {
        holder?.bind(p3)
    }


    inner class ConnectionsSectionViewHolder constructor(private val binding: LayoutItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TagsDrawingSectionHeader?) {

            item?.let {
                if (item.childItems.isNullOrEmpty()) {
                    binding.headerTitle.visibility = View.GONE
                } else {
                    binding.headerTitle.visibility = View.VISIBLE
                    binding.headerTitle.text = item.getSectionText()
                }
            }

        }
    }

    inner class ConnectionsChildViewHolder constructor(val binding: LayoutTagItemListingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TopicsResponse.TopicData) {

            val currentTag = oldTags.find { it.id == item.id }
            if (currentTag != null) {
                binding.cbTag.isChecked = true
            }
            binding.cbTag.text = item.topic
            binding.cbTag.setOnClickListener {
                if (binding.cbTag.isChecked) {
                    itemClickListener?.invoke(true, it, position, item)
                } else {
                    itemClickListener?.invoke(false, it, position, item)
                }
            }
        }
    }

    fun setData(oldTagsList: MutableList<TopicsResponse.TopicData>?) {
        oldTags.clear()
        if (oldTagsList!=null){
            oldTags.addAll(oldTagsList)
        }
        notifyDataSetChanged()

    }
}