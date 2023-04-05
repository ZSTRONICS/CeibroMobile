package com.zstronics.ceibro.ui.projects.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.toCamelCase
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutProjectBoxBinding
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class AllProjectsAdapter @Inject constructor(val sessionManager: SessionManager) :
    RecyclerView.Adapter<AllProjectsAdapter.AllProjectsViewHolder>() {
    val user = sessionManager.getUser().value
    var itemClickListener: ((view: View, position: Int, data: AllProjectsResponse.Projects) -> Unit)? =
        null
    var itemLongClickListener: ((view: View, position: Int, data: AllProjectsResponse.Projects) -> Unit)? =
        null
    var childItemClickListener: ((view: View, position: Int, data: AllProjectsResponse.Projects) -> Unit)? =
        null

    private var list: MutableList<AllProjectsResponse.Projects> = mutableListOf()
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllProjectsViewHolder {
        return AllProjectsViewHolder(
            LayoutProjectBoxBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: AllProjectsViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun setList(list: List<AllProjectsResponse.Projects>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllProjectsViewHolder(private val binding: LayoutProjectBoxBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: AllProjectsResponse.Projects) {
            val context = binding.root.context
            //val otherUser: Member? = item.members.find { member -> member.id != user?.id }
            binding.allProjects = item

            binding.projectOwnerNameText.text = if (item.owner.size > 1)
                "${item.owner[0].firstName} ${item.owner[0].surName}  +${item.owner.size - 1}"
            else
                "${item.owner[0].firstName} ${item.owner[0].surName}"

            binding.creatorNameText.text =
                if (item.creator != null)
                    "${item.creator.firstName} ${item.creator.surName}"
                else " - -"

            binding.projectDueDateText.text = DateUtils.reformatStringDate(
                date = item.dueDate,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR
            )
            if (binding.projectDueDateText.text == "") {                              // Checking if date format was not dd-MM-yyyy then still it is empty
                binding.projectDueDateText.text = DateUtils.reformatStringDate(
                    date = item.dueDate,
                    DateUtils.FORMAT_YEAR_MON_DATE,
                    DateUtils.FORMAT_SHORT_DATE_MON_YEAR
                )                                                         // Checking if date format was not yyyy-MM-dd then it will be empty
                if (binding.projectDueDateText.text == "") {
                    binding.projectDueDateText.text =
                        context.getString(R.string.general_text_nna)
                }
            }

            binding.creationDateText.text = DateUtils.reformatStringDate(
                date = item.createdAt,
                DateUtils.SERVER_DATE_FULL_FORMAT,
                DateUtils.FORMAT_SHORT_DATE_MON_YEAR
            )

            if (item.projectPhoto == "" || item.projectPhoto.isNullOrEmpty()) {
                binding.projectImg.setBackgroundResource(R.drawable.splash_background)
            } else {
                Glide.with(binding.projectImg.context)
                    .load(item.projectPhoto)
                    .diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
                    .placeholder(R.drawable.splash_background)
                    .into(binding.projectImg)
            }
            if (item.publishStatus.isNullOrEmpty()) {
                binding.projectStatusName.text = context.getString(R.string.default_status_text)
            } else {
                binding.projectStatusName.text = item.publishStatus.toCamelCase()
            }
            when {
                item.publishStatus?.lowercase() == "done" ||
                        item.publishStatus?.lowercase() == "complete" ||
                        item.publishStatus?.lowercase() == "completed" ||
                        item.publishStatus?.lowercase() == "finish" ||
                        item.publishStatus?.lowercase() == "testing" ||
                        item.publishStatus?.lowercase() == "finished" -> {
                    binding.projectCardLayout.setBackgroundResource(R.drawable.status_done_outline)
                    binding.projectStatusName.background =
                        context.getDrawable(R.drawable.status_done_filled)
                }
                else -> {
                    binding.projectCardLayout.setBackgroundResource(R.drawable.status_draft_outline)
                    binding.projectStatusName.background =
                        context.getDrawable(R.drawable.status_draft_filled)
                }
            }

            itemView.setOnClickListener {
                itemClickListener?.invoke(it, adapterPosition, item)
            }
        }
    }
}