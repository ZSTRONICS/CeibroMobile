package com.zstronics.ceibro.base.interfaces

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job

interface CoroutineViewModel {
    val viewModelJob: Job
    val viewModelScope: CoroutineScope
    fun cancelAllJobs()
    fun launch(block: suspend () -> Unit)
    fun async(block: suspend () -> Unit): Any
}