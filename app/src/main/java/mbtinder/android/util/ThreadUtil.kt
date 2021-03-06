package mbtinder.android.util

import android.os.Handler
import android.os.Looper
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import mbtinder.lib.util.BlockWrapper
import mbtinder.lib.util.block
import mbtinder.lib.util.blockNull

@AnyThread
fun runOnUiThread(@MainThread process: () -> Unit) {
    Handler(Looper.getMainLooper()).post(process)
}

@AnyThread
fun runOnBackground(process: () -> Unit): Thread {
    return Thread(process).apply { start() }
}

@WorkerThread
inline fun <R> runOnBackground(crossinline process: () -> R): R {
    var result: R? = null
    Thread { result = process.invoke() }.apply { start(); join() }

    return result!!
}