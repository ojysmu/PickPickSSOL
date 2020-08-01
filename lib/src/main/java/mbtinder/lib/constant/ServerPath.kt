package mbtinder.lib.constant

import java.util.*

object ServerPath {
    const val WEB_PROTOCOL = "http"
//    const val ADDRESS = "==========FILL HERE=========="
//    const val ADDRESS = "10.211.55.6"
    const val ADDRESS = "172.31.50.82"
    const val PORT_WEB = 80
    const val PORT_SOCKET = 8080

    fun getUserImageUrl(userId: UUID, imageName: String) =
        "$WEB_PROTOCOL://$ADDRESS:$PORT_WEB/raw/$userId/$imageName"
}