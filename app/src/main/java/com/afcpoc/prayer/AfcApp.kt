package com.afcpoc.prayer

import android.app.Application
import com.afcpoc.prayer.data.ContentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Owns the shared [ContentRepository] and prefetches JSON + default guided
 * step lists on [Dispatchers.IO] so first Rosary navigation never
 * hits lazy asset decode or large list alloc on the main thread.
 */
class AfcApp : Application() {

    lateinit var repository: ContentRepository
        private set

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        repository = ContentRepository.getInstance(this)
        appScope.launch {
            repository.preload()
        }
    }

    companion object {
        fun from(context: android.content.Context): AfcApp =
            context.applicationContext as AfcApp
    }
}
