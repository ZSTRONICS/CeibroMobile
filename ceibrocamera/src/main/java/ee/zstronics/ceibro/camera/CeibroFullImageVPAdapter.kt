package ee.zstronics.ceibro.camera

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ee.zstronics.ceibro.camera.databinding.LayoutCeibroFullImageViewerBinding

class CeibroFullImageVPAdapter constructor() :
    RecyclerView.Adapter<CeibroFullImageVPAdapter.CeibroFullImageViewerViewHolder>() {
    var itemClickListener: ((view: View, position: Int) -> Unit)? =
        null
    var listItems: MutableList<PickedImages> = mutableListOf()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CeibroFullImageViewerViewHolder {
        return CeibroFullImageViewerViewHolder(
            LayoutCeibroFullImageViewerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CeibroFullImageViewerViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<PickedImages>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class CeibroFullImageViewerViewHolder(private val binding: LayoutCeibroFullImageViewerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PickedImages) {
//            binding.root.setOnClickListener {
//                itemClickListener?.invoke(it, adapterPosition)
//            }
            val context = binding.fullImgView.context

            Glide.with(context)
                .load(item.fileUri)
                .into(binding.fullImgView)

        }
    }
}