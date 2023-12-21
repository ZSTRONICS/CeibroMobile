package com.zstronics.ceibro.ui.projectv2.projectdetailv2.drawing

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupMenu
import android.widget.PopupWindow
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.cancelAndMakeToast
import com.zstronics.ceibro.data.database.dao.DownloadedDrawingV2Dao
import com.zstronics.ceibro.data.database.models.projects.CeibroGroupsV2
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.databinding.DrawingDetailItemListBinding
import com.zstronics.ceibro.databinding.LayoutDrawingItemListBinding
import com.zstronics.ceibro.databinding.LayoutItemHeaderBinding
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.io.File

class AllDrawingsAdapterSectionRecycler(
    val context: Context,
    sectionList: MutableList<DrawingSectionHeader>,
    val downloadedDrawingV2Dao: DownloadedDrawingV2Dao
) : SectionRecyclerViewAdapter<
        DrawingSectionHeader,
        CeibroGroupsV2,
        AllDrawingsAdapterSectionRecycler.ConnectionsSectionViewHolder,
        AllDrawingsAdapterSectionRecycler.ConnectionsChildViewHolder>(
    context,
    sectionList
) {
    var popupMenu: PopupMenu? = null
    private var isPopupMenuShowing = false
    var drawingFileClickListener: ((view: View, data: DrawingV2, tag: String) -> Unit)? =
        null

    fun drawingFileClickListenerCallBack(itemClickListener: ((view: View, data: DrawingV2, tag: String) -> Unit)?) {
        this.drawingFileClickListener = itemClickListener
    }

    var downloadFileClickListener: ((view: View, data: DrawingV2, tag: String) -> Unit)? =
        null

    fun downloadFileCallBack(itemClickListener: ((view: View, data: DrawingV2, tag: String) -> Unit)?) {
        this.downloadFileClickListener = itemClickListener
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
        p3: CeibroGroupsV2?
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
        fun bind(item: CeibroGroupsV2?) {

            binding.groupLayout.setOnClickListener {
                if (binding.llParent.visibility == View.VISIBLE) {
                    binding.ivDropDown.setImageResource(R.drawable.icon_drop_down)
                    binding.llParent.visibility = View.GONE
                } else {
                    binding.ivDropDown.setImageResource(R.drawable.arrow_drop_up)
                    binding.llParent.visibility = View.VISIBLE
                }
            }

            binding.tvGroupName.text = item?.groupName
            binding.tvGroupBy.text = "From: ${item?.creator?.firstName} ${item?.creator?.surName}"


//            binding.ivDownload.setOnClickListener {
//                itemClickListener?.invoke(it, 1, "", "")
//            }

            binding.ivOptions.setOnClickListener {
                togglePopupMenu(it)
            }

            binding.llParent.removeAllViews()


            item?.drawings?.forEachIndexed { index, data ->


                val itemViewBinding: DrawingDetailItemListBinding = DataBindingUtil.inflate(
                    LayoutInflater.from(binding.root.context),
                    R.layout.drawing_detail_item_list,
                    binding.llParent,
                    false
                )
                itemViewBinding.tvSample.text = "${data.fileName}"
                itemViewBinding.tvFloor.text = "${data.floor.floorName} Floor"

                /*
                   itemViewBinding.root.setOnClickListener{
                       val file = File(data.uploaderLocalFilePath)
                       if (file.exists()) {
                           drawingFileClickListener?.invoke(it, data, "")
                       } else {
                           cancelAndMakeToast(it.context, "File not downloaded", Toast.LENGTH_SHORT)
                       }
                   }*/

                itemViewBinding.root.setOnClickListener { view ->
                    MainScope().launch {
                        val drawingObject =
                            downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(data._id)
                        drawingObject?.let {

                            val file = File(it.localUri)
                            if (file.exists()) {
                                drawingFileClickListener?.invoke(view, data, it.localUri)
                            } else {
                                cancelAndMakeToast(
                                    view.context,
                                    "File not downloaded",
                                    Toast.LENGTH_SHORT
                                )
                            }
                        }
                    }


                }


                MainScope().launch {
                    val drawingObject =
                        downloadedDrawingV2Dao.getDownloadedDrawingByDrawingId(data._id)
                    drawingObject?.let {

                        itemViewBinding.ivDownloadFile.visibility = View.INVISIBLE
                        itemViewBinding.ivDownloadFile.isClickable =false
                    }?: kotlin.run {
                        itemViewBinding.ivDownloadFile.visibility = View.VISIBLE
                    }
                }
                itemViewBinding.ivDownloadFile.setOnClickListener {
                    val file = File(data.fileName)
                    if (file.exists()) {
                        //openFile
                    } else {
                        downloadFileClickListener?.invoke(it, data, "")
                        //  cancelAndMakeToast(it.context, "File not downloaded", Toast.LENGTH_SHORT)
                    }
                }



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