package mbtinder.android.util

import android.os.Handler
import android.os.Looper
import mbtinder.lib.util.BlockWrapper
import mbtinder.lib.util.block
import mbtinder.lib.util.blockNull

object ThreadUtil {
    fun runOnUiThread(process: () -> Unit) {
        Handler(Looper.getMainLooper()).post(process)
    }

    inline fun <R> runOnBackground(crossinline process: () -> R): R {
        var result: R? = null
        Thread { result = process.invoke() }.apply { start(); join() }

        return blockNull(BlockWrapper(result)) { it == null }!!
    }

    fun runOnBackground(process: () -> Unit): Thread {
        return Thread { process.invoke() }.apply { start() }
    }
}