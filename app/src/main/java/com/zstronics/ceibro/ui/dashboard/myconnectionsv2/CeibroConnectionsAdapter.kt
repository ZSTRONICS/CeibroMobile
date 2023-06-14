package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.LayoutItemConnectionBinding
import javax.inject.Inject

class CeibroConnectionsAdapter @Inject constructor() :
    RecyclerView.Adapter<CeibroConnectionsAdapter.CeibroConnectionsViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: AllCeibroConnections.CeibroConnection) -> Unit)? =
        null
    var listItems: MutableList<AllCeibroConnections.CeibroConnection> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CeibroConnectionsViewHolder {
        return CeibroConnectionsViewHolder(
            LayoutItemConnectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: CeibroConnectionsViewHolder, position: Int) {
        holder.bind(listItems[position])
    }

    override fun getItemCount(): Int {
        return listItems.size
    }

    fun setList(list: List<AllCeibroConnections.CeibroConnection>) {
        this.listItems.clear()
        this.listItems.addAll(list)
        notifyDataSetChanged()
    }

    inner class CeibroConnectionsViewHolder(private val binding: LayoutItemConnectionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllCeibroConnections.CeibroConnection) {
            binding.root.setOnClickListener {
                itemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }
            val context = binding.ceibroLogo.context

            binding.ceibroConnection = item
            when {
                item.isCeiborUser && !item.isBlocked -> {
                    ImageViewCompat.setImageTintList(
                        binding.ceibroLogo,
                        ColorStateList.valueOf(context.resources.getColor(R.color.appYellow))
                    )
                }
                item.isCeiborUser && item.isBlocked -> {
                    ImageViewCompat.setImageTintList(
                        binding.ceibroLogo,
                        ColorStateList.valueOf(context.resources.getColor(R.color.appRed))
                    )
                }
                else -> {
                    ImageViewCompat.setImageTintList(
                        binding.ceibroLogo,
                        ColorStateList.valueOf(context.resources.getColor(R.color.appGrey))
                    )
                }
            }
            binding.contactName.text = if (item.contactFullName.isNullOrEmpty()) {
                "${item.contactFirstName} ${item.contactSurName}"
            } else {
                "${item.contactFullName}"
            }

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

            if (item.isCeiborUser) {
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
                binding.phoneNumber.text = item.phoneNumber
                binding.companyName.visibility = View.GONE
                binding.dot.visibility = View.GONE
                binding.jobTitle.visibility = View.GONE
            }
        }
    }
}