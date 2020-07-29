package mbtinder.android.util

import android.util.Log

object Log {
    private const val LOG_TAG = "MBTinder"

    fun v(msg: String) {
        Log.v(LOG_TAG, msg)
    }

    fun v(msg: String, tr: Throwable) {
        Log.v(LOG_TAG, msg, tr)
    }

    fun e(msg: String) {
        Log.e(LOG_TAG, msg)
    }

    fun e(msg: String, tr: Throwable) {
        Log.e(LOG_TAG, msg, tr)
    }
}