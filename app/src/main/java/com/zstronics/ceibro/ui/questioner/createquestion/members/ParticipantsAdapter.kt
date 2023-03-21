package com.zstronics.ceibro.ui.questioner.createquestion.members

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
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

    fun setMembersCheckedUnChecked(checked: Boolean, accessList: List<String>) {
        val updatedMembers = dataList.map { member ->
            member.copy(isChecked = if (accessList.contains(member.id)) checked else member.isChecked)
        }
        setList(updatedMembers)
    }

    inner class ChaMembersViewHolder(private val binding: LayoutItemQuestionParticipantsBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Member) {
            binding.member = item
            binding.appCompatCheckBox.isChecked = item.isChecked
            binding.root.setOnClickListener {
                item.isChecked = !item.isChecked
                dataList[absoluteAdapterPosition].isChecked = item.isChecked
                notifyItemChanged(absoluteAdapterPosition)
                itemClickListener?.invoke(it, position, item)
            }

            if (item.profilePic == "" || item.profilePic.isNullOrEmpty()) {
                binding.connectionImgText.text =
                    "${item.firstName.get(0)?.uppercaseChar()}${
                        item.surName.get(0)?.uppercaseChar()
                    }"
                binding.connectionImgText.visibility = View.VISIBLE
                binding.connectionImg.visibility = View.GONE
            } else {
                Glide.with(binding.connectionImg.context)
                    .load(item.profilePic)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.profile_img)
                    .into(binding.connectionImg)
                binding.connectionImg.visibility = View.VISIBLE
                binding.connectionImgText.visibility = View.GONE
            }
        }
    }
}