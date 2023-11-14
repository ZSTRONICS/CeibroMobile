package com.zstronics.ceibro.ui.projectv2.allprojectsv2

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import com.zstronics.ceibro.data.repos.chat.room.Project
import com.zstronics.ceibro.databinding.LayoutItemHeaderBinding
import com.zstronics.ceibro.databinding.LayoutProjectItemListBinding

class AllProjectsAdapterSectionRecycler constructor(
    val context: Context,
    sectionList: MutableList<ProjectsSectionHeader>
) :
    SectionRecyclerViewAdapter<ProjectsSectionHeader, Project, AllProjectsAdapterSectionRecycler.ConnectionsSectionViewHolder, AllProjectsAdapterSectionRecycler.ConnectionsChildViewHolder>(
        context,
        sectionList
    ) {

    var itemClickListener: ((view: View, position: Int, data: Project) -> Unit)? = null

    fun setCallBack(itemClickListener: ((view: View, position: Int, data: Project) -> Unit)?) {
        this.itemClickListener = itemClickListener

    }

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
        p1: Int,
        connectionsSectionHeader: ProjectsSectionHeader?
    ) {
        connectionsSectionViewHolder?.bind(connectionsSectionHeader)
    }

    override fun onCreateChildViewHolder(
        childViewGroup: ViewGroup?,
        viewType: Int
    ): ConnectionsChildViewHolder {
        return ConnectionsChildViewHolder(
            LayoutProjectItemListBinding.inflate(
                LayoutInflater.from(context),
                childViewGroup,
                false
            )
        )
    }

    override fun onBindChildViewHolder(
        p0: ConnectionsChildViewHolder?,
        p1: Int,
        p2: Int,
        p3: Project?
    ) {
        p0?.bind(p3,p2)
    }




    inner class ConnectionsSectionViewHolder constructor(private val binding: LayoutItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProjectsSectionHeader?) {
            binding.headerTitle.text = item?.getSectionText()


        }
    }

    inner class ConnectionsChildViewHolder constructor(val binding: LayoutProjectItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Project?, p2: Int) {

            item?.let {
                with(binding) {
                    this.projectName.text = item.title
                    this.llProjectDetail.setOnClickListener {
                        itemClickListener?.invoke(it,p2,item)
                    }
                }
            }
        }
    }

}