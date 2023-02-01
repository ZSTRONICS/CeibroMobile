package com.zstronics.ceibro.ui.socket

object LocalEvents {
    class TaskCreatedEvent
    data class SubTaskCreatedEvent(val taskId: String)
}