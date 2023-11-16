package com.zstronics.ceibro.ui.projectv2.projectdetailv2.projectinfo

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.database.models.projects.CeibroProjectV2
import com.zstronics.ceibro.databinding.FragmentProjectInfoV2Binding
import com.zstronics.ceibro.utils.DateUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ProjectInfoV2Fragment :
    BaseNavViewModelFragment<FragmentProjectInfoV2Binding, IProjectInfoV2.State, ProjectInfoV2VM>() {
    private var projectData: CeibroProjectV2? = null
    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectInfoV2VM by viewModels()
    override val layoutResId: Int = R.layout.fragment_project_info_v2
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {

        }
    }

    override fun onDetach() {
        CookiesManager.projectDataForDetails = null
        CookiesManager.projectNameForDetails = ""
        super.onDetach()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.projectData.observe(viewLifecycleOwner) {
            if (it != null) {

                viewState.projectImagegUrl.value = it.projectPic
                viewState.projectCreator.value = "${it.creator.firstName} ${it.creator.surName}"
                viewState.projectName.value = it.title
                viewState.projectdate.value = DateUtils.formatCreationUTCTimeToCustom(
                    utcTime = it.createdAt,
                    inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
                )

                if (it.description.isNotEmpty()) {
                    viewState.projectDescription.value = it.description
                } else {
                    viewState.projectDescription.value = it.description
                }
            }
        }
    }
}


//                    val circularProgressDrawable = CircularProgressDrawable(requireContext())
//                    circularProgressDrawable.strokeWidth = 5f
//                    circularProgressDrawable.centerRadius = 30f
//                    circularProgressDrawable.start()
//
//                    val requestOptions = RequestOptions()
//                        .placeholder(circularProgressDrawable)
//                        .error(R.drawable.project_img)
//                        .skipMemoryCache(true)
//                        .centerCrop()
//
//                    Glide.with(requireContext())
//                        .load(it.projectPic)
//                        .apply(requestOptions)
//                        .listener(object : RequestListener<Drawable> {
//                            override fun onLoadFailed(
//                                e: GlideException?,
//                                model: Any?,
//                                target: Target<Drawable>?,
//                                isFirstResource: Boolean
//                            ): Boolean {
//                                circularProgressDrawable.stop()
//                                return false
//                            }
//                            override fun onResourceReady(
//                                resource: Drawable?,
//                                model: Any?,
//                                target: Target<Drawable>?,
//                                dataSource: DataSource?,
//                                isFirstResource: Boolean
//                            ): Boolean {
//                                circularProgressDrawable.stop()
//                                return false
//                            }
//                        })
//                        .transition(DrawableTransitionOptions.withCrossFade())
//                        .into(mViewDataBinding.projectImg)
//                } else {
//                    mViewDataBinding.projectImg.setImageResource(R.drawable.project_img)
//                }
//
//                mViewDataBinding.tvCreator.text = "${it.creator.firstName} ${it.creator.surName}"
//                mViewDataBinding.tvProjectNamee.text = it.title
//                mViewDataBinding.tvCreationDate.text = DateUtils.formatCreationUTCTimeToCustom(
//                    utcTime = it.createdAt,
//                    inputFormatter = DateUtils.SERVER_DATE_FULL_FORMAT_IN_UTC
//                )
//
//                if (it.description.isNotEmpty()) {
//                    mViewDataBinding.tvProjectDescription.text = it.description
//                } else {
//                    mViewDataBinding.tvProjectDescription.text = getString(R.string.no_description_added)
//                }
//            }

