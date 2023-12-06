package com.zstronics.ceibro.ui.locationv2.drawing

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.databinding.DrawingDetailItemListBinding
import com.zstronics.ceibro.databinding.LayoutDrawingItemListBinding
import com.zstronics.ceibro.databinding.LayoutItemHeaderBinding

class AllDrawingsAdapterSectionRecycler constructor(
    val context: Context,
    sectionList: MutableList<DrawingSectionHeader>
) :
    SectionRecyclerViewAdapter<
            DrawingSectionHeader,
            StringListData,
            AllDrawingsAdapterSectionRecycler.ConnectionsSectionViewHolder,
            AllDrawingsAdapterSectionRecycler.ConnectionsChildViewHolder>(
        context,
        sectionList
    ) {

    var itemClickListener: ((view: View, position: Int, data: CeibroProjectV2, tag: String) -> Unit)? =
        null

    fun setCallBack(itemClickListener: ((view: View, position: Int, data: CeibroProjectV2, tag: String) -> Unit)?) {
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
        connectionsSectionHeader: DrawingSectionHeader?
    ) {
        connectionsSectionViewHolder?.bind(connectionsSectionHeader)
    }

    override fun onCreateChildViewHolder(
        childViewGroup: ViewGroup?,
        viewType: Int
    ): ConnectionsChildViewHolder {
        return ConnectionsChildViewHolder(
            LayoutDrawingItemListBinding.inflate(
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
        p3: StringListData?
    ) {
        holder?.bind(p3)
    }


    inner class ConnectionsSectionViewHolder constructor(private val binding: LayoutItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: DrawingSectionHeader?) {
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

    inner class ConnectionsChildViewHolder constructor(val binding: LayoutDrawingItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: StringListData?) {


            binding.root.setOnClickListener {
                if (binding.llParent.visibility == View.VISIBLE) {
                    binding.ivDropDown.setImageResource(R.drawable.icon_drop_down)
                    binding.llParent.visibility = View.GONE
                } else {
                    binding.ivDropDown.setImageResource(R.drawable.arrow_drop_up)
                    binding.llParent.visibility = View.VISIBLE
                }
            }
            binding.llParent.removeAllViews()

            item?.stringList?.forEachIndexed { index, data ->


                val itemViewBinding: DrawingDetailItemListBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(binding.root.context),
                    R.layout.drawing_detail_item_list,
                    binding.llParent,
                    false
                )
                itemViewBinding.tvSample.text = "$data .pdf"
                itemViewBinding.tvFloor.text = "Floor ${index + 1}"


                binding.llParent.addView(itemViewBinding.root)

            }
        }
    }

    private fun dpToPx(context: Context, dp: Int): Int {
        val scale = context.resources.displayMetrics.density
        return (dp * scale + 0.5f).toInt()
    }
}