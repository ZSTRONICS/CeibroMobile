package com.zstronics.ceibro.ui.projectv2.hiddenprojectv2


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
import com.zstronics.ceibro.R
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.databinding.LayoutProjectItemListBinding
import com.zstronics.ceibro.utils.DateUtils
import javax.inject.Inject

class HiddenProjectAdapter @Inject constructor(val sessionManager: SessionManager) :
    RecyclerView.Adapter<HiddenProjectAdapter.AllProjectViewHolder>() {
    var callback: ((Pair<String, CeibroProjectV2>) -> Unit)? = null

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

    fun setList(list: MutableList<CeibroProjectV2>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }

    inner class AllProjectViewHolder(private val binding: LayoutProjectItemListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: CeibroProjectV2) {
            val context = binding.root.context
            binding.ivHide.setImageResource(R.drawable.icon_visibility_on)
            binding.ivFav.visibility = View.GONE
            binding.tvDate.text = DateUtils.formatCreationUTCTimeToCustom(
                utcTime = item.createdAt,
                inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
            )
            binding.projectName.text = item.title

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


//            if (item.projectPic.isNotEmpty()) {
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
                    .into(binding.projectImg)
//            }

            binding.root.setOnClickListener {
                val pairToPass = Pair("root", item) // Replace this with your actual Pair values
                callback?.invoke(pairToPass)
            }

            binding.ivHide.setOnClickListener {
                val pairToPass = Pair("unHide", item) // Replace this with your actual Pair values
                callback?.invoke(pairToPass)
            }

//            if (item.isHiddenByMe) {
//                binding.ivFav.visibility = View.GONE
//            } else {
//                binding.ivFav.visibility = View.VISIBLE
//            }

        }
    }

    fun setCallBack(callback: (Pair<String, CeibroProjectV2>) -> Unit) {
        this.callback = callback
    }
}