package mbtinder.android.util

import android.os.Handler
import android.os.Looper
import androidx.annotation.AnyThread
import androidx.annotation.MainThread
import androidx.annotation.WorkerThread
import mbtinder.lib.util.BlockWrapper
import mbtinder.lib.util.blockNull

@AnyThread
fun runOnUiThread(@MainThread process: () -> Unit) {
//    val start = System.nanoTime()
    Handler(Looper.getMainLooper()).post {
        process.invoke();
//        Log.v("runOnUiThread(): elapsed=${System.nanoTime() - start}, ${getStackTrace()}")
    }
}

@AnyThread
fun runOnBackground(process: () -> Unit): Thread {
//    val start = System.nanoTime()
    return Thread {
        process.invoke();
//        Log.v("runOnBackground(): elapsed=${System.nanoTime() - start}, ${getStackTrace()}")
    }.apply { start() }
}

@WorkerThread
inline fun <R> runOnBackground(crossinline process: () -> R): R {
    var result: R? = null
    Thread { result = process.invoke() }.apply { start(); join() }

    return blockNull(BlockWrapper(result)) { it == null }!!
}

fun getStackTrace(): String {
    val stackTraceElements = Thread.currentThread().stackTrace
    val traceBuilder = StringBuilder()
    for (i in 3 until stackTraceElements.size) {
        traceBuilder.append(stackTraceElements[i].fileName)
        traceBuilder.append(":")
        traceBuilder.append(stackTraceElements[i].lineNumber)
        traceBuilder.append(":")
        traceBuilder.append(stackTraceElements[i].methodName)
        traceBuilder.append(" => ")
    }

    return traceBuilder.toString()
}