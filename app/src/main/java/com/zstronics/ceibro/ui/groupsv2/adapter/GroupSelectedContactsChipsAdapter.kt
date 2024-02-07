package com.zstronics.ceibro.ui.groupsv2.adapter

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
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncDBContactsList
import com.zstronics.ceibro.databinding.LayoutItemAssigneeChipBinding
import com.zstronics.ceibro.databinding.LayoutItemAssigneeSelectionBinding
import com.zstronics.ceibro.databinding.LayoutItemContactBinding
import javax.inject.Inject

class GroupSelectedContactsChipsAdapter @Inject constructor() :
    RecyclerView.Adapter<GroupSelectedContactsChipsAdapter.AssigneeChipsViewHolder>() {
    var removeItemClickListener: ((view: View, position: Int, data: SyncDBContactsList.CeibroDBContactsLight) -> Unit)? =
        null
    var dataList: MutableList<SyncDBContactsList.CeibroDBContactsLight> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssigneeChipsViewHolder {
        return AssigneeChipsViewHolder(
            LayoutItemAssigneeChipBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AssigneeChipsViewHolder, position: Int) {
        holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    fun setList(list: List<SyncDBContactsList.CeibroDBContactsLight>) {
        this.dataList.clear()
        this.dataList.addAll(list)
        notifyDataSetChanged()
    }

    inner class AssigneeChipsViewHolder(private val binding: LayoutItemAssigneeChipBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SyncDBContactsList.CeibroDBContactsLight) {

            binding.removeBtn.setOnClickListener {
                val oldList = dataList
                oldList.removeAt(absoluteAdapterPosition)
                dataList = oldList
                removeItemClickListener?.invoke(it, absoluteAdapterPosition, item)
                notifyDataSetChanged()
            }


            if (item.isCeibroUser) {
                if (item.userCeibroData?.profilePic.isNullOrEmpty()) {
                    binding.contactInitials.visibility = View.VISIBLE
                    binding.contactImage.visibility = View.GONE
                    var initials = ""
                    if (item.contactFirstName.isNotEmpty()) {
                        initials += item.contactFirstName[0].uppercaseChar()
                    }
                    if (item.contactSurName.isNotEmpty()) {
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
                if (item.contactFirstName.isNotEmpty()) {
                    initials += item.contactFirstName[0].uppercaseChar()
                }
                if (item.contactSurName.isNotEmpty()) {
                    initials += item.contactSurName[0].uppercaseChar()
                }

                binding.contactInitials.text = initials
            }
        }
    }
}