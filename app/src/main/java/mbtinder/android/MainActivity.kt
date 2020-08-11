package mbtinder.android

import android.content.Intent
import android.os.Bundle
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import mbtinder.android.io.SQLiteConnection
import mbtinder.android.io.SocketClient
import mbtinder.android.service.ThreadService
import mbtinder.android.ui.fragment.splash.SplashFragment
import mbtinder.android.ui.model.Activity
import mbtinder.android.util.DialogFactory
import mbtinder.android.util.Log
import mbtinder.android.util.SharedPreferencesUtil
import mbtinder.android.util.SharedPreferencesUtil.PREF_ACCOUNT
import mbtinder.android.util.ThreadUtil
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
            initializeSQLiteConnection()
        }

        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
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

    private fun initializeSQLiteConnection() {
        SQLiteConnection.createInstance(this).start()
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
        ThreadUtil.runOnBackground {
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

        while (result == null) {
            Thread.sleep(500)
        }

        return result!!
    }
}