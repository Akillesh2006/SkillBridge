package com.skillbridge.ai.app

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

/**
 * SkillBridge AI Application class.
 *
 * Annotated with [HiltAndroidApp] to trigger Hilt's code generation and
 * serve as the root component in the Hilt hierarchy.
 *
 * Initialises [Timber] for structured, tag-free logging.
 */
@HiltAndroidApp
class SkillBridgeApp : Application() {

    override fun onCreate() {
        super.onCreate()
        initLogging()
    }

    private fun initLogging() {
        Timber.plant(Timber.DebugTree())
    }
}
