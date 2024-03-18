package com.zstronics.ceibro.data.repos.task.models.v2

enum class TaskDetailEvents(val eventValue: String) {
    Comment("comment"), ForwardTask("forwardTask"), InvitedUser("invitedUser"), DoneTask("doneTask"),
    CancelTask("cancelTask"), UnCancelTask("unCancelTask"), JoinedTask("joinedTask"),
    APPROVE("approve"), APPROVED("approved"), REJECT_REOPEN("reject-reopen"),
    REJECT_REOPENED("reject-reopened"), REJECT_CLOSE("reject-close"), REJECT_CLOSED("reject-closed"), ReOpen("reopen"),
}
