package com.zstronics.ceibro.ui.admin

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.dashboard.admins.AdminUserObj
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutAdminUserBoxBinding
import javax.inject.Inject


class AdminAndUserAdapter @Inject constructor(val sessionManager: SessionManager) :
    RecyclerView.Adapter<AdminAndUserAdapter.AllAdminAndUserViewHolder>() {
    val user = sessionManager.getUser().value
    var itemClickListener: ((view: View, position: Int, data: AdminUserObj) -> Unit)? = null
    var itemLongClickListener: ((view: View, position: Int, data: AdminUserObj) -> Unit)? =
        null
    var childItemClickListener: ((view: View, position: Int, data: AdminUserObj) -> Unit)? = null
    var optionMenuItemClickListener: ((view: View, position: Int, data: AdminUserObj) -> Unit)? = null

    private var list: MutableList<AdminUserObj> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllAdminAndUserViewHolder {
        return AllAdminAndUserViewHolder(
            LayoutAdminUserBoxBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllAdminAndUserViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<AdminUserObj>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllAdminAndUserViewHolder(private val binding: LayoutAdminUserBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AdminUserObj) {
            val context = binding.root.context
            binding.allUsers = item

            if (item.profilePic == "" || item.profilePic.isNullOrEmpty()) {
                binding.adminUserImgText.text =
                    "${item.firstName[0].uppercaseChar()}${
                        item.surName[0].uppercaseChar()
                    }"
                binding.adminUserImgText.visibility = View.VISIBLE
                binding.adminUserImg.visibility = View.GONE
            } else {
                Glide.with(binding.adminUserImg.context)
                    .load(item.profilePic)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.profile_img)
                    .into(binding.adminUserImg)
                binding.adminUserImg.visibility = View.VISIBLE
                binding.adminUserImgText.visibility = View.GONE
            }

            val stringID = item.id
            println("stringID: $stringID")
            binding.adminUserID.text = ""
            if (stringID.length > 2) {
                val firstTwoChar: String = stringID.substring(0, 2)
                val lastTwoChar: String = stringID.substring(Math.max(stringID.length - 2, 0))

                binding.adminUserID.text = "$firstTwoChar..$lastTwoChar"
            }

            binding.optionMenu.setOnClickListener {
                optionMenuItemClickListener?.invoke(it, absoluteAdapterPosition, item)
            }


        }
    }
}