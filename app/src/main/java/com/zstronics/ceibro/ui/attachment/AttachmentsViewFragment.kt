package com.zstronics.ceibro.ui.attachment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import com.github.tntkhang.fullscreenimageview.library.FullScreenImageViewActivity
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.database.models.attachments.FilesAttachments
import com.zstronics.ceibro.databinding.FragmentAttachmentBinding
import com.zstronics.ceibro.ui.socket.LocalEvents
import dagger.hilt.android.AndroidEntryPoint
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject

@AndroidEntryPoint
class AttachmentsViewFragment :
    BaseNavViewModelFragment<FragmentAttachmentBinding, IAttachment.State, AttachmentVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: AttachmentVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_attachment
    override fun toolBarVisibility(): Boolean = false

    @Inject
    lateinit var attachmentAdapter: AttachmentViewAdapter

    override fun onClick(id: Int) {
        when (id) {
            R.id.backBtn -> navigateBack()
            R.id.attachmentAddBtn -> navigate(R.id.addAttachmentFragment, arguments)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mViewDataBinding.attachmentRecyclerView.adapter = attachmentAdapter

        viewModel.attachments.observe(viewLifecycleOwner) { list ->
            attachmentAdapter.setList(list)
        }

        viewModel.selectedTab.observe(viewLifecycleOwner) { selectedTab ->
            when (selectedTab) {
                "media" -> {
                    mViewDataBinding.attachmentMediaBtn.setTextColor(requireContext().getColor(R.color.appYellow))
                    mViewDataBinding.attachmentAll.setTextColor(requireContext().getColor(R.color.black))
                    mViewDataBinding.attachmentDocsBtn.setTextColor(requireContext().getColor(R.color.black))
                }
                "doc" -> {
                    mViewDataBinding.attachmentDocsBtn.setTextColor(requireContext().getColor(R.color.appYellow))
                    mViewDataBinding.attachmentMediaBtn.setTextColor(requireContext().getColor(R.color.black))
                    mViewDataBinding.attachmentAll.setTextColor(requireContext().getColor(R.color.black))
                }
                "all" -> {
                    mViewDataBinding.attachmentAll.setTextColor(requireContext().getColor(R.color.appYellow))
                    mViewDataBinding.attachmentDocsBtn.setTextColor(requireContext().getColor(R.color.black))
                    mViewDataBinding.attachmentMediaBtn.setTextColor(requireContext().getColor(R.color.black))
                }
            }
        }
        attachmentAdapter.itemClickListener =
            { _: View, position: Int, data: FilesAttachments? ->
                openFileViewer(data?.fileUrl, position)
            }
    }

    private fun openFileViewer(fileUrl: String?, position: Int) {
        if (fileUrl != null) {
            val fileExtension = fileUrl.substringAfterLast(".")
            if (imageExtensions.contains(".$fileExtension")) {
                val fullImageIntent = Intent(
                    requireContext(),
                    FullScreenImageViewActivity::class.java
                )

                val uriString: ArrayList<String> = arrayListOf()
                uriString.add(fileUrl)
                fullImageIntent.putExtra(FullScreenImageViewActivity.URI_LIST_DATA, uriString)

                fullImageIntent.putExtra(
                    FullScreenImageViewActivity.IMAGE_FULL_SCREEN_CURRENT_POS, position
                )
                startActivity(fullImageIntent)
            } else {
                // Handle other file types
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.onFirsTimeUiCreate(arguments)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAllFilesUploaded(event: LocalEvents.AllFilesUploaded?) {
        viewModel.onFirsTimeUiCreate(arguments)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }
}