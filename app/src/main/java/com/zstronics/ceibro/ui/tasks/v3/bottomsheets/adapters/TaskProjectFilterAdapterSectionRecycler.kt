package com.zstronics.ceibro.ui.tasks.v3.bottomsheets.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.databinding.LayoutItemHeaderBinding
import com.zstronics.ceibro.databinding.LayoutItemProjectListingBinding
import com.zstronics.ceibro.ui.locationv2.locationproject.LocationProjectsSectionHeader

class TaskProjectFilterAdapterSectionRecycler constructor(
    val context: Context,
    sectionList: MutableList<LocationProjectsSectionHeader>
) :
    SectionRecyclerViewAdapter<
            LocationProjectsSectionHeader,
            CeibroProjectV2,
            TaskProjectFilterAdapterSectionRecycler.ConnectionsSectionViewHolder,
            TaskProjectFilterAdapterSectionRecycler.ConnectionsChildViewHolder>(
        context,
        sectionList
    ) {
    var selectedList= ArrayList<CeibroProjectV2>()
    fun setSelectedList(list: MutableList<CeibroProjectV2>) {
        selectedList.addAll(list)
        notifyDataSetChanged()
    }

    var itemClickListener: ((data: CeibroProjectV2, tag: Boolean) -> Unit)? =
        null

    fun setCallBack(itemClickListener: ((data: CeibroProjectV2, tag: Boolean) -> Unit)?) {
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
        sectionPosition: Int,
        connectionsSectionHeader: LocationProjectsSectionHeader?
    ) {
        connectionsSectionViewHolder?.bind(connectionsSectionHeader)
    }

    override fun onCreateChildViewHolder(
        childViewGroup: ViewGroup?,
        viewType: Int
    ): ConnectionsChildViewHolder {
        return ConnectionsChildViewHolder(
            LayoutItemProjectListingBinding.inflate(
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
        data: CeibroProjectV2?
    ) {
        holder?.bind(data, sectionPosition)
    }


    inner class ConnectionsSectionViewHolder constructor(private val binding: LayoutItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: LocationProjectsSectionHeader?) {
            binding.headerTitle.text = item?.getSectionText()
            binding.headerTitle.textSize = 14f
            binding.headerTitle.setTextColor(context.getColor(R.color.appGrey3))

            if (item?.childItems.isNullOrEmpty()) {
                binding.headerTitle.visibility = View.GONE
            } else {
                binding.headerTitle.visibility = View.VISIBLE
            }
        }
    }

    inner class ConnectionsChildViewHolder constructor(val binding: LayoutItemProjectListingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CeibroProjectV2?, sectionPosition: Int) {

            item?.let {
                with(binding) {

                    projectImgImgCard.setOnClickListener {
                        if (projectImgImgCard.isChecked) {
                            itemClickListener?.invoke(item, true)
                        } else {
                            itemClickListener?.invoke(item, false)
                        }
                    }
                    ivHide.visibility = View.GONE
                    if (item.isFavoriteByMe) {
                        ivFav.visibility = View.VISIBLE
                    } else {
                        ivFav.visibility = View.GONE
                    }

                    projectName.text = item.title

                    if (item.creator.firstName.trim().isEmpty() && item.creator.surName.trim()
                            .isEmpty()
                    ) {
                        binding.userName.text = ""
                        binding.userName.visibility = View.GONE
                    } else {
                        binding.userName.text = "${item.creator.firstName} ${item.creator.surName}"
                    }

                    if (item.creator.companyName.isNullOrEmpty()) {
                        binding.userCompany.text = ""
                        binding.userCompany.visibility = View.GONE
                    } else {
                        binding.userCompany.text = item.creator.companyName
                    }

                    val project = selectedList.find { listItem ->
                        listItem._id == item._id
                    }
                    projectImgImgCard.isChecked = project != null

                    root.setOnClickListener {
                        if (projectImgImgCard.isChecked) {
                            projectImgImgCard.isChecked = false
                            itemClickListener?.invoke(item, false)
                        } else {
                            projectImgImgCard.isChecked = true
                            itemClickListener?.invoke(item, true)
                        }
                    }
                }
            }
        }
    }
}