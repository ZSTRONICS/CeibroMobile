package com.zstronics.ceibro.ui.projects.newproject.overview.ownersheet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutOwnerBinding
import javax.inject.Inject

class OwnerSelectionAdapter @Inject constructor() :
    RecyclerView.Adapter<OwnerSelectionAdapter.OwnerSelectionViewHolder>() {
    var itemClickListener: ((view: View, position: Int, data: String) -> Unit)? = null

    private var list: MutableList<MyConnection> = mutableListOf()
    var selectedConnections: ArrayList<String> = arrayListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OwnerSelectionViewHolder {
        return OwnerSelectionViewHolder(
            LayoutOwnerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: OwnerSelectionViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<MyConnection>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    fun setSelectedConnection(list: List<String>) {
        this.selectedConnections.clear()
        this.selectedConnections.addAll(list)
        notifyDataSetChanged()
    }

    inner class OwnerSelectionViewHolder(private val binding: LayoutOwnerBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MyConnection) {
            val context = binding.root.context
            binding.allConnections = item
            var connectionId: String? = ""

            if (item.isEmailInvite) {                //If email invite is true then it means user is not registered in ceibro and only email will be displayed

                binding.connectionPendingStatus.visibility = View.VISIBLE

                binding.connectionImgText.text = "${item.email[0].uppercaseChar()}"

                binding.connectionUserName.text = "${item.email}"
                binding.connectionUserCompany.text = "No company added"

            } else if (item.sentByMe) {                //It means if it's true, then i've invited the user and i'll get other user's data from "To" parameter
                val userToObj =
                    item.to             //Getting data from "To" class. because in "From" that's our user object, as we sent the request
                connectionId = userToObj.id
                binding.connectionPendingStatus.visibility = View.GONE

                binding.connectionImgText.text = ""
                if (userToObj.profilePic == "" || userToObj.profilePic.isNullOrEmpty()) {
                    binding.connectionImgText.text =
                        "${userToObj.firstName[0]?.uppercaseChar()}${
                            userToObj.surName[0]?.uppercaseChar()
                        }"
                    binding.connectionImgText.visibility = View.VISIBLE
                    binding.connectionImg.visibility = View.GONE
                } else {
                    Glide.with(binding.connectionImg.context)
                        .load(userToObj.profilePic)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.profile_img)
                        .into(binding.connectionImg)
                    binding.connectionImg.visibility = View.VISIBLE
                    binding.connectionImgText.visibility = View.GONE
                }

                binding.connectionUserName.text = "${userToObj.firstName} ${userToObj.surName}"
                binding.connectionUserCompany.text = "${userToObj.companyName}"

                if (userToObj.companyName == "" || userToObj.companyName.isNullOrEmpty()) {
                    binding.connectionUserCompany.text = "No company added"
                }

            } else {
                val userFromObj =
                    item.from             //Getting data from "From" class. because in "To" that's our user object, as we received the request
                connectionId = userFromObj?.id
                binding.connectionPendingStatus.visibility = View.GONE
                binding.connectionImgText.text = ""
                if (userFromObj?.profilePic == "" || userFromObj?.profilePic.isNullOrEmpty()) {
                    binding.connectionImgText.text = "${
                        userFromObj?.firstName?.get(0)?.uppercaseChar()
                    }${userFromObj?.surName?.get(0)?.uppercaseChar()}"
                    binding.connectionImgText.visibility = View.VISIBLE
                    binding.connectionImg.visibility = View.GONE
                } else {
                    Glide.with(binding.connectionImg.context)
                        .load(userFromObj?.profilePic)
                        .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                        .placeholder(R.drawable.profile_img)
                        .into(binding.connectionImg)
                    binding.connectionImg.visibility = View.VISIBLE
                    binding.connectionImgText.visibility = View.GONE
                }

                binding.connectionUserName.text =
                    "${userFromObj?.firstName} ${userFromObj?.surName}"
                binding.connectionUserCompany.text = "${userFromObj?.companyName}"

                if (userFromObj?.companyName == "" || userFromObj?.companyName.isNullOrEmpty()) {
                    binding.connectionUserCompany.text = "No company added"
                }

            }

            with(binding.connectionRadio) {
                isChecked = selectedConnections.find { it == connectionId } != null
                binding.mainLayout.setOnClickListener {
                    connectionId?.let { it1 ->
                        itemClickListener?.invoke(
                            it, absoluteAdapterPosition,
                            it1
                        )
                    }
                    notifyDataSetChanged()
                }
            }

        }
    }
}