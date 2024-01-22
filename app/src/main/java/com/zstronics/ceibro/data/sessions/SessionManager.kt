package com.zstronics.ceibro.data.sessions

import android.os.Handler
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onesignal.OneSignal
import com.zstronics.ceibro.CeibroApplication
import com.zstronics.ceibro.base.KEY_ANDROID_ID
import com.zstronics.ceibro.base.KEY_DATA_SYNC_UPDATED_AT
import com.zstronics.ceibro.base.KEY_DEVICE_TYPE
import com.zstronics.ceibro.base.KEY_DRAWING_OBJ
import com.zstronics.ceibro.base.KEY_IS_FIRST_TIME_LAUNCH
import com.zstronics.ceibro.base.KEY_IS_FROM_ME_UNREAD
import com.zstronics.ceibro.base.KEY_IS_HIDDEN_UNREAD
import com.zstronics.ceibro.base.KEY_IS_TO_ME_UNREAD
import com.zstronics.ceibro.base.KEY_IS_USER_LOGGED_IN
import com.zstronics.ceibro.base.KEY_PASS
import com.zstronics.ceibro.base.KEY_PROJECT
import com.zstronics.ceibro.base.KEY_SAVED_TASK
import com.zstronics.ceibro.base.KEY_SECURE_UUID
import com.zstronics.ceibro.base.KEY_SYNCED_CONTACTS
import com.zstronics.ceibro.base.KEY_TOKEN
import com.zstronics.ceibro.base.KEY_USER
import com.zstronics.ceibro.data.repos.auth.login.Tokens
import com.zstronics.ceibro.data.repos.auth.login.User
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.repos.projects.drawing.DrawingV2
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskToSave

class SessionManager constructor(
    val sharedPreferenceManager: SharedPreferenceManager
) {
    fun startUserSession(
        user: User,
        tokens: Tokens,
        pass: String,
        rememberMe: Boolean,
        secureUUID: String,
        deviceType: String,
        androidId: String
    ) {
        sharedPreferenceManager.saveBoolean(
            KEY_IS_USER_LOGGED_IN,
            rememberMe
        )
        CeibroApplication.CookiesManager.isLoggedIn = true
        CeibroApplication.CookiesManager.jwtToken = tokens.access.token
        CeibroApplication.CookiesManager.secureUUID = secureUUID
        CeibroApplication.CookiesManager.deviceType = deviceType
        CeibroApplication.CookiesManager.androidId = androidId
        sharedPreferenceManager.saveCompleteUserObj(KEY_USER, user)
        sharedPreferenceManager.saveCompleteTokenObj(KEY_TOKEN, tokens)
        sharedPreferenceManager.saveString(KEY_PASS, pass)
        sharedPreferenceManager.saveString(KEY_SECURE_UUID, secureUUID)
        sharedPreferenceManager.saveString(KEY_DEVICE_TYPE, deviceType)
        sharedPreferenceManager.saveString(KEY_ANDROID_ID, androidId)

        _user.postValue(user)
    }

    fun endUserSession() {
        OneSignal.removeExternalUserId()
        OneSignal.disablePush(true)
        OneSignal.pauseInAppMessages(true)
        sharedPreferenceManager.removeValue(KEY_IS_USER_LOGGED_IN)
        sharedPreferenceManager.removeValue(KEY_USER)
        sharedPreferenceManager.removeValue(KEY_TOKEN)
        sharedPreferenceManager.removeValue(KEY_PASS)
        sharedPreferenceManager.removeValue(KEY_SECURE_UUID)
        sharedPreferenceManager.removeValue(KEY_IS_TO_ME_UNREAD)
        sharedPreferenceManager.removeValue(KEY_SYNCED_CONTACTS)
        sharedPreferenceManager.removeValue(KEY_SAVED_TASK)
        sharedPreferenceManager.removeValue(KEY_DRAWING_OBJ)
        saveCompleteDrawingObj(null)
        val handler = Handler()
        handler.postDelayed(Runnable {
            _user.postValue(
                user.value?.copy(
                    id = "",
                    phoneNumber = "",
                    firstName = "",
                    surName = "",
                    companyName = "",
                    jobTitle = "",
                    profilePic = "", autoContactSync = false
                )
            )
        }, 500)

        CeibroApplication.CookiesManager.isLoggedIn = false
        CeibroApplication.CookiesManager.jwtToken = ""
        CeibroApplication.CookiesManager.secureUUID = ""
        CeibroApplication.CookiesManager.deviceType = ""
        CeibroApplication.CookiesManager.androidId = ""
    }

    companion object {
        private var _user: MutableLiveData<User?> = MutableLiveData()
        var user: MutableLiveData<User?> = _user
        private var _projects: MutableLiveData<MutableList<ProjectsWithMembersResponse.ProjectDetail>?> =
            MutableLiveData()
        var projects: LiveData<MutableList<ProjectsWithMembersResponse.ProjectDetail>?> = _projects
    }

    fun saveToMeUnread(isUnread: Boolean) {
        sharedPreferenceManager.saveBoolean(KEY_IS_TO_ME_UNREAD, isUnread)
    }

    fun saveFromMeUnread(isUnread: Boolean) {
        sharedPreferenceManager.saveBoolean(KEY_IS_FROM_ME_UNREAD, isUnread)
    }

    fun saveHiddenUnread(isUnread: Boolean) {
        sharedPreferenceManager.saveBoolean(KEY_IS_HIDDEN_UNREAD, isUnread)
    }

    fun isToMeUnread(): Boolean {
        return sharedPreferenceManager.getValueBoolean(KEY_IS_TO_ME_UNREAD, false)
    }

    fun isFromMeUnread(): Boolean {
        return sharedPreferenceManager.getValueBoolean(KEY_IS_FROM_ME_UNREAD, false)
    }

    fun isHiddenUnread(): Boolean {
        return sharedPreferenceManager.getValueBoolean(KEY_IS_HIDDEN_UNREAD, false)
    }

    fun saveNewTaskData(newTaskToSave: NewTaskToSave) {
        sharedPreferenceManager.saveCompleteTask(KEY_SAVED_TASK, newTaskToSave)
    }

    fun getSavedNewTaskData(): NewTaskToSave? {
        return sharedPreferenceManager.getCompleteTask(KEY_SAVED_TASK)
    }

    fun getUser(): LiveData<User?> {
        return user
    }

    fun getProjects(): LiveData<MutableList<ProjectsWithMembersResponse.ProjectDetail>?> {
        return projects
    }

    fun getUserObj(): User? {
        return sharedPreferenceManager.getCompleteUserObj(KEY_USER)
    }

    fun setUser() {
        val userPref: User? = sharedPreferenceManager.getCompleteUserObj(KEY_USER)
        _user.postValue(userPref)
    }

    fun setProject() {
        val projectList = sharedPreferenceManager.getCompleteProjectObj(KEY_PROJECT)
        _projects.postValue(projectList)
        projects = _projects
    }

    fun setNewProjectList(projectList: MutableList<ProjectsWithMembersResponse.ProjectDetail>?) {
        sharedPreferenceManager.saveCompleteProjectObj(KEY_PROJECT, projectList)
        _projects.postValue(projectList)
    }

    fun updateUser(userObj: User?) {

        userObj?.let {
            sharedPreferenceManager.saveCompleteUserObj(KEY_USER, it)
            setUser()
        }

    }

    fun updateTokens(tokens: Tokens) {
        sharedPreferenceManager.saveCompleteTokenObj(KEY_TOKEN, tokens)
        CeibroApplication.CookiesManager.isLoggedIn = true
        CeibroApplication.CookiesManager.jwtToken = tokens.access.token
    }

    fun setToken() {
        val tokenPref: Tokens? = sharedPreferenceManager.getCompleteTokenObj(KEY_TOKEN)
        val secureUUID = sharedPreferenceManager.getValueString(KEY_SECURE_UUID) ?: ""
        val deviceType = sharedPreferenceManager.getValueString(KEY_DEVICE_TYPE) ?: ""
        val androidId = sharedPreferenceManager.getValueString(KEY_ANDROID_ID) ?: ""
        CeibroApplication.CookiesManager.isLoggedIn = true
        CeibroApplication.CookiesManager.jwtToken = tokenPref?.access?.token
        CeibroApplication.CookiesManager.secureUUID = secureUUID
        CeibroApplication.CookiesManager.deviceType = deviceType
        CeibroApplication.CookiesManager.androidId = androidId
    }

    private fun getTokens(): Tokens? {
        val tokenPref: Tokens? = sharedPreferenceManager.getCompleteTokenObj(KEY_TOKEN)
        return tokenPref
    }

    fun getCompleteDrawingObj(): DrawingV2? {
        return sharedPreferenceManager.getCompleteDrawingObj()
    }

    fun saveCompleteDrawingObj(drawingV2: DrawingV2?) {
        return sharedPreferenceManager.saveCompleteDrawingObj(drawingV2)
    }


    fun getSecureUUID(): String {
        return sharedPreferenceManager.getValueString(KEY_SECURE_UUID) ?: ""
    }

    fun isUserLoggedIn(): Boolean {
        val isLoggedIn =
            if (sharedPreferenceManager.getValueBoolean(KEY_IS_USER_LOGGED_IN, false)) {
                setToken()
                true
            } else false
        return isLoggedIn
    }

    fun refreshToken(tokens: Tokens) {
        sharedPreferenceManager.saveCompleteTokenObj(KEY_TOKEN, tokens)
        CeibroApplication.CookiesManager.jwtToken = tokens.access.token
    }

    fun getRefreshToken(): String? {
        val tokenPref: Tokens? = sharedPreferenceManager.getCompleteTokenObj(KEY_TOKEN)
        return tokenPref?.refresh?.token
    }

    fun getUserId(): String {
        return getUserObj()?.id ?: ""
    }

    fun isLoggedIn(): Boolean {
        return sharedPreferenceManager.getValueBoolean(KEY_IS_USER_LOGGED_IN, false)
    }

    fun getToken(): String? {
        val tokenPref: Tokens? = sharedPreferenceManager.getCompleteTokenObj(KEY_TOKEN)
        return tokenPref?.access?.token
    }

    fun saveSyncedContacts(selectedContacts: List<SyncContactsRequest.CeibroContactLight>) {
        sharedPreferenceManager.saveSyncedContacts(KEY_SYNCED_CONTACTS, selectedContacts)
    }

    fun getSyncedContacts(): List<SyncContactsRequest.CeibroContactLight>? {
        return sharedPreferenceManager.getSyncedContacts(KEY_SYNCED_CONTACTS)
    }

    fun updateAutoSync(enabled: Boolean) {
        val oldUser = getUserObj()
        oldUser?.autoContactSync = enabled
        oldUser?.let { user ->
            updateUser(user)
        }
    }

    fun isFirstTimeLaunch(): Boolean {
        return sharedPreferenceManager.getValueBoolean(KEY_IS_FIRST_TIME_LAUNCH, true)
    }

    fun setNotFirstTimeLaunch() {
        sharedPreferenceManager.saveBoolean(KEY_IS_FIRST_TIME_LAUNCH, false)
    }

    fun getUpdatedAtTimeStamp(): String {
        // return sharedPreferenceManager.getValueString(KEY_DATA_SYNC_UPDATED_AT) ?: "2020-01-01T17:12:18.787Z"
        return "2020-01-01T17:12:18.787Z"
    }

    fun saveUpdatedAtTimeStamp(updatedAtTimeStamp: String?) {
        if (!updatedAtTimeStamp.isNullOrEmpty()) {
            sharedPreferenceManager.saveString(KEY_DATA_SYNC_UPDATED_AT, updatedAtTimeStamp)
        }
    }

    fun getBooleanValue(key: String): Boolean {
        return sharedPreferenceManager.getValueBoolean(key, false)
    }

    fun saveBooleanValue(key: String, value: Boolean) {
        sharedPreferenceManager.saveBoolean(key, value)
    }

    fun getIntegerValue(key: String): Int {
        return sharedPreferenceManager.getValueInt(key) ?: -5
    }

    fun saveIntegerValue(key: String, value: Int) {
        sharedPreferenceManager.saveInt(key, value)
    }

    fun getStringValue(key: String): String {
        return sharedPreferenceManager.getValueString(key) ?: ""
    }

    fun saveStringValue(key: String, value: String) {
        sharedPreferenceManager.saveString(key, value)
    }

}