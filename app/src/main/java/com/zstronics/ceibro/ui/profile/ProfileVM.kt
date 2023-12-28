package com.zstronics.ceibro.ui.profile

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.work.WorkManager
import com.onesignal.OneSignal
import com.zstronics.ceibro.R
import com.zstronics.ceibro.base.viewmodel.HiltBaseViewModel
import com.zstronics.ceibro.data.database.dao.ConnectionsV2Dao
import com.zstronics.ceibro.data.database.dao.FloorsV2Dao
import com.zstronics.ceibro.data.database.dao.GroupsV2Dao
import com.zstronics.ceibro.data.database.dao.ProjectsV2Dao
import com.zstronics.ceibro.data.database.dao.TaskV2Dao
import com.zstronics.ceibro.data.database.dao.TopicsV2Dao
import com.zstronics.ceibro.data.repos.task.TaskRepository
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskV2Entity
import com.zstronics.ceibro.data.sessions.SessionManager
import com.zstronics.ceibro.ui.contacts.ContactSyncWorker
import com.zstronics.ceibro.ui.socket.LocalEvents
import com.zstronics.ceibro.ui.socket.SocketHandler
import dagger.hilt.android.lifecycle.HiltViewModel
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import javax.inject.Inject


@HiltViewModel
class ProfileVM @Inject constructor(
    override val viewState: ProfileState,
    val sessionManager: SessionManager,
    private val taskRepository: TaskRepository,
    private val taskDao: TaskV2Dao,
    private val topicsV2Dao: TopicsV2Dao,
    private val projectsV2Dao: ProjectsV2Dao,
    private val floorV2Dao: FloorsV2Dao,
    private val groupV2Dao: GroupsV2Dao,
    private val connectionsV2Dao: ConnectionsV2Dao,
) : HiltBaseViewModel<IProfile.State>(), IProfile.ViewModel {
    var user = sessionManager.getUser().value

    init {
        EventBus.getDefault().register(this)
        sessionManager.setUser()
        user = sessionManager.getUser().value
    }

    override fun showMenuPopup(v: View) {
        val popUpWindowObj = popUpMenu(v)
        popUpWindowObj.showAsDropDown(v.findViewById(R.id.profileMenuBtn), 0, 35)
    }

    suspend fun getDraftTasks(): List<NewTaskV2Entity> {
        return draftNewTaskV2Internal.getUnSyncedRecords() ?: emptyList()
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
        } else {
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


    override fun endUserSession(context: Context) {
        val oneSignalPlayerId = OneSignal.getDeviceState()?.userId
        SocketHandler.sendLogout(oneSignalPlayerId)
        launch {
            taskRepository.eraseTaskTable()
            taskRepository.eraseSubTaskTable()
            taskDao.deleteAllEventsData()
            taskDao.deleteAllTasksData()
            topicsV2Dao.deleteAllData()
            projectsV2Dao.deleteAll()
            groupV2Dao.deleteAll()
            floorV2Dao.deleteAll()
            connectionsV2Dao.deleteAll()
            draftNewTaskV2Internal.deleteAllData()

        }
        sessionManager.endUserSession()
        // Cancel all periodic work with the tag "contactSync"
        WorkManager.getInstance(context)
            .cancelAllWorkByTag(ContactSyncWorker.CONTACT_SYNC_WORKER_TAG)
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onUserDataUpdated(event: LocalEvents.UserDataUpdated?) {
        val handler = Handler()
        handler.postDelayed(Runnable {
            sessionManager.setUser()
            user = sessionManager.getUser().value
        }, 100)
    }

    override fun onCleared() {
        super.onCleared()
        EventBus.getDefault().unregister(this)
    }
}