package com.zstronics.ceibro.ui.admin.personaldetail

import android.content.Intent
import androidx.fragment.app.viewModels
import com.github.tntkhang.fullscreenimageview.library.FullScreenImageViewActivity
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentWorksBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class PersonalDetailFragment :
    BaseNavViewModelFragment<FragmentWorksBinding, IPersonalDetail.State, PersonalDetailVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: PersonalDetailVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_personal_detail
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
            R.id.ImgCard -> {
                val fullImageIntent = Intent(
                    requireContext(),
                    FullScreenImageViewActivity::class.java
                )

                val uriString: ArrayList<String> = arrayListOf()
                uriString.add(viewState.userProfilePic.value.toString())
                fullImageIntent.putExtra(FullScreenImageViewActivity.URI_LIST_DATA, uriString)

                fullImageIntent.putExtra(
                    FullScreenImageViewActivity.IMAGE_FULL_SCREEN_CURRENT_POS, 0)
                startActivity(fullImageIntent)
            }
        }
    }
}