package com.zstronics.ceibro.ui.dashboard.myconnectionsv2

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
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
                binding.phoneNumber.visibility = View.GONE

                if (item.userCeibroData?.companyName?.trim().isNullOrEmpty()) {
                    binding.companyName.visibility = View.GONE
                    binding.dot.visibility = View.GONE
                } else {
                    binding.companyName.visibility = View.VISIBLE
                    binding.companyName.text = item.userCeibroData?.companyName?.trim()
                }

                if (item.userCeibroData?.jobTitle?.trim().isNullOrEmpty()) {
                    binding.dot.visibility = View.GONE
                    binding.jobTitle.visibility = View.GONE
                } else {
                    if (!item.userCeibroData?.companyName?.trim().isNullOrEmpty()) {
                        binding.dot.visibility = View.VISIBLE
                    }
                    binding.jobTitle.visibility = View.VISIBLE
                    binding.jobTitle.text = item.userCeibroData?.jobTitle?.trim()
                }

            } else {
                binding.phoneNumber.text = item.phoneNumber
                binding.phoneNumber.visibility = View.VISIBLE
                binding.companyName.visibility = View.GONE
                binding.dot.visibility = View.GONE
                binding.jobTitle.visibility = View.GONE
            }
        }
    }
}

class CeibroConnectionsDiffCallback(
    private val oldList: List<AllCeibroConnections.CeibroConnection>,
    private val newList: List<AllCeibroConnections.CeibroConnection>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        // Assuming your item has an identifier, compare the identifiers to check if the items are the same.
        return oldList[oldPosition].id == newList[newPosition].id
    }

    override fun areContentsTheSame(oldPosition: Int, newPosition: Int): Boolean {
        // If the items have the same identifier, compare the contents of the items to check if they are the same.
        // In this case, you can consider the lists as being equal if the items are equal.
        return oldList[oldPosition].id == newList[newPosition].id
    }
}
