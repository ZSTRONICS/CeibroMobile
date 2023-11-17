package com.zstronics.ceibro.ui.projectv2.allprojectsv2

import android.content.Context
import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.intrusoft.sectionedrecyclerview.SectionRecyclerViewAdapter
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.databinding.LayoutItemHeaderBinding
import com.zstronics.ceibro.databinding.LayoutProjectItemListBinding
import com.zstronics.ceibro.utils.DateUtils

class AllProjectsAdapterSectionRecycler constructor(
    val context: Context,
    sectionList: MutableList<ProjectsSectionHeader>
) :
    SectionRecyclerViewAdapter<
            ProjectsSectionHeader,
            CeibroProjectV2,
            AllProjectsAdapterSectionRecycler.ConnectionsSectionViewHolder,
            AllProjectsAdapterSectionRecycler.ConnectionsChildViewHolder>(
        context,
        sectionList
    ) {

    var itemClickListener: ((view: View, position: Int, data: CeibroProjectV2, tag: String) -> Unit)? =
        null

    fun setCallBack(itemClickListener: ((view: View, position: Int, data: CeibroProjectV2, tag: String) -> Unit)?) {
        this.itemClickListener = itemClickListener

    }

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

    override fun onBindSectionViewHolder(
        connectionsSectionViewHolder: ConnectionsSectionViewHolder?,
        sectionPosition: Int,
        connectionsSectionHeader: ProjectsSectionHeader?
    ) {
        connectionsSectionViewHolder?.bind(connectionsSectionHeader)
    }

    override fun onCreateChildViewHolder(
        childViewGroup: ViewGroup?,
        viewType: Int
    ): ConnectionsChildViewHolder {
        return ConnectionsChildViewHolder(
            LayoutProjectItemListBinding.inflate(
                LayoutInflater.from(context),
                childViewGroup,
                false
            )
        )
    }

    override fun onBindChildViewHolder(
        holder: ConnectionsChildViewHolder?,
        sectionPosition: Int,
        childPostitoin: Int,
        data: CeibroProjectV2?
    ) {
        holder?.bind(data, sectionPosition)
    }


    inner class ConnectionsSectionViewHolder constructor(private val binding: LayoutItemHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProjectsSectionHeader?) {
            binding.headerTitle.text = item?.getSectionText()
            binding.headerTitle.textSize = 14f
            binding.headerTitle.setTextColor(context.getColor(R.color.appGrey3))

            if (item?.childItems.isNullOrEmpty()) {
                binding.headerTitle.visibility = View.GONE
            } else {
                binding.headerTitle.visibility = View.VISIBLE
            }
        }
    }

    inner class ConnectionsChildViewHolder constructor(val binding: LayoutProjectItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CeibroProjectV2?, sectionPosition: Int) {

            item?.let {
                with(binding) {

                    if (item.isFavoriteByMe) {
                        ivHide.visibility = View.GONE
                        ivFav.setImageResource(R.drawable.icon_star_filled)
                    } else {
                        ivHide.visibility = View.VISIBLE
                        ivFav.setImageResource(R.drawable.icon_star_outline)
                    }

                    projectName.text = item.title
                    tvDate.text = DateUtils.formatCreationUTCTimeToCustom(
                        utcTime = item.createdAt,
                        inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
                    )

                    if (item.creator.firstName.trim().isEmpty() && item.creator.surName.trim()
                            .isEmpty()
                    ) {
                        binding.userName.text = ""
                        binding.userName.visibility = View.GONE
                    } else {
                        binding.userName.text = "${item.creator.firstName} ${item.creator.surName}"
                    }

                    if (item.creator.companyName.isNullOrEmpty()) {
                        binding.userCompany.text = ""
                        binding.userCompany.visibility = View.GONE
                    } else {
                        binding.userCompany.text = item.creator.companyName
                    }

//                    if (item.projectPic.isNotEmpty()) {
                        val circularProgressDrawable = CircularProgressDrawable(context)
                        circularProgressDrawable.strokeWidth = 5f
                        circularProgressDrawable.centerRadius = 30f
                        circularProgressDrawable.start()

                        val requestOptions = RequestOptions()
                            .placeholder(circularProgressDrawable)
                            .error(R.drawable.app_icon)
                            .skipMemoryCache(true)
                            .centerCrop()

                        Glide.with(context)
                            .load(item.projectPic)
                            .apply(requestOptions)
                            .listener(object : RequestListener<Drawable> {
                                override fun onLoadFailed(
                                    e: GlideException?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    circularProgressDrawable.stop()
                                    return false
                                }
                                override fun onResourceReady(
                                    resource: Drawable?,
                                    model: Any?,
                                    target: Target<Drawable>?,
                                    dataSource: DataSource?,
                                    isFirstResource: Boolean
                                ): Boolean {
                                    circularProgressDrawable.stop()
                                    return false
                                }
                            })
                            .transition(DrawableTransitionOptions.withCrossFade())
                            .into(projectImg)
//                    }

                    root.setOnClickListener {
                        itemClickListener?.invoke(it, sectionPosition, item, "detail")
                    }
                    ivHide.setOnClickListener {
                        itemClickListener?.invoke(it, sectionPosition, item, "hide")
                    }
                    ivFav.setOnClickListener {
                        itemClickListener?.invoke(it, sectionPosition, item, "favorite")
                    }

                }
            }
        }
    }

}