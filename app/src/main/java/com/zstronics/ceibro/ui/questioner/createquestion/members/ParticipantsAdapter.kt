package com.zstronics.ceibro.ui.questioner.createquestion.members

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.repos.chat.room.Member
import com.zstronics.ceibro.databinding.LayoutItemQuestionParticipantsBinding

class ParticipantsAdapter : RecyclerView.Adapter<ParticipantsAdapter.ChaMembersViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: Member) -> Unit)? = null

    var dataList: ArrayList<Member> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChaMembersViewHolder {
        return ChaMembersViewHolder(
            LayoutItemQuestionParticipantsBinding.inflate(
                LayoutInflater.from(parent.context), parent, false
            )
        )
    }

    override fun onBindViewHolder(holder: ChaMembersViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setList(list: List<Member>) {
        this.dataList.clear()
        this.dataList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ChaMembersViewHolder(private val binding: LayoutItemQuestionParticipantsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Member) {
            binding.member = item
            binding.appCompatCheckBox.isChecked = item.isChecked

            binding.appCompatCheckBox.setOnCheckedChangeListener { p0, p1 ->
                item.isChecked = p1
                dataList[absoluteAdapterPosition].isChecked = p1
                notifyItemChanged(absoluteAdapterPosition)
            }

        }
    }
}