package mbtinder.server.util

import mbtinder.lib.component.UserContent
import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.database.component.Row
import java.util.*

object UserUtil {
    private const val UPDATE_DURATION = 60 * 1000

    private var users: List<UserContent> = updateUsers()
    private var lastUpdate: Long = 0

    private fun updateUsers(): List<UserContent> {
        val sql = "SELECT * FROM mbtinder.user"
        val queryId = MySQLServer.getInstance().addQuery(sql)
        val queryResult = MySQLServer.getInstance().getResult(queryId)

        lastUpdate = System.currentTimeMillis()
        return queryResult.content.map { buildUser(it) }.sorted()
    }

    private fun ensureUpdate() {
        if (lastUpdate < System.currentTimeMillis() - UPDATE_DURATION) {
            updateUsers()
        }
    }

    private fun buildUser(row: Row) = UserContent(
        row.getUUID("user_id"), row.getString("email"), row.getString("password"),
        row.getString("name"), row.getInt("age"), row.getInt("gender"),
        row.getString("description"), row.getDouble("last_location_lng"),
        row.getDouble("last_location_lat"), row.getString("password_question"),
        row.getString("password_answer")
    )

    fun getUser(userId: UUID, withPassword: Boolean = false): UserContent? = synchronized(users) {
        ensureUpdate()

        val index = users.binarySearch { it.userId.compareTo(userId) }
        return if (index >= 0) {
            if (!withPassword) {
                users[index].password = ""
                users[index].passwordAnswer = ""
            }
            users[index]
        } else {
            updateUsers()
            val found = users.find { it.userId == userId }
            if (!withPassword) {
                found?.password = ""
                found?.passwordAnswer = ""
            }
            return found
        }
    }

    fun getUserByEmail(email: String): UserContent? = synchronized(users) {
        ensureUpdate()

        val found = users.find { it.email == email }
        return if (found != null) {
            found
        } else {
            updateUsers()
            return users.find { it.email == email }
        }
    }
}