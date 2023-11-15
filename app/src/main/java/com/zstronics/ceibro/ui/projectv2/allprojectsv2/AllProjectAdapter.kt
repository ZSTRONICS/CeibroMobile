package com.zstronics.ceibro.ui.projectv2.allprojectsv2


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutProjectItemListBinding
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class AllProjectAdapter @Inject constructor(val sessionManager: SessionManager) :
    RecyclerView.Adapter<AllProjectAdapter.AllProjectViewHolder>() {
    var callback: ((Pair<Int, CeibroProjectV2>) -> Unit)? = null
    var fav: Boolean = true
    private var list: MutableList<CeibroProjectV2> = mutableListOf()

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

    fun setList(list: MutableList<CeibroProjectV2>, fav: Boolean) {
        this.fav = fav
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllProjectViewHolder(private val binding: LayoutProjectItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CeibroProjectV2) {
            val context = binding.root.context

            binding.tvDate.text = DateUtils.formatCreationUTCTimeToCustom(
                utcTime = item.createdAt,
                inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
            )
            binding.projectName.text = item.title
            binding.userCompany.text = item.creator.companyName
            binding.userName.text = "${item.creator.firstName} ${item.creator.firstName}"


            binding.connectionImg.setOnClickListener {
                val pairToPass = Pair(1, item) // Replace this with your actual Pair values
                callback?.invoke(pairToPass)

            }
            binding.llProjectDetail.setOnClickListener {
                val pairToPass = Pair(1, item) // Replace this with your actual Pair values
                callback?.invoke(pairToPass)
            }
            binding.ivHide.setOnClickListener {
                val pairToPass =
                    Pair(2, item) // Replace this with your actual Pair values
                callback?.invoke(pairToPass)
            }
            if (fav) {
                binding.ivFav.visibility = View.VISIBLE
            } else {
                binding.ivFav.visibility = View.GONE
            }

        }
    }

    fun setCallBack(callback: (Pair<Int, CeibroProjectV2>) -> Unit) {

        this.callback = callback
    }
}