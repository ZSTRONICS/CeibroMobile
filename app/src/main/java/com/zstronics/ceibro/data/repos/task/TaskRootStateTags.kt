package com.zstronics.ceibro.data.repos.task

enum class TaskRootStateTags(val tagValue: String) {
    All("all"), ToMe("to-me"), FromMe("from-me"), Hidden("hidden"), Canceled("canceled"), Default("default"), Ongoing("Ongoing")
    , Approval("Approval"), Closed("Closed"), ToReview("to-review"), InReview("in-review"),
    AllWithoutViewOnly("allWithoutViewOnly"), ViewOnly("viewOnly"), Approver("approver")
}
