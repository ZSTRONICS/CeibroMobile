package com.zstronics.ceibro.ui.projectv2.allprojectsv2


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.repos.chat.room.Project
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutProjectItemListBinding
import javax.inject.Inject

class AllProjectAdapter @Inject constructor(val sessionManager: SessionManager) :
    RecyclerView.Adapter<AllProjectAdapter.AllProjectViewHolder>() {
    var callback: ((Int) -> Unit)? = null
    var fav: Boolean = true
    private var list: MutableList<Project> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllProjectViewHolder {
        return AllProjectViewHolder(
            LayoutProjectItemListBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllProjectViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: MutableList<Project>, fav: Boolean) {
        this.fav = fav
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllProjectViewHolder(private val binding: LayoutProjectItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Project) {
            val context = binding.root.context

            binding.tvDate.text = context.getString(R.string.sample_date)
            binding.projectName.text = item.title
            binding.userCompany.text =  context.getString(R.string.ceibro)
            binding.userName.text = context.getString(R.string.username)


            binding.connectionImg.setOnClickListener {
                callback?.invoke(1)
            }
            binding.llProjectDetail.setOnClickListener {
                callback?.invoke(1)
            }
            binding.ivHide.setOnClickListener {
                callback?.invoke(2)
            }
            if (fav) {
                binding.ivFav.visibility = View.VISIBLE
            } else {
                binding.ivFav.visibility = View.GONE
            }

        }
    }

    fun setCallBack(callback: (Int) -> Unit) {
        this.callback = callback
    }
}