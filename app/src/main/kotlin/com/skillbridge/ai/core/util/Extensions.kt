package com.skillbridge.ai.core.util

import android.content.Context
import android.widget.Toast
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import timber.log.Timber

// ── String Extensions ──────────────────────────────────────────────────────────

/** Returns `true` if the string is not null and not blank. */
fun String?.isNotNullOrBlank(): Boolean = !this.isNullOrBlank()

/** Trims and returns the string, or null if blank after trimming. */
fun String?.trimOrNull(): String? = this?.trim()?.ifBlank { null }

// ── Context Extensions ─────────────────────────────────────────────────────────

/** Shows a short [Toast] message. */
fun Context.showToast(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

// ── Flow Extensions ────────────────────────────────────────────────────────────

/**
 * Catches any upstream [Throwable], logs it with [Timber], and terminates the
 * flow without re-throwing.
 */
fun <T> Flow<T>.catchAndLog(tag: String = "Flow"): Flow<T> =
    catch { throwable -> Timber.tag(tag).e(throwable) }
