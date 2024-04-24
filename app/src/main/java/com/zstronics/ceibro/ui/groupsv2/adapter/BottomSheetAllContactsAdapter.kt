package com.zstronics.ceibro.ui.groupsv2.adapter

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncDBContactsList
import com.zstronics.ceibro.databinding.LayoutItemGroupMemberSelectionBinding
import javax.inject.Inject

class BottomSheetAllContactsAdapter @Inject constructor() :
    RecyclerView.Adapter<BottomSheetAllContactsAdapter.ContactsSelectionViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: SyncDBContactsList.CeibroDBContactsLight) -> Unit)? =
        null
    var dataList: MutableList<SyncDBContactsList.CeibroDBContactsLight> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsSelectionViewHolder {
        return ContactsSelectionViewHolder(
            LayoutItemGroupMemberSelectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ContactsSelectionViewHolder, position: Int) {
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

    fun setSelectedList(contact: List<SyncDBContactsList.CeibroDBContactsLight>) {
        for (item in dataList) {
            val matchingContact = contact.find { it.connectionId == item.connectionId || it.ceibroUserId == item.userCeibroData?.id }
            item.isChecked = matchingContact != null
        }
        notifyDataSetChanged()
    }

    inner class ContactsSelectionViewHolder(private val binding: LayoutItemGroupMemberSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SyncDBContactsList.CeibroDBContactsLight) {

            with(binding) {
                val context = contactCheckBox.context

                contactCheckBox.isChecked = item.isChecked
                root.setOnClickListener {
                    item.isChecked = !item.isChecked
                    dataList[absoluteAdapterPosition].isChecked = item.isChecked
                    notifyItemChanged(absoluteAdapterPosition)
                    itemClickListener?.invoke(it, position, item)
                }

                when {
                    item.isCeibroUser -> {
                        ImageViewCompat.setImageTintList(
                            binding.ceibroLogo,
                            ColorStateList.valueOf(context.resources.getColor(R.color.appYellow))
                        )
                    }

                    else -> {
                        ImageViewCompat.setImageTintList(
                            binding.ceibroLogo,
                            ColorStateList.valueOf(context.resources.getColor(R.color.appGrey))
                        )
                    }
                }

                contactName.text = "${item.contactFirstName} ${item.contactSurName}"

                if (item.isCeibroUser) {
                    phoneNumber.text = ""
                    phoneNumber.visibility = View.GONE

                    if (item.userCeibroData?.companyName?.trim().isNullOrEmpty()) {
                        companyName.visibility = View.GONE
                        dot.visibility = View.GONE
                    } else {
                        companyName.visibility = View.VISIBLE
                        companyName.text = item.userCeibroData?.companyName?.trim()
                    }

                    if (item.userCeibroData?.jobTitle?.trim().isNullOrEmpty()) {
                        dot.visibility = View.GONE
                        jobTitle.visibility = View.GONE
                    } else {
                        if (!item.userCeibroData?.companyName?.trim().isNullOrEmpty()) {
                            dot.visibility = View.VISIBLE
                        }
                        jobTitle.visibility = View.VISIBLE
                        jobTitle.text = item.userCeibroData?.jobTitle?.trim()
                    }

                } else {
                    phoneNumber.text = item.phoneNumber
                    phoneNumber.visibility = View.VISIBLE
                    companyName.visibility = View.GONE
                    dot.visibility = View.GONE
                    jobTitle.visibility = View.GONE
                }



                if (item.isCeibroUser) {
                    if (item.userCeibroData?.profilePic.isNullOrEmpty()) {
                        contactInitials.visibility = View.VISIBLE
                        contactImage.visibility = View.GONE
                        var initials = ""
                        if (item.contactFirstName.isNotEmpty()) {
                            initials += item.contactFirstName[0].uppercaseChar()
                        }
                        if (item.contactSurName.isNotEmpty()) {
                            initials += item.contactSurName[0].uppercaseChar()
                        }

                        contactInitials.text = initials
                    } else {
                        contactInitials.visibility = View.GONE
                        contactImage.visibility = View.VISIBLE

                        Glide.with(binding.contactImage.context)
                            .load(item.userCeibroData?.profilePic)
                            .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                            .placeholder(R.drawable.profile_img)
                            .into(binding.contactImage)
                    }
                } else {
                    contactInitials.visibility = View.VISIBLE
                    contactImage.visibility = View.GONE
                    var initials = ""
                    if (item.contactFirstName.isNotEmpty()) {
                        initials += item.contactFirstName[0].uppercaseChar()
                    }
                    if (item.contactSurName.isNotEmpty()) {
                        initials += item.contactSurName[0].uppercaseChar()
                    }

                    contactInitials.text = initials
                }
            }
        }
    }
}