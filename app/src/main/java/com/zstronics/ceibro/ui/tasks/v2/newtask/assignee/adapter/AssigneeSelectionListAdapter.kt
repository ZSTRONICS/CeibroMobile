package com.zstronics.ceibro.ui.tasks.v2.newtask.assignee.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.databinding.LayoutItemAssigneeSelectionBinding
import com.zstronics.ceibro.databinding.LayoutItemContactBinding
import javax.inject.Inject

class AssigneeSelectionListAdapter @Inject constructor() :
    PagingDataAdapter<AllCeibroConnections.CeibroConnection, AssigneeSelectionListAdapter.AssigneeSelectionListViewHolder>(DIFF_CALLBACK) {
    var itemClickListener: ((view: View, position: Int, data: AllCeibroConnections.CeibroConnection) -> Unit)? =
        null
    var dataList: MutableList<AllCeibroConnections.CeibroConnection> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AssigneeSelectionListViewHolder {
        return AssigneeSelectionListViewHolder(
            LayoutItemAssigneeSelectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    companion object {
        private val DIFF_CALLBACK = object :
            DiffUtil.ItemCallback<AllCeibroConnections.CeibroConnection>() {
            override fun areItemsTheSame(
                oldItem: AllCeibroConnections.CeibroConnection,
                newItem: AllCeibroConnections.CeibroConnection
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: AllCeibroConnections.CeibroConnection,
                newItem: AllCeibroConnections.CeibroConnection
            ): Boolean {
                return oldItem == newItem
            }
        }

    }

    override fun onBindViewHolder(holder: AssigneeSelectionListViewHolder, position: Int) {
        val connectionObj: AllCeibroConnections.CeibroConnection? = getItem(position)
        holder.bind(connectionObj)
    }


//    fun setList(list: List<AllCeibroConnections.CeibroConnection>) {
//        this.dataList.clear()
//        this.dataList.addAll(list)
//        notifyDataSetChanged()
//    }

    inner class AssigneeSelectionListViewHolder(private val binding: LayoutItemAssigneeSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllCeibroConnections.CeibroConnection?) {

//            binding.contactCheckBox.isChecked = item?.isChecked ?: false
            binding.root.setOnClickListener {
//                item.isChecked = !item.isChecked
//                dataList[absoluteAdapterPosition].isChecked = item.isChecked
//                notifyDataSetChanged()
//                itemClickListener?.invoke(it, position, item)
            }

            binding.contactName.text = "${item?.contactFirstName} ${item?.contactSurName}"

            /*val phoneNumberUtil = PhoneNumberUtil.getInstance()
            var formattedNumber = ""
            if (item?.phoneNumber != null) {
                val parsedNumber = phoneNumberUtil.parse(item.phoneNumber, null)
                formattedNumber =
                    phoneNumberUtil.format(
                        parsedNumber,
                        PhoneNumberUtil.PhoneNumberFormat.INTERNATIONAL
                    )
            } else {
                formattedNumber = "N/A/N"
                println("NullNumber: ${item?.contactFirstName} ${item?.contactSurName}")
            }

            if (item?.isCeiborUser == true) {
                binding.phoneNumber.text = ""
                binding.companyName.text =
                    if (item.userCeibroData?.companyName.equals("")) {
                        "N/A"
                    }
                    else {
                        item.userCeibroData?.companyName
                    }
                binding.jobTitle.text =
                    if (item.userCeibroData?.jobTitle.equals("")) {
                        "N/A"
                    }
                    else {
                        item.userCeibroData?.jobTitle
                    }
                binding.companyName.visibility = View.VISIBLE
                binding.dot.visibility = View.VISIBLE
                binding.jobTitle.visibility = View.VISIBLE
            }
            else {
                binding.phoneNumber.text = formattedNumber
                binding.companyName.visibility = View.GONE
                binding.dot.visibility = View.GONE
                binding.jobTitle.visibility = View.GONE
            }



            if (item?.isCeiborUser == true) {
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
                if (item?.contactFirstName?.isNotEmpty() == true) {
                    initials += item.contactFirstName[0].uppercaseChar()
                }
                if (item?.contactSurName?.isNotEmpty() == true) {
                    initials += item.contactSurName[0].uppercaseChar()
                }

                binding.contactInitials.text = initials
            }*/
        }
    }
}