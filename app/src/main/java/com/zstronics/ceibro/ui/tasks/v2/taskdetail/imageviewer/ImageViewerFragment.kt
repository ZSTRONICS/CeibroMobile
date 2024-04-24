package com.zstronics.ceibro.ui.tasks.v2.taskdetail.imageviewer

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import android.widget.TextView
import androidx.fragment.app.viewModels
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.databinding.FragmentImageViewerBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
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
        when (id) {
            R.id.closeBtn -> navigateBack()
        }
    }

    private var activeAdapter = ""
    private var index = 0


    @Inject
    lateinit var imagePagerAdapter: ImagePagerAdapter

    @Inject
    lateinit var localImagePagerAdapter: LocalImagePagerAdapter

    @Inject
    lateinit var detailsImagePagerAdapter: DetailsImagePagerAdapter


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.viewPager.adapter = imagePagerAdapter
        mViewDataBinding.localViewPager.adapter = localImagePagerAdapter
        mViewDataBinding.detailViewPager.adapter = detailsImagePagerAdapter


        mViewDataBinding.ivMenu.setOnClick {
            replyPopUp(mViewDataBinding.ivMenu) {
                when (activeAdapter) {
                    "images" -> {
                        val item =
                            imagePagerAdapter.listItems[imagePagerAdapter.currentVisibleIndex]
                        navigateBack()
                        Handler(Looper.getMainLooper()).postDelayed({
                            EventBus.getDefault().post(LocalEvents.ImageFile(item, it))
                        }, 100)
                    }

                    "localImages" -> {
                        val item =
                            localImagePagerAdapter.listItems[localImagePagerAdapter.currentVisibleIndex]
                        navigateBack()
                        item.fileUri?.let {
                            EventBus.getDefault().post(LocalEvents.ImageUri(it))
                        }
                    }

                    "detailViewImages" -> {
                        val item =
                            detailsImagePagerAdapter.listItems[detailsImagePagerAdapter.currentVisibleIndex]
                        navigateBack()
                        EventBus.getDefault().post(LocalEvents.LocalImageFile(item, it))
                    }
                }

            }
        }

        viewModel.images.observe(viewLifecycleOwner) {
            if (it != null) {
                activeAdapter = "images"
                mViewDataBinding.detailViewPager.visibility = View.GONE
                mViewDataBinding.localViewPager.visibility = View.GONE
                mViewDataBinding.viewPager.visibility = View.VISIBLE
                imagePagerAdapter.setList(it)
                val handler = Handler()
                handler.postDelayed(Runnable {
                    mViewDataBinding.viewPager.setCurrentItem(viewModel.imagePosition, true)
                }, 60)
            }
        }

        viewModel.localImages.observe(viewLifecycleOwner) {
            if (it != null) {
                activeAdapter = "localImages"
                mViewDataBinding.viewPager.visibility = View.GONE
                mViewDataBinding.detailViewPager.visibility = View.GONE
                mViewDataBinding.localViewPager.visibility = View.VISIBLE
                localImagePagerAdapter.setList(it)
                val handler = Handler()
                handler.postDelayed(Runnable {
                    mViewDataBinding.localViewPager.setCurrentItem(viewModel.imagePosition, true)
                }, 60)
            }
        }

        viewModel.detailViewImages.observe(viewLifecycleOwner) {
            if (it != null) {
                activeAdapter = "detailViewImages"
                mViewDataBinding.viewPager.visibility = View.GONE
                mViewDataBinding.localViewPager.visibility = View.GONE
                mViewDataBinding.detailViewPager.visibility = View.VISIBLE
                detailsImagePagerAdapter.setList(it)
                val handler = Handler()
                handler.postDelayed(Runnable {
                    mViewDataBinding.detailViewPager.setCurrentItem(viewModel.imagePosition, true)
                }, 60)
            }
        }
    }

    private fun replyPopUp(
        v: View,
        callback: (String) -> Unit
    ): PopupWindow {
        val context: Context = v.context
        val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.image_reply_dialog, null)

        val popupWindow = PopupWindow(
            view,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            true
        )
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        popupWindow.elevation = 13F
        popupWindow.isOutsideTouchable = true

        val reply: TextView = view.findViewById(R.id.reply)
        val editReply: TextView = view.findViewById(R.id.editReply)

        reply.setOnClick {
            callback.invoke("reply")
            popupWindow.dismiss()
        }

        editReply.setOnClick {

            callback.invoke("editReply")
            popupWindow.dismiss()
        }

        val values = IntArray(2)
        v.getLocationInWindow(values)
        val positionOfIcon = values[1]

        //Get the height of 2/3rd of the height of the screen
        val displayMetrics = context.resources.displayMetrics
        val height = displayMetrics.heightPixels * 2 / 3

        if (positionOfIcon > height) {
            popupWindow.showAsDropDown(v, 0, -180)
        } else {
            popupWindow.showAsDropDown(v, 0, -30)
        }
        return popupWindow
    }

}