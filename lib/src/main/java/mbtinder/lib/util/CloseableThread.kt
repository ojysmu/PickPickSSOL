package mbtinder.lib.util

import mbtinder.lib.constant.ThreadConstants

open class CloseableThread : Thread(), AutoCloseable {
    private val defaultLoop = { sleep() }

    @Volatile
    private var status: Int = ThreadConstants.WAITING

    var prefix: () -> Unit = {}
    var loop: () -> Unit = defaultLoop
    var suffix: () -> Unit = {}

    var intervalInMillis: Long = 100

    override fun run() {
        status = ThreadConstants.RUNNING
        prefix()

        while (status == ThreadConstants.RUNNING || status == ThreadConstants.WAITING) {
            while (status == ThreadConstants.WAITING) {
                sleep()
            }

            loop()
        }

        suffix()
    }

    override fun close() {
        status = ThreadConstants.FINISHING
    }

    fun sleep() = sleep(intervalInMillis)

    open fun pauseThread() {
        status = ThreadConstants.WAITING
    }

    open fun resumeThread() {
        status = ThreadConstants.RUNNING
    }

    fun stopThread() {
        status = ThreadConstants.FINISHING
    }

    fun isFinished() = status == ThreadConstants.FINISHED
}