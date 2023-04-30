package com.zstronics.ceibro.ui.contacts.worker

import androidx.work.DelegatingWorkerFactory
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppWorkerFactory @Inject constructor(
    contactSyncWorkerFactory: ContactSyncWorkerFactory
) : DelegatingWorkerFactory() {

    init {
        addFactory(contactSyncWorkerFactory)
    }
}