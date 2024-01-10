package com.zstronics.ceibro.data.repos.task

enum class TaskRootStateTags(val tagValue: String) {
    ToMe("to-me"), FromMe("from-me"), Hidden("hidden"), Canceled("canceled"), Default("default")
}
