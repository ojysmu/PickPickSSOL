package mbtinder.android.util

import android.os.Handler
import android.os.Looper
import android.view.View
import mbtinder.lib.util.BlockWrapper
import mbtinder.lib.util.block
import mbtinder.lib.util.blockNull

fun runOnUiThread(process: () -> Unit) {
    Handler(Looper.getMainLooper()).post(process)
}

fun runOnBackground(process: () -> Unit): Thread {
    return Thread { process.invoke() }.apply { start() }
}

inline fun <R> runOnBackground(crossinline process: () -> R): R {
    var result: R? = null
    Thread { result = process.invoke() }.apply { start(); join() }

    return blockNull(BlockWrapper(result)) { it == null }!!
}


fun runEach(processes: Array<() -> Unit>, needMainThread: Boolean = false): List<Thread> {
    return processes.map {
        if (needMainThread) {
            runOnBackground { runOnUiThread(it) }
        } else {
            runOnBackground(it)
        }
    }
}