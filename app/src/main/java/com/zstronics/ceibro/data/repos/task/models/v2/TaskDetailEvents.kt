package com.zstronics.ceibro.data.repos.task.models.v2

enum class TaskDetailEvents(val eventValue: String) {
    Comment("comment"), ForwardTask("forwardTask"), InvitedUser("invitedUser"), DoneTask("doneTask"),
    CancelTask("cancelTask")
}
