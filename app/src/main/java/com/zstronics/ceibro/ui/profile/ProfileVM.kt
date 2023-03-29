package com.zstronics.ceibro.ui.profile

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.*
import android.widget.PopupWindow
import androidx.appcompat.widget.AppCompatImageView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.extensions.shortToastNow
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.sessions.SessionManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


@HiltViewModel
class ProfileVM @Inject constructor(
    override val viewState: ProfileState,
    val sessionManager: SessionManager,
    private val taskRepository: TaskRepository
) : HiltBaseViewModel<IProfile.State>(), IProfile.ViewModel {
    var user = sessionManager.getUser().value
    init {
        sessionManager.setUser()
        user = sessionManager.getUser().value
    }

    override fun showMenuPopup(v: View) {
        val popUpWindowObj = popUpMenu(v)
        popUpWindowObj.showAsDropDown(v.findViewById(R.id.profileMenuBtn), 0, 35)
    }

    override fun popUpMenu(v: View): PopupWindow {
        val popupWindow = PopupWindow(v.context)
        val context: Context = v.context
        val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
        val view: View = inflater.inflate(R.layout.layout_profile_menu, null)

        val menuEdit = view.findViewById<View>(R.id.menuEdit)
        val menuAdmin = view.findViewById<View>(R.id.menuAdmin)
        val menuHelp = view.findViewById<View>(R.id.menuHelp)
        val menuLogout = view.findViewById<View>(R.id.menuLogout)

        if (user?.role.equals("admin", true)) {
            menuAdmin.visibility = View.VISIBLE
        }
        else {
            menuAdmin.visibility = View.GONE
        }

        menuEdit.setOnClickListener {
            clickEvent?.postValue(106)
            popupWindow.dismiss()
        }
        menuAdmin.setOnClickListener {
            clickEvent?.postValue(107)
            popupWindow.dismiss()
        }
        menuHelp.setOnClickListener {
            clickEvent?.postValue(108)
            popupWindow.dismiss()
        }
        menuLogout.setOnClickListener {
            clickEvent?.postValue(110)
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


    override fun endUserSession() {
        launch {
            taskRepository.eraseTaskTable()
            taskRepository.eraseSubTaskTable()
        }
        sessionManager.endUserSession()
    }
}