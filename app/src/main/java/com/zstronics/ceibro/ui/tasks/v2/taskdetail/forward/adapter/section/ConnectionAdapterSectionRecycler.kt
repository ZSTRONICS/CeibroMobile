package com.zstronics.ceibro.ui.tasks.v2.taskdetail.forward.adapter.section

import android.Manifest
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.ceibro.permissionx.PermissionX
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.cancelAndMakeToast
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.data.repos.dashboard.connections.v2.AllCeibroConnections
import com.zstronics.ceibro.databinding.LayoutItemAssigneeSelectionBinding
import com.zstronics.ceibro.databinding.LayoutItemHeaderBinding

class ConnectionAdapterSectionRecycler constructor(
    val context: Context,
    val sectionList: MutableList<ConnectionsSectionHeader>
) :
    SectionRecyclerViewAdapter<ConnectionsSectionHeader, AllCeibroConnections.CeibroConnection, ConnectionAdapterSectionRecycler.ConnectionsSectionViewHolder, ConnectionAdapterSectionRecycler.ConnectionsChildViewHolder>(
        context,
        sectionList
    ) {
    var showContactPermissionToast = true
    var isConfirmer = false
    var isViewer = false

    var itemClickListener: ((view: View, position: Int, data: AllCeibroConnections.CeibroConnection) -> Unit)? =
        null

    var fullItemClickListener: ((view: View, position: Int, data: AllCeibroConnections.CeibroConnection) -> Unit)? =
        null
    var dataList: MutableList<AllCeibroConnections.CeibroConnection> = mutableListOf()
    var disableContscts: MutableList<AllCeibroConnections.CeibroConnection> = mutableListOf()
    var oldContacts: ArrayList<String> = arrayListOf()

    override fun onCreateSectionViewHolder(
        sectionViewGroup: ViewGroup?,
        viewType: Int
    ): ConnectionsSectionViewHolder {
        return ConnectionsSectionViewHolder(
            LayoutItemHeaderBinding.inflate(
                LayoutInflater.from(context),
                sectionViewGroup,
                false
            )
        )
    }

    override fun onCreateChildViewHolder(
        childViewGroup: ViewGroup?,
        viewType: Int
    ): ConnectionsChildViewHolder {
        return ConnectionsChildViewHolder(
            LayoutItemAssigneeSelectionBinding.inflate(
                LayoutInflater.from(context),
                childViewGroup,
                false
            )
        )
    }

    override fun onBindChildViewHolder(
        childViewHolder: ConnectionsChildViewHolder?,
        p1: Int,
        p2: Int,
        connectionItem: AllCeibroConnections.CeibroConnection?
    ) {
        childViewHolder?.bind(connectionItem)
    }

    override fun onBindSectionViewHolder(
        connectionsSectionViewHolder: ConnectionsSectionViewHolder?,
        p1: Int,
        connectionsSectionHeader: ConnectionsSectionHeader?
    ) {
        connectionsSectionViewHolder?.bind(connectionsSectionHeader)
    }

    fun setData(oldSelectedContacts: ArrayList<String>) {
        oldContacts.clear()
        oldContacts.addAll(oldSelectedContacts)
        oldContacts.size
        notifyDataSetChanged()
    }

    fun disableContacts(oldSelectedContacts: MutableList<AllCeibroConnections.CeibroConnection>) {
       disableContscts.clear()
        disableContscts = oldSelectedContacts
        notifyDataSetChanged()
    }

    inner class ConnectionsSectionViewHolder constructor(private val binding: LayoutItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ConnectionsSectionHeader?) {
            binding.headerTitle.text = item?.getSectionText()
            if (!isPermissionGranted(Manifest.permission.READ_CONTACTS) && showContactPermissionToast) {
                showContactPermissionToast = false
                context.shortToastNow(context.getString(R.string.contacts_permission))
            }

            if (!showContactPermissionToast) {
                return
            }
            if (item?.isDataLoading == true) {
                binding.noConnections.visibility = View.GONE
                binding.connectionsLoadingLayout.visibility = View.VISIBLE
            } else if (item?.childItems?.isEmpty() == true) {
                binding.connectionsLoadingLayout.visibility = View.GONE
                binding.noConnections.visibility = View.VISIBLE
            } else {
                binding.noConnections.visibility = View.GONE
                binding.connectionsLoadingLayout.visibility = View.GONE
            }

        }
    }

    inner class ConnectionsChildViewHolder constructor(val binding: LayoutItemAssigneeSelectionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AllCeibroConnections.CeibroConnection?) {
            val context = binding.root.context
            item?.let {
                with(binding) {
                    if (isConfirmer) {
                        contactCheckBox.visibility = View.GONE
                    } else {
                        contactCheckBox.visibility = View.VISIBLE
                    }
                    contactCheckBox.isChecked = item.isChecked
                    mainLayout.isEnabled = true
                    root.isEnabled = true
                    root.isClickable = true
                    root.alpha = 1.0f

                    contactInitials.setTextColor(context.resources.getColor(R.color.black))
                    contactName.setTextColor(context.resources.getColor(R.color.black))


                    contactCheckBox.setOnClickListener {
                        item.isChecked = !item.isChecked
                        notifyDataSetChanged()
                        itemClickListener?.invoke(it, position, item)
                    }
                    root.setOnClickListener {
                        val disable = disableContscts.find { contact -> contact.id == item.id }
                        if (isViewer && disable != null) {
                            cancelAndMakeToast(
                                context,
                                "This user is already selected as assignee/confirmer",
                                Toast.LENGTH_SHORT
                            )
                        } else if (disable != null) {
                            cancelAndMakeToast(
                                context,
                                "This user is already selected as a viewer",
                                Toast.LENGTH_SHORT
                            )
                        } else {
                            item.isChecked = !item.isChecked
                            notifyDataSetChanged()
                            fullItemClickListener?.invoke(it, position, item)
                        }
                    }

                    contactName.text = "${item.contactFirstName} ${item.contactSurName}"

                    if (item.isCeiborUser) {
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



                    if (item.isCeiborUser) {
                        if (item.userCeibroData?.profilePic.isNullOrEmpty()) {
                            contactInitials.visibility = View.VISIBLE
                            contactImage.visibility = View.GONE
                            var initials = ""
                            if (item.contactFirstName?.isNotEmpty() == true) {
                                initials += item.contactFirstName[0].uppercaseChar()
                            }
                            if (item.contactSurName?.isNotEmpty() == true) {
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
                        if (item.contactFirstName?.isNotEmpty() == true) {
                            initials += item.contactFirstName[0].uppercaseChar()
                        }
                        if (item.contactSurName?.isNotEmpty() == true) {
                            initials += item.contactSurName[0].uppercaseChar()
                        }

                        contactInitials.text = initials
                    }

                    val currentContact = oldContacts.find { it == item.phoneNumber }
                    if (!currentContact.isNullOrEmpty()) {
                        contactCheckBox.isChecked = true
                        mainLayout.isEnabled = false
                        contactCheckBox.isClickable = false
                        root.isEnabled = false
                        root.isClickable = false
                        root.alpha = 0.7f
                        contactInitials.setTextColor(context.resources.getColor(R.color.appGrey3))
                        contactName.setTextColor(context.resources.getColor(R.color.appGrey3))
                    }

                    val disable = disableContscts.find { it.id == item.id }
                    disable?.let {
                        contactCheckBox.isChecked = false
                        contactCheckBox.isClickable = false
//                        mainLayout.isEnabled = false
//                        root.isEnabled = false
//                        root.isClickable = false
                        root.alpha = 0.6f
                        contactInitials.setTextColor(context.resources.getColor(R.color.appGrey3))
                        contactName.setTextColor(context.resources.getColor(R.color.appGrey3))
                    }
                }
            }
        }
    }

    fun isPermissionGranted(permission: String): Boolean {
        return PermissionX.isGranted(context, permission)
    }
}