package com.zstronics.ceibro.data.database.models.tasks

data class LocalTaskDetail(
    val topicName: String?,
    val assignee: MutableList<String>?,
    val dueDate: String?,
    val noOfFiles: Int
)