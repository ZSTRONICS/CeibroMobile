package com.zstronics.ceibro.data.repos.projects.createNewProject

import com.google.gson.annotations.SerializedName
import java.io.File

data class CreateProjectRequest(
    @SerializedName("projectPhoto")
    val projectPhoto: File,
    @SerializedName("title")
    val title: String,
    @SerializedName("location")
    val location: String,
    @SerializedName("description")
    val description: String,
    @SerializedName("dueDate")
    val dueDate: String,
    @SerializedName("publishStatus")
    val publishStatus: String,
    @SerializedName("extraStatus")
    val extraStatus: String,
    @SerializedName("owner")
    val owner: String,
)