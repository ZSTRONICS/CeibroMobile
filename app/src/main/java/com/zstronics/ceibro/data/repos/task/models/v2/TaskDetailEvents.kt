package com.zstronics.ceibro.data.repos.task.models.v2

enum class TaskDetailEvents(val eventValue: String) {
    NewComment("newComment"), ForwardTask("forwardTask"), InvitedUser("invitedUser"), DoneTask("doneTask"),
    CancelTask("cancelTask")
}
