package ee.zstronics.ceibro.camera

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ee.zstronics.ceibro.camera.databinding.LayoutCeibroFullImageViewerBinding
import ee.zstronics.ceibro.camera.databinding.LayoutCeibroSmallImageViewerBinding

class CeibroSmallImageRVAdapter constructor() :
    RecyclerView.Adapter<CeibroSmallImageRVAdapter.CeibroSmallImageViewerViewHolder>() {
    var itemClickListener: ((view: View, position: Int) -> Unit)? =
        null
    var listItems: MutableList<PickedImages> = mutableListOf()
    private var selectedItemPosition = RecyclerView.NO_POSITION

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CeibroSmallImageViewerViewHolder {
        return CeibroSmallImageViewerViewHolder(
            LayoutCeibroSmallImageViewerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CeibroSmallImageViewerViewHolder, position: Int) {
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

    fun setSelectedItem(position: Int) {
        val previousSelectedPosition = selectedItemPosition
        selectedItemPosition = position
        notifyItemChanged(previousSelectedPosition)
        notifyItemChanged(selectedItemPosition)
    }

    inner class CeibroSmallImageViewerViewHolder(private val binding: LayoutCeibroSmallImageViewerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PickedImages) {
            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, adapterPosition)
                val previousSelectedPosition = selectedItemPosition
                selectedItemPosition = adapterPosition

                // Notify adapter about the item selection changes
                notifyItemChanged(previousSelectedPosition)
                notifyItemChanged(selectedItemPosition)
            }

            val context = binding.smallImgView.context

            Glide.with(context)
                .load(item.fileUri)
                .into(binding.smallImgView)


            if (adapterPosition == selectedItemPosition) {
                binding.parentCard.background = context.resources.getDrawable(R.drawable.card_outline)
            } else {
                binding.parentCard.background = null
            }

        }
    }
}