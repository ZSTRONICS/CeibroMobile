package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.base.BaseResponse
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.HeaderItem
import com.zstronics.ceibro.databinding.LayoutItemAssigneeSelectionBinding
import com.zstronics.ceibro.databinding.LayoutItemHeaderBinding
import javax.inject.Inject

class ForwardSelectionAdapter @Inject constructor() :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: AllCeibroConnections.CeibroConnection) -> Unit)? =
        null
    var dataList: MutableList<BaseResponse> = mutableListOf()
    var oldContacts: ArrayList<String> = arrayListOf()
    val ITEM_TYPE_HEADER = 0
    val ITEM_TYPE_CONNECTION = 1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            ITEM_TYPE_HEADER ->
                HeaderViewHolder(
                    LayoutItemHeaderBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
            else ->
                ForwardSelectionViewHolder(
                    LayoutItemAssigneeSelectionBinding.inflate(
                        LayoutInflater.from(parent.context),
                        parent,
                        false
                    )
                )
        }

    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is ForwardSelectionViewHolder)
            holder.bind(dataList[position])
        else if (holder is HeaderViewHolder)
            holder.bind(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    override fun getItemViewType(position: Int): Int {
        val item = dataList[position]
        return if (item is HeaderItem)
            ITEM_TYPE_HEADER
        else
            ITEM_TYPE_CONNECTION
    }

    fun setList(
        list: List<BaseResponse>,
        oldSelectedContacts: ArrayList<String>
    ) {
        this.dataList.clear()
        this.dataList.addAll(list)
        this.oldContacts.clear()
        this.oldContacts.addAll(oldSelectedContacts)
        notifyDataSetChanged()
    }

    inner class ForwardSelectionViewHolder(private val binding: LayoutItemAssigneeSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(itemParent: BaseResponse) {
            val item = itemParent as AllCeibroConnections.CeibroConnection
            val context = binding.contactName.context

            binding.contactCheckBox.isChecked = item.isChecked
            binding.mainLayout.isEnabled = true
            binding.root.isEnabled = true
            binding.root.isClickable = true
            binding.root.alpha = 1.0f
            binding.contactInitials.setTextColor(context.resources.getColor(R.color.black))
            binding.contactName.setTextColor(context.resources.getColor(R.color.black))

            binding.root.setOnClickListener {
                item.isChecked = !item.isChecked
                (dataList[absoluteAdapterPosition] as AllCeibroConnections.CeibroConnection).isChecked =
                    item.isChecked
                notifyDataSetChanged()
                itemClickListener?.invoke(it, position, item)
            }

            binding.contactName.text = "${item.contactFirstName} ${item.contactSurName}"

            if (item.isCeiborUser) {
                binding.phoneNumber.text = ""
                binding.companyName.text =
                    if (item.userCeibroData?.companyName.equals("")) {
                        "N/A"
                    } else {
                        item.userCeibroData?.companyName
                    }
                binding.jobTitle.text =
                    if (item.userCeibroData?.jobTitle.equals("")) {
                        "N/A"
                    } else {
                        item.userCeibroData?.jobTitle
                    }
                binding.companyName.visibility = View.VISIBLE
                binding.dot.visibility = View.VISIBLE
                binding.jobTitle.visibility = View.VISIBLE
            } else {
                binding.phoneNumber.text = item.phoneNumber
                binding.companyName.visibility = View.GONE
                binding.dot.visibility = View.GONE
                binding.jobTitle.visibility = View.GONE
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

            val currentContact = oldContacts.find { it == item.phoneNumber }
            if (!currentContact.isNullOrEmpty()) {
                binding.contactCheckBox.isChecked = true
                binding.mainLayout.isEnabled = false
                binding.root.isEnabled = false
                binding.root.isClickable = false
                binding.root.alpha = 0.7f
                binding.contactInitials.setTextColor(context.resources.getColor(R.color.appGrey3))
                binding.contactName.setTextColor(context.resources.getColor(R.color.appGrey3))
            }

        }
    }

    inner class HeaderViewHolder(private val binding: LayoutItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(itemParent: BaseResponse) {
            binding.headerTitle.text = (itemParent as HeaderItem).title
        }
    }
}