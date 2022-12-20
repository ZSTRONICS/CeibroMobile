package com.zstronics.ceibro.ui.invitations.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.dashboard.connections.MyConnection
import com.zstronics.ceibro.data.repos.dashboard.invites.MyInvitationsItem
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutConnectionBoxBinding
import com.zstronics.ceibro.databinding.LayoutInvitationsBoxBinding
import javax.inject.Inject

class AllInvitationsAdapter @Inject constructor(val sessionManager: SessionManager) :
    RecyclerView.Adapter<AllInvitationsAdapter.AllInvitationsViewHolder>() {
    val user = sessionManager.getUser().value
    var itemClickListener: ((view: View, position: Int, data: MyInvitationsItem) -> Unit)? = null
    var itemLongClickListener: ((view: View, position: Int, data: MyInvitationsItem) -> Unit)? =
        null
    var childItemClickListener: ((view: View, position: Int, data: MyInvitationsItem) -> Unit)? = null

    private var list: MutableList<MyInvitationsItem> = mutableListOf()
    var selectedPosition = -1
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllInvitationsViewHolder {
        return AllInvitationsViewHolder(
            LayoutInvitationsBoxBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllInvitationsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<MyInvitationsItem>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllInvitationsViewHolder(private val binding: LayoutInvitationsBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: MyInvitationsItem) {
            val context = binding.root.context
            binding.allInvitations = item

            binding.newInvitePersonName.text = "${item.from.firstName} ${item.from.surName}"

        }
    }
}