package mbtinder.android

import android.os.Bundle
import androidx.navigation.Navigation.findNavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import mbtinder.android.io.SocketClient
import mbtinder.android.ui.fragment.splash.SplashFragment
import mbtinder.android.ui.model.Activity
import mbtinder.android.util.Log
import mbtinder.android.util.SharedPreferencesUtil
import mbtinder.android.util.SharedPreferencesUtil.PREF_ACCOUNT
import mbtinder.lib.constant.ServerPath

class MainActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView = findViewById<BottomNavigationView>(R.id.nav_view)
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        navView.setupWithNavController(navController)
    }

    private fun initializeNotifications() {

    }

    private fun initializeSocketClient() {
        val socketClient = SocketClient.createInstance(ServerPath.ADDRESS, ServerPath.PORT_SOCKET)
        socketClient.onConnected = {
            Log.v("onConnected()")
        }
        socketClient.onConnectionFailed = {
            Log.e("onConnectionFailed", it)
        }
        socketClient.onDisconnected = {
            Log.e("onDisconnected", it)
        }
        socketClient.start()
    }
}