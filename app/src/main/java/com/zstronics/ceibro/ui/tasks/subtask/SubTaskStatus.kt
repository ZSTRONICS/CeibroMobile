package com.zstronics.ceibro.ui.tasks.subtask

import com.zstronics.ceibro.R

enum class SubTaskStatus {
    ALL,
    ONGOING,
    ASSIGNED,
    ACCEPTED,
    REJECTED,
    DONE,
    DRAFT,
    START,
    PAUSE,
    RESUME;

    companion object {
        fun String.stateToHeadingAndBg(): Pair<Int, SubTaskStatus> {
            return when (uppercase()) {
                ONGOING.name -> Pair(
                    R.drawable.status_ongoing_filled,
                    ONGOING
                )
                ASSIGNED.name -> Pair(
                    R.drawable.status_assigned_filled,
                    ASSIGNED
                )
                ACCEPTED.name -> Pair(
                    R.drawable.status_accepted_filled,
                    ACCEPTED
                )
                REJECTED.name -> Pair(
                    R.drawable.status_reject_filled,
                    REJECTED
                )
                DONE.name -> Pair(
                    R.drawable.status_done_filled,
                    DONE
                )
                DRAFT.name -> Pair(
                    R.drawable.status_draft_filled,
                    DRAFT
                )
                else -> Pair(
                    R.drawable.status_draft_filled,
                    SubTaskStatus.valueOf(this)
                )
            }
        }
    }
}