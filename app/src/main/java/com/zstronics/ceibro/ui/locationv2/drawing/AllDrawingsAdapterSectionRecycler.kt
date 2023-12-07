package com.zstronics.ceibro.ui.locationv2.drawing

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.tasks.CeibroTask
import com.zstronics.ceibro.databinding.DrawingDetailItemListBinding
import com.zstronics.ceibro.databinding.LayoutDrawingItemListBinding
import com.zstronics.ceibro.databinding.LayoutItemHeaderBinding
import com.zstronics.ceibro.ui.tasks.task.TaskStatus

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
    var popupMenu : PopupMenu?=null
    private var isPopupMenuShowing=false
    var itemClickListener: ((view: View, position: Int, data: String, tag: String) -> Unit)? =
        null

    fun setCallBack(itemClickListener: ((view: View, position: Int, data: String, tag: String) -> Unit)?) {
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


            binding.ivDownload.setOnClickListener {
                itemClickListener?.invoke(it, 1, "", "")
            }

            binding.ivOptions.setOnClickListener {
          togglePopupMenu(it)
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
    private fun togglePopupMenu(view: View) {
//        if (isPopupMenuShowing) {
//            popupMenu?.dismiss()
//        } else {
            popUpMenu(view)
//        }
//        isPopupMenuShowing = !isPopupMenuShowing
    }

    private fun popUpMenu(v: View): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_location_group_menu, null)

        //following code is to make popup at top if the view is at bottom
        popupWindow.isFocusable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view

        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        //ShowAsDropDown statement at bottom, according to the view visibilities
        //////////////////////
        popupWindow.showAsDropDown(v, 0, 5)

//        val editTask = view.findViewById<View>(R.id.editTask)
//        val deleteTask = view.findViewById<View>(R.id.deleteTask)


//        if (positionOfIcon > height) {
//            if (deleteTask.visibility == View.GONE) {
//                popupWindow.showAsDropDown(v, -135, -245)
//            }
//            else {
//                popupWindow.showAsDropDown(v, -170, -405)
//            }
//        } else {
//            popupWindow.showAsDropDown(v, 0, 5)
//        }
        return popupWindow
    }


}