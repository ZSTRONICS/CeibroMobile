package com.zstronics.ceibro.ui.tasks.newtask

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.databinding.LayoutItemChipBinding
import javax.inject.Inject

class MemberChipAdapter @Inject constructor() :
    RecyclerView.Adapter<MemberChipAdapter.ChipViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: Member) -> Unit)? = null

    private var list: MutableList<Member> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChipViewHolder {
        return ChipViewHolder(
            LayoutItemChipBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ChipViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<Member>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class ChipViewHolder(private val binding: LayoutItemChipBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: Member) {
            binding.crossView.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            binding.textView.text = item.firstName + " " + item.surName
        }
    }
}