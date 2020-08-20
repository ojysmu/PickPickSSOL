package mbtinder.android

import android.content.Intent
import android.os.Bundle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import mbtinder.android.io.socket.SocketClient
import mbtinder.android.service.ThreadService
import mbtinder.android.ui.model.Activity
import mbtinder.android.util.DialogFactory
import mbtinder.android.util.LocationUtil
import mbtinder.android.util.Log
import mbtinder.android.util.runOnBackground
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.constant.Notification
import mbtinder.lib.constant.ServerPath
import java.io.IOException
import java.lang.RuntimeException

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            initializeSocketClient()
        } catch (e: RuntimeException) {
            SocketClient.releaseInstance()
            initializeSocketClient()
        }

        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setOnNavigationItemReselectedListener { /* 재선택시 refresh 방지 */ }
        navView.setupWithNavController(navController)

        startForegroundService(Intent(this, ThreadService::class.java))
    }

    private fun initializeSocketClient() {
        val socketClient = SocketClient.createInstance(ServerPath.ADDRESS, ServerPath.PORT_SOCKET, this)
        socketClient.onConnected = this::onConnected
        socketClient.onConnectionFailed = this::onConnectionFailed
        socketClient.onDisconnected = this::onDisconnected
        socketClient.start()
    }

    private fun onConnected() {
        Log.v("onConnected()")
    }

    private fun onConnectionFailed(e: IOException) {
        Log.e("onConnectionFailed", e)

        SocketClient.releaseInstance()
        runOnUiThread {
            DialogFactory.getContentedDialog(this, R.string.socket_on_connection_failed, this::finish).show()
        }
    }

    private fun onDisconnected(e: IOException) {
        Log.e("onDisconnected", e)

        SocketClient.releaseInstance()
        runOnBackground {
            val result = retryConnection(0)
            if (result) {
                SocketClient.getInstance().onConnectionFailed = this::onConnectionFailed
                SocketClient.getInstance().onDisconnected = this::onDisconnected
            } else {
                runOnUiThread {
                    DialogFactory.getContentedDialog(this, R.string.socket_on_connection_failed, this::finish).show()
                }
            }
        }
    }

    private fun retryConnection(depth: Int): Boolean {
        Log.v("retryConnection: depth=$depth")

        if (depth == SocketClient.CONNECTION_RETRY_MAX) {
            return false
        }

        var result: Boolean? = null
        SocketClient.createInstance(ServerPath.ADDRESS, ServerPath.PORT_SOCKET, this).apply {
            onConnected = {
                Log.v("retryConnection: onConnected depth=$depth")
                result = true
            }
            onConnectionFailed = {
                Log.v("retryConnection: onConnectionFailed depth=$depth")
                SocketClient.releaseInstance()
                Thread.sleep(1000)
                result = retryConnection(depth + 1)
            }
        }.start()

        while (result == null) { // TODO: Use block
            Thread.sleep(500)
        }

        return result!!
    }
}