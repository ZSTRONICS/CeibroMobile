package com.zstronics.ceibro.data.sessions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.zstronics.ceibro.base.*
import com.zstronics.ceibro.data.base.CookiesManager
import com.zstronics.ceibro.data.repos.auth.login.Tokens
import com.zstronics.ceibro.data.repos.auth.login.User
import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse
import com.zstronics.ceibro.data.repos.projects.projectsmain.ProjectsWithMembersResponse

class SessionManager constructor(
    private val sharedPreferenceManager: SharedPreferenceManager
) {
    fun startUserSession(user: User, tokens: Tokens, pass: String) {
        sharedPreferenceManager.saveBoolean(
            KEY_IS_USER_LOGGED_IN,
            true
        )
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
        _user = MutableLiveData()

        CookiesManager.isLoggedIn = false
        CookiesManager.jwtToken = ""
    }

    companion object {
        private var _user: MutableLiveData<User?> = MutableLiveData()
        var user: LiveData<User?> = _user
        private var _projects: MutableLiveData<MutableList<ProjectsWithMembersResponse.ProjectDetail>?> = MutableLiveData()
        var projects: LiveData<MutableList<ProjectsWithMembersResponse.ProjectDetail>?> = _projects
    }

    fun getUser(): LiveData<User?> {
        return user
    }
    fun getProjects(): LiveData<MutableList<ProjectsWithMembersResponse.ProjectDetail>?> {
        return projects
    }

    fun getUserObj(): User? {
        val userPref: User? = sharedPreferenceManager.getCompleteUserObj(KEY_USER)
        return userPref
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
}