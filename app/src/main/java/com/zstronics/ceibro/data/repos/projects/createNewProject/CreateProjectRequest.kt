package com.zstronics.ceibro.data.repos.projects.createNewProject

data class CreateProjectRequest(
    val projectPhoto: String,
    val title:String,
    val location:String,
    val description:String,
    val dueDate:String,
    val publishStatus:String,
    val extraStatus:List<String>,
    val owner:List<String>,
)
