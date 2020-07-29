package mbtinder.android.util

import android.os.Handler
import android.os.Looper

object ThreadUtil {
    fun runOnUiThread(process: () -> Unit) {
        Handler(Looper.getMainLooper()).post(process)
    }

    fun runOnBackground(process: () -> Unit): Thread {
        return Thread { process() }.apply { start() }
    }
}