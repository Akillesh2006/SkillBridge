package com.skillbridge.ai.core.constants

object AppConstants {

    // Base URL automatically configured per build type (debug vs release) from build.gradle.kts
    // Change production URL in app/build.gradle.kts (buildTypes -> release -> BASE_URL)
    val BASE_URL: String = com.skillbridge.ai.BuildConfig.BASE_URL

    const val CONNECT_TIMEOUT_SECONDS = 30L
    const val READ_TIMEOUT_SECONDS = 30L
    const val WRITE_TIMEOUT_SECONDS = 30L

    const val DATABASE_NAME = "skillbridge_db"
    const val DATABASE_VERSION = 1

    const val DATASTORE_NAME = "skillbridge_prefs"

    const val SPLASH_DELAY_MS = 2000L
}