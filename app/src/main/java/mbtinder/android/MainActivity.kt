package mbtinder.android

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import mbtinder.android.io.socket.SocketClient
import mbtinder.android.service.ThreadService
import mbtinder.android.ui.model.Activity
import mbtinder.android.util.DialogFactory
import mbtinder.android.util.Log
import mbtinder.android.util.runOnBackground
import mbtinder.lib.constant.ServerPath
import mbtinder.lib.util.block
import java.io.IOException

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            initializeSocketClient(true)
        } catch (e: RuntimeException) {
            SocketClient.releaseInstance(false)
            initializeSocketClient(false)
        }

        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setOnNavigationItemReselectedListener { /* 재선택시 refresh 방지 */ }
        navView.setupWithNavController(navController)
        navView.setOnNavigationItemSelectedListener { item ->
            val navOptionsBuilder = NavOptions.Builder()
                .setLaunchSingleTop(true)
                .setEnterAnim(R.anim.nav_default_enter_anim)
                .setExitAnim(R.anim.nav_default_exit_anim)
                .setPopEnterAnim(R.anim.nav_default_pop_enter_anim)
                .setPopExitAnim(R.anim.nav_default_pop_exit_anim)
            if ((item.order and Menu.CATEGORY_SECONDARY) == 0) {
                var startDestination: NavDestination? = navController.graph
                while (startDestination is NavGraph) {
                    val parent = startDestination
                    startDestination = parent.findNode(parent.startDestination)
                }
                navOptionsBuilder.setPopUpTo(startDestination!!.id, true)
            }
            navController.popBackStack()
            navController.navigate(item.itemId, null, navOptionsBuilder.build())
            true
        }

        startForegroundService(Intent(this, ThreadService::class.java))
    }

    private fun initializeSocketClient(setDisconnected: Boolean) {
        val socketClient = SocketClient.createInstance(ServerPath.ADDRESS, ServerPath.PORT_SOCKET, this)
        socketClient.onConnected = this::onConnected
        if (setDisconnected) {
            socketClient.onConnectionFailed = this::onConnectionFailed
            socketClient.onDisconnected = this::onDisconnected
        }
        socketClient.start()
    }

    private fun onConnected() {
        Log.v("onConnected()")
    }

    private fun onConnectionFailed(e: IOException) {
        Log.e("onConnectionFailed", e)

        SocketClient.releaseInstance(false)
        runOnUiThread {
            DialogFactory.getContentedDialog(this, R.string.socket_on_connection_failed, this::finish).show()
        }
    }

    private fun onDisconnected(e: IOException) {
        Log.e("onDisconnected", e)

        SocketClient.releaseInstance(false)
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
                SocketClient.releaseInstance(false)
                Thread.sleep(1000)
                result = retryConnection(depth + 1)
            }
        }.start()

        block(500) { result == null }

        return result!!
    }
}