package com.zstronics.ceibro.data.repos.projects.createNewProject

import com.google.gson.annotations.SerializedName

data class CreateProjectRequest(
    @SerializedName("projectPhoto")
    val projectPhoto: String,
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
    val extraStatus: JsonWithDataObject,
    @SerializedName("owner")
    val owner: JsonWithDataObject,
)

data class JsonWithDataObject(
    @SerializedName("data")
    val data: List<String>
)