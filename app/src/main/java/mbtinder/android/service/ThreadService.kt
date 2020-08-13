package mbtinder.android.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import mbtinder.android.io.database.SQLiteConnection
import mbtinder.android.io.socket.SocketClient
import mbtinder.android.util.Log
import mbtinder.android.util.ThreadUtil
import mbtinder.lib.constant.ServerPath

class ThreadService : Service() {
    private var flag = true

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return if (flag) {
            flag = false
            ThreadUtil.runOnBackground {
                Thread.sleep(1000)

                onTaskRemoved(intent)
            }

            START_STICKY
        } else {
            START_NOT_STICKY
        }
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onTaskRemoved(rootIntent: Intent?) {
        if (flag) {
            return
        }
        flag = true

        Thread.sleep(1000)
        if (!SocketClient.isAlive()) {
            Log.v("Socket Dead")
            SocketClient.createInstance(ServerPath.ADDRESS, ServerPath.PORT_SOCKET, this).start()
        }

        val restartIntent = Intent(applicationContext, this::class.java)
        restartIntent.setPackage(packageName)
        startService(restartIntent)

        super.onTaskRemoved(rootIntent)
    }
}
