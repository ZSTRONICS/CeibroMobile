package com.zstronics.ceibro.ui.tasks.v2.newtask.tag.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import com.zstronics.ceibro.databinding.LayoutItemHeaderBinding
import com.zstronics.ceibro.databinding.LayoutTagItemListingBinding

class TagsSectionRecyclerView(
    val context: Context,
    sectionList: MutableList<TagsDrawingSectionHeader>
) : SectionRecyclerViewAdapter<
        TagsDrawingSectionHeader,
        String,
        TagsSectionRecyclerView.ConnectionsSectionViewHolder,
        TagsSectionRecyclerView.ConnectionsChildViewHolder>(
    context,
    sectionList
) {


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
        p3: String?
    ) {
        holder?.bind(p3)
    }


    inner class ConnectionsSectionViewHolder constructor(private val binding: LayoutItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: TagsDrawingSectionHeader?) {

            binding.headerTitle.text = item?.getSectionText()
        }
    }

    inner class ConnectionsChildViewHolder constructor(val binding: LayoutTagItemListingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: String?) {
            binding.cbTag.text = item

        }
    }

}