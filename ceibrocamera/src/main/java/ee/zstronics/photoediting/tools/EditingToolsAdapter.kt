package ee.zstronics.photoediting.tools

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import ee.zstronics.ceibro.camera.R

/**
 * @author [Burhanuddin Rashid](https://github.com/burhanrashid52)
 * @version 0.1.2
 * @since 5/23/2018
 */
class EditingToolsAdapter(private val mOnItemSelected: OnItemSelected) :
    RecyclerView.Adapter<EditingToolsAdapter.ViewHolder>() {
    private val mToolList: MutableList<ToolModel> = ArrayList()
    var selectedPosition = -1

    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType)
    }

    internal inner class ToolModel(
        val mToolName: String,
        val mToolIcon: Int,
        val mToolType: ToolType
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_editing_tools, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = mToolList[position]
        holder.txtTool.text = item.mToolName
        holder.imgToolIcon.setImageResource(item.mToolIcon)
        if (selectedPosition == position) {
            holder.mainView.setBackgroundResource(R.color.white)
            ImageViewCompat.setImageTintList(
                holder.imgToolIcon,
                ColorStateList.valueOf(holder.imgToolIcon.context.resources.getColor(R.color.black))
            )
        } else {
            holder.mainView.setBackgroundResource(R.color.transparent)
            ImageViewCompat.setImageTintList(
                holder.imgToolIcon,
                ColorStateList.valueOf(holder.imgToolIcon.context.resources.getColor(R.color.white))
            )
        }
    }

    override fun getItemCount(): Int {
        return mToolList.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val mainView: View = itemView
        val imgToolIcon: ImageView = itemView.findViewById(R.id.imgToolIcon)
        val txtTool: TextView = itemView.findViewById(R.id.txtTool)

        init {
            itemView.setOnClickListener { _: View? ->
                selectedPosition = layoutPosition
                notifyDataSetChanged()
                mOnItemSelected.onToolSelected(
                    mToolList[layoutPosition].mToolType
                )
            }
        }
    }

    init {
        mToolList.add(
            ToolModel(
                "Rectangle Shape",
                R.drawable.icon_square,
                ToolType.RECTANGLE_SHAPE
            )
        )
        mToolList.add(ToolModel("Arrow Shape", R.drawable.icon_top_arrow, ToolType.ARROW_SHAPE))
        mToolList.add(ToolModel("Brush", R.drawable.icon_edit_pencil, ToolType.BRUSH))
        mToolList.add(ToolModel("Text", R.drawable.icon_insert_text, ToolType.TEXT))
        mToolList.add(ToolModel("Undo", R.drawable.icon_undo, ToolType.UNDO))
//        mToolList.add(ToolModel("Eraser", R.drawable.ic_eraser, ToolType.ERASER))
//        mToolList.add(ToolModel("Emoji", R.drawable.ic_insert_emoticon, ToolType.EMOJI))
//        mToolList.add(ToolModel("Sticker", R.drawable.ic_sticker, ToolType.STICKER))
    }
}