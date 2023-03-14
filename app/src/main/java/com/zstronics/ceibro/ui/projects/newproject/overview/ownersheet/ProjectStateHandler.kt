package com.zstronics.ceibro.ui.projects.newproject.overview.ownersheet

import com.zstronics.ceibro.data.repos.projects.projectsmain.AllProjectsResponse

interface ProjectStateHandler {
    fun onProjectCreated(project: AllProjectsResponse.Projects?)
    fun onMemberDelete()
    fun onMemberAdd()
}