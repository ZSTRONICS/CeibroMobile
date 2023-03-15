package com.zstronics.ceibro.ui.projects.newproject.documents

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatTextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.BR
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.clickevents.setOnClick
import com.zstronics.ceibro.base.extensions.gone
import com.zstronics.ceibro.base.extensions.visible
import com.zstronics.ceibro.base.navgraph.BaseNavViewModelFragment
import com.zstronics.ceibro.data.repos.projects.documents.CreateProjectFolderResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.databinding.FragmentProjectDocumentsBinding
import com.zstronics.ceibro.ui.attachment.SubtaskAttachment
import com.zstronics.ceibro.ui.projects.newproject.documents.adapter.ProjectFilesNestedAdapter
import com.zstronics.ceibro.ui.projects.newproject.documents.adapter.ProjectFoldersAdapter
import com.zstronics.ceibro.ui.projects.newproject.documents.newfolder.NewFolderSheet
import com.zstronics.ceibro.ui.tasks.newsubtask.AttachmentAdapter
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class ProjectDocumentsFragment(private val projectLive: MutableLiveData<AllProjectsResponse.Projects>) :
    BaseNavViewModelFragment<FragmentProjectDocumentsBinding, IProjectDocuments.State, ProjectDocumentsVM>() {

    override val bindingVariableId = BR.viewModel
    override val bindingViewStateVariableId = BR.viewState
    override val viewModel: ProjectDocumentsVM by viewModels()
    override val layoutResId: Int = R.layout.fragment_project_documents
    override fun toolBarVisibility(): Boolean = true
    override fun hasOptionMenu(): Boolean = true
    override fun getToolBarTitle() =
        projectLive.value?.title ?: getString(R.string.new_projects_title)

    override fun onClick(id: Int) {
        when (id) {
            R.id.saveFileBtn -> {
                viewModel.uploadDocumentsInProject(requireContext())
                mViewDataBinding.attachmentView.gone()
            }
        }
    }

    override fun onPrepareOptionsMenu(menu: Menu) {

        val menuItem = menu.findItem(R.id.itemAdd)
        val addMenuLayout = menuItem.actionView as ConstraintLayout
        val addMenuBtn = addMenuLayout.findViewById<AppCompatButton>(R.id.addMenuBtn)

        addMenuBtn.setOnClick {
            val popUpWindowObj =
                uploadPopupMenu(addMenuLayout)
            popUpWindowObj.showAsDropDown(
                addMenuLayout.findViewById(R.id.addMenuBtn),
                0,
                5
            )
        }
    }

    private fun uploadPopupMenu(
        v: View,
    ): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_upload_file_folder_menu, null)

        val uploadFolder = view.findViewById<AppCompatTextView>(R.id.uploadFolder)
        val uploadFiles = view.findViewById<AppCompatTextView>(R.id.uploadFiles)

        uploadFolder.setOnClick {
            val sheet = NewFolderSheet()
            sheet.onFolderAdd = { folderName ->
                projectLive.value?.id?.let { projectId ->
                    viewModel.addFolder(
                        projectId,
                        folderName
                    )
                }
            }
            sheet.show(childFragmentManager, "NewFolderSheet")
            popupWindow.dismiss()
        }
        uploadFiles.setOnClick {
            viewModel.isRootSelected = true
            pickAttachment(true)
            popupWindow.dismiss()
        }

        popupWindow.isFocusable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 13f
        return popupWindow
    }

    @Inject
    lateinit var foldersAdapter: ProjectFoldersAdapter

    @Inject
    lateinit var filesAdapter: ProjectFilesNestedAdapter

    @Inject
    lateinit var attachmentAdapter: AttachmentAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.projectId = projectLive.value?.id.toString()
        mViewDataBinding.foldersRV.adapter = foldersAdapter
        mViewDataBinding.filesRV.adapter = filesAdapter
        mViewDataBinding.attachmentRecyclerView.adapter = attachmentAdapter

        foldersAdapter.simpleChildItemClickListener =
            { childView: View, position: Int, data: CreateProjectFolderResponse.ProjectFolder ->
                val folderOptionPopupMenu =
                    folderOptionPopupMenu(
                        position = position,
                        v = childView,
                        folder = data
                    )
                folderOptionPopupMenu.showAsDropDown(
                    childView.findViewById(R.id.optionMenu),
                    0,
                    1
                )
            }

        foldersAdapter.onFolderExpand =
            { childView: View, position: Int, data: CreateProjectFolderResponse.ProjectFolder ->
                if (data.files.isNullOrEmpty())
                    viewModel.getFilesUnderFolder(data.id)
            }
        viewModel.fileUriList.observe(viewLifecycleOwner) { list ->
            if (list != null) {
                if (list.isNotEmpty()) {
                    mViewDataBinding.attachmentView.visible()
                } else
                    mViewDataBinding.attachmentView.gone()
                attachmentAdapter.setList(list)
            }
        }
        attachmentAdapter.itemClickListener =
            { _: View, position: Int, data: SubtaskAttachment? ->
                viewModel.removeFile(position)
            }

        viewModel.documents(projectLive.value?.id ?: "")
        viewModel.folders.observe(viewLifecycleOwner) {
            foldersAdapter.setList(it)
        }

        viewModel.files.observe(viewLifecycleOwner) {
            filesAdapter.setList(it)
        }
    }

    private fun folderOptionPopupMenu(
        position: Int,
        v: View,
        folder: CreateProjectFolderResponse.ProjectFolder,
    ): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_folder_option_menu, null)

        val manageAccess = view.findViewById<AppCompatTextView>(R.id.manageAccess)
        manageAccess.setOnClick {
            popupWindow.dismiss()
        }
        val uploadFiles = view.findViewById<AppCompatTextView>(R.id.uploadFiles)
        uploadFiles.setOnClick {
            viewModel.isRootSelected = false
            viewModel.selectedFolderId = folder.id
            pickAttachment(true)
            popupWindow.dismiss()
        }

        popupWindow.isFocusable = true
        popupWindow.width = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.height = WindowManager.LayoutParams.WRAP_CONTENT
        popupWindow.contentView = view
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.WHITE))
        popupWindow.elevation = 13f
        return popupWindow
    }
}