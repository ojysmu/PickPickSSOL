package mbtinder.android.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import androidx.core.content.ContextCompat
import mbtinder.android.io.http.HttpConnection
import mbtinder.android.io.http.RequestMethod
import mbtinder.android.ui.model.Fragment
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.util.block
import org.json.JSONObject

object LocationUtil {
    private const val REST_API_KEY = "fa565989920a419ce53bd66f1d1467cc"
    private const val COORDINATE_TO_ADDRESS = "https://dapi.kakao.com/v2/local/geo/coord2address.json"
    private const val ADDRESS_TO_COORDINATE = "https://dapi.kakao.com/v2/local/search/address.json"

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

    fun getAddress(coordinator: Coordinator): String {
        val httpConnection = HttpConnection(
            rawUrl = "$COORDINATE_TO_ADDRESS?x=${coordinator.longitude}&y=${coordinator.latitude}&input_coord=WGS84",
            requestMethod = RequestMethod.GET,
            isHttps = true,
            requestParameters = listOf(Pair("Authorization", "KakaoAK $REST_API_KEY"))
        )
        val rawResult = httpConnection.execute().get()
        val result = JSONObject(rawResult)
        Log.v("getOldAddress(): result=$result")
        return if (result.getJSONObject("meta").getInt("total_count") == 1) {
            val rawContents = result.getJSONArray("documents").getJSONObject(0)
            if (!rawContents.isNull("address")) {
                rawContents.getJSONObject("address").getString("address_name")
            } else {
                ""
            }
        } else {
            ""
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