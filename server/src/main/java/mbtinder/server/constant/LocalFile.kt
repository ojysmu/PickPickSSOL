package mbtinder.server.constant

import mbtinder.lib.component.InterestContent
import mbtinder.lib.util.loadJSONList
import java.util.*

object LocalFile {
    val fileRoot = "${System.getProperty("user.home")}/raw"
    val userRoot = "$fileRoot/user"

    val userInterestList = "interests.json"

    fun getUserInterestPath(userId: UUID) = "$userRoot/$userId/$userInterestList"
    fun getUserInterestList(userId: UUID) = loadJSONList<InterestContent>(getUserInterestPath(userId))
    fun getUserImagePath(userId: UUID) = "$userRoot/$userId"
}