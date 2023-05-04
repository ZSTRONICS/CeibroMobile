package com.zstronics.ceibro.ui.contacts.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.databinding.LayoutItemContactBinding
import javax.inject.Inject

class ContactsSelectionAdapter @Inject constructor() :
    RecyclerView.Adapter<ContactsSelectionAdapter.ContactsSelectionViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: SyncContactsRequest.CeibroContactLight) -> Unit)? =
        null
    var dataList: MutableList<SyncContactsRequest.CeibroContactLight> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsSelectionViewHolder {
        return ContactsSelectionViewHolder(
            LayoutItemContactBinding.inflate(
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

    fun setList(list: List<SyncContactsRequest.CeibroContactLight>) {
        this.dataList.clear()
        this.dataList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ContactsSelectionViewHolder(private val binding: LayoutItemContactBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: SyncContactsRequest.CeibroContactLight) {

            binding.contactCheckBox.isChecked = item.isChecked
            binding.root.setOnClickListener {
                item.isChecked = !item.isChecked
                dataList[absoluteAdapterPosition].isChecked = item.isChecked
                notifyItemChanged(absoluteAdapterPosition)
                itemClickListener?.invoke(it, position, item)
            }

            val phoneNumberUtil = PhoneNumberUtil.getInstance()
            val parsedNumber = phoneNumberUtil.parse(item.countryCode + item.phoneNumber, null)
            val formattedNumber =
                phoneNumberUtil.format(
                    parsedNumber,
                    PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
                )

            binding.phoneNumber.text = formattedNumber
            binding.contactName.text = "${item.contactFirstName} ${item.contactSurName}"
            if (item.beneficiaryPictureUrl.isEmpty()) {
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
                    .load(item.beneficiaryPictureUrl)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.profile_img)
                    .into(binding.contactImage)
            }
        }
    }
}