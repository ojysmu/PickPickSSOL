package mbtinder.android.receiver.boot

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import mbtinder.android.service.ThreadService

class BootReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            Intent(context, ThreadService::class.java).also {
                context.startForegroundService(it)
            }
        }
    }
}