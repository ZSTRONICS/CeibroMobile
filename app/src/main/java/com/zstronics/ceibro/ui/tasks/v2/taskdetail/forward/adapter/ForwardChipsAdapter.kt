package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.databinding.LayoutItemAssigneeChipBinding
import com.zstronics.ceibro.databinding.LayoutItemAssigneeSelectionBinding
import com.zstronics.ceibro.databinding.LayoutItemContactBinding
import javax.inject.Inject

class ForwardChipsAdapter @Inject constructor() :
    RecyclerView.Adapter<ForwardChipsAdapter.ForwardChipsViewHolder>() {
    var removeItemClickListener: ((view: View, position: Int, data: AllCeibroConnections.CeibroConnection) -> Unit)? =
        null
    var dataList: MutableList<AllCeibroConnections.CeibroConnection> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ForwardChipsViewHolder {
        return ForwardChipsViewHolder(
            LayoutItemAssigneeChipBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ForwardChipsViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setList(list: List<AllCeibroConnections.CeibroConnection>) {
        this.dataList.clear()
        this.dataList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ForwardChipsViewHolder(private val binding: LayoutItemAssigneeChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllCeibroConnections.CeibroConnection) {

            binding.removeBtn.setOnClickListener {
                val oldList = dataList
                oldList.removeAt(absoluteAdapterPosition)
                dataList = oldList
                notifyDataSetChanged()
                removeItemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }


            if (item.isCeiborUser) {
                if (item.userCeibroData?.profilePic.isNullOrEmpty()) {
                    binding.contactInitials.visibility = View.VISIBLE
                    binding.contactImage.visibility = View.GONE
                    var initials = ""
                    if (item.contactFirstName?.isNotEmpty() == true) {
                        initials += item.contactFirstName[0].uppercaseChar()
                    }
                    if (item.contactSurName?.isNotEmpty() == true) {
                        initials += item.contactSurName[0].uppercaseChar()
                    }

                    binding.contactInitials.text = initials
                } else {
                    binding.contactInitials.visibility = View.GONE
                    binding.contactImage.visibility = View.VISIBLE

                    Glide.with(binding.contactImage.context)
                        .load(item.userCeibroData?.profilePic)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.profile_img)
                        .into(binding.contactImage)
                }
            } else {
                binding.contactInitials.visibility = View.VISIBLE
                binding.contactImage.visibility = View.GONE
                var initials = ""
                if (item.contactFirstName?.isNotEmpty() == true) {
                    initials += item.contactFirstName[0].uppercaseChar()
                }
                if (item.contactSurName?.isNotEmpty() == true) {
                    initials += item.contactSurName[0].uppercaseChar()
                }

                binding.contactInitials.text = initials
            }
        }
    }
}