package com.zstronics.ceibro.data.sessions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.onesignal.OneSignal
import com.zstronics.ceibro.base.*
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.repos.auth.login.Tokens
import com.zstronics.ceibro.data.repos.auth.login.User
import com.zstronics.ceibro.data.repos.dashboard.contacts.SyncContactsRequest
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse
import com.zstronics.ceibro.data.repos.task.models.v2.NewTaskToSave

class SessionManager constructor(
    val sharedPreferenceManager: SharedPreferenceManager
) {
    fun startUserSession(user: User, tokens: Tokens, pass: String, rememberMe: Boolean) {
        sharedPreferenceManager.saveBoolean(
            KEY_IS_USER_LOGGED_IN,
            rememberMe
        )
        CookiesManager.isLoggedIn = true
        CookiesManager.jwtToken = tokens.access.token
        sharedPreferenceManager.saveCompleteUserObj(KEY_USER, user)
        sharedPreferenceManager.saveCompleteTokenObj(KEY_TOKEN, tokens)
        sharedPreferenceManager.saveString(KEY_PASS, pass)

        _user.postValue(user)
    }

    fun endUserSession() {
        sharedPreferenceManager.removeValue(KEY_IS_USER_LOGGED_IN)
        sharedPreferenceManager.removeValue(KEY_USER)
        sharedPreferenceManager.removeValue(KEY_TOKEN)
        sharedPreferenceManager.removeValue(KEY_PASS)
        sharedPreferenceManager.removeValue(KEY_IS_TO_ME_UNREAD)
        sharedPreferenceManager.removeValue(KEY_SYNCED_CONTACTS)
        sharedPreferenceManager.removeValue(KEY_SAVED_TASK)
        _user.postValue(
            user.value?.copy(
                phoneNumber = "",
                firstName = "",
                surName = "",
                companyName = "",
                profilePic = "", autoContactSync = false
            )
        )
        CookiesManager.isLoggedIn = false
        CookiesManager.jwtToken = ""
        OneSignal.removeExternalUserId()
        OneSignal.disablePush(true)
        OneSignal.pauseInAppMessages(true)
    }

    companion object {
        private var _user: MutableLiveData<User?> = MutableLiveData()
        var user: LiveData<User?> = _user
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
        user = _user
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

    fun updateUser(userObj: User) {
        sharedPreferenceManager.saveCompleteUserObj(KEY_USER, userObj)
        _user.postValue(userObj)
        user = _user
    }

    fun updateTokens(tokens: Tokens) {
        sharedPreferenceManager.saveCompleteTokenObj(KEY_TOKEN, tokens)
        CookiesManager.isLoggedIn = true
        CookiesManager.jwtToken = tokens.access.token
    }

    private fun setToken() {
        val tokenPref: Tokens? = sharedPreferenceManager.getCompleteTokenObj(KEY_TOKEN)
        CookiesManager.isLoggedIn = true
        CookiesManager.jwtToken = tokenPref?.access?.token
    }

    private fun getTokens(): Tokens? {
        val tokenPref: Tokens? = sharedPreferenceManager.getCompleteTokenObj(KEY_TOKEN)
        return tokenPref
    }


    fun getPass(): String {
        val pass = sharedPreferenceManager.getValueString(KEY_PASS) ?: ""
        return pass
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
        CookiesManager.jwtToken = tokens.access.token
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
}