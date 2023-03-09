package com.zstronics.ceibro.ui.projects.newproject.overview.ownersheet

import com.zstronics.ceibro.data.repos.projects.createNewProject.CreateNewProjectResponse

interface ProjectStateHandler {
    fun onProjectCreated(project: CreateNewProjectResponse.CreateProject?)
}