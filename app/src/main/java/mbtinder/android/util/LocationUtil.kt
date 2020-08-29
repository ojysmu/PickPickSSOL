package mbtinder.android.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import mbtinder.android.ui.model.Fragment
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.util.block

object LocationUtil {
    private const val LOCATION_REQUEST_CODE = 0x00

    private fun getLocation(context: Context, onLocationChangedListener: (Location) -> Unit) {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val locationListener = object : LocationListener {
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                onLocationChangedListener.invoke(location)
            }
        }

        runOnUiThread {
            if (checkLocationPermission(context)) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1L, 0f, locationListener)
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1L, 0f, locationListener)
            }
        }
    }

    fun checkLocationPermission(context: Context) =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED

    fun requestLocationPermission(fragment: Fragment) =
        fragment.requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_REQUEST_CODE)

    fun isLocationPermissionGranted(requestCode: Int, grantResults: IntArray) =
        requestCode == LOCATION_REQUEST_CODE && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED

    fun onLocationPermissionGranted(context: Context): Coordinator {
        var coordinator: Coordinator? = null

        getLocation(context) {
            if (coordinator == null) {
                coordinator = Coordinator(it.longitude, it.latitude)
            }
        }

        block { coordinator == null }
        return coordinator!!
    }
}