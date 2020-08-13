package mbtinder.lib.component

import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

class Coordinator: JSONParsable {
    var longitude: Double = 0.0
    var latitude: Double = 0.0

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(longitude: Double, latitude: Double) {
        this.longitude = longitude
        this.latitude = latitude

        updateJSONObject()
    }

    fun getDistance(other: Coordinator): Double {
        val r = 6378.137
        val dLat = (other.latitude - latitude) * Math.PI / 180
        val dLng = (other.longitude - longitude) * Math.PI / 180
        val a = sin(dLat/2) * sin(dLat/2) +
                cos(latitude * Math.PI / 180) * cos(other.latitude * Math.PI / 180) *
                sin(dLng/2) * sin(dLng/2)
        val c = 2 * atan2(sqrt(a), sqrt(1-a))
        return r * c * 1000
    }
}