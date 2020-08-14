package mbtinder.lib.constant

import java.util.*

object ServerPath {
    const val WEB_PROTOCOL = "http"
//    const val ADDRESS = "==========FILL HERE=========="
//    const val ADDRESS = "10.211.55.6"
    const val ADDRESS = "172.31.14.50"
    const val PORT_WEB = 80
    const val PORT_SOCKET = 8080

    fun getUserDatabaseUrl(userId: UUID) = "$WEB_PROTOCOL://$ADDRESS:$PORT_WEB/raw/user/$userId/table.db"

    fun getUserImageUrl(userId: UUID, imageName: String) =
        "$WEB_PROTOCOL://$ADDRESS:$PORT_WEB/raw/user/$userId/$imageName"
}