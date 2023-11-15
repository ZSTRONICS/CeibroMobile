package com.zstronics.ceibro.ui.tasks.v2.taskdetail.imageviewer

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentImageViewerBinding
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ImageViewerFragment :
    BaseNavViewModelFragment<FragmentImageViewerBinding, IImageViewer.State, ImageViewerVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ImageViewerVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_image_viewer
    override fun toolBarVisibility(): Boolean = false
    override fun onClick(id: Int) {
        when(id) {
            R.id.closeBtn -> navigateBack()
        }
    }


    @Inject
    lateinit var imagePagerAdapter: ImagePagerAdapter

    @Inject
    lateinit var localImagePagerAdapter: LocalImagePagerAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.viewPager.adapter = imagePagerAdapter
        mViewDataBinding.localViewPager.adapter = localImagePagerAdapter

        viewModel.images.observe(viewLifecycleOwner) {
            if (it != null) {
                mViewDataBinding.localViewPager.visibility = View.GONE
                mViewDataBinding.viewPager.visibility = View.VISIBLE
                imagePagerAdapter.setList(it)
                val handler = Handler()
                handler.postDelayed(Runnable {
                    mViewDataBinding.viewPager.setCurrentItem(viewModel.imagePosition, true)
                }, 50)
            }
        }

        viewModel.localImages.observe(viewLifecycleOwner) {
            if (it != null) {
                mViewDataBinding.viewPager.visibility = View.GONE
                mViewDataBinding.localViewPager.visibility = View.VISIBLE
                localImagePagerAdapter.setList(it)
                val handler = Handler()
                handler.postDelayed(Runnable {
                    mViewDataBinding.localViewPager.setCurrentItem(viewModel.imagePosition, true)
                }, 50)
            }
        }
    }

}