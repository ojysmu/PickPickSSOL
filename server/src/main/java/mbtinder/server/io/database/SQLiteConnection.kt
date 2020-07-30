package mbtinder.server.io.database

import mbtinder.lib.component.IDContent
import mbtinder.lib.util.CloseableThread
import mbtinder.lib.util.IDList
import mbtinder.server.constant.LocalFile
import mbtinder.server.io.database.component.Query
import mbtinder.server.io.database.component.QueryResult
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.*

class SQLiteConnection private constructor(val userId: UUID): CloseableThread(), IDContent {
    companion object {
        const val SELECT_MESSAGE_LIMIT = 20

        val dbHeader = "jdbc:sqlite:${LocalFile.fileRoot}/user"

        private val connections = IDList<SQLiteConnection>()

        fun getConnection(userId: UUID): SQLiteConnection = synchronized(connections) {
            return connections.find { it.userId == userId }
                ?: SQLiteConnection(userId).apply { connections.add(this) }
        }

        fun getCreateChatSql(chatId: UUID) = "CREATE TABLE $chatId (" +
                "`_id`         INT NOT NULL AUTO_INCREMENT , " +
                "`sender_id`   CHAR(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , " +
                "`receiver_id` CHAR(36) CHARACTER SET utf8 COLLATE utf8_general_ci NOT NULL , " +
                "`timestamp`   BIGINT NOT NULL , " +
                "`body`        VARCHAR(200) NOT NULL , " +
                "PRIMARY KEY (`_id`))"

        fun getInsertNewChatSql(chatId: UUID, participantId: UUID) =
            "INSERT INTO chat (chat_id, receiver_id) VALUES ('${chatId}', '${participantId}')"

        fun getSelectMessageSql(chatId: UUID, endIndex: Int): String {
            return "SELECT * FROM $chatId LIMIT ${endIndex - SELECT_MESSAGE_LIMIT}, $SELECT_MESSAGE_LIMIT"
        }
    }

    private val statement: Statement
    private val queries = IDList<Query>()
    private val results = IDList<QueryResult>()

    init {
        val connection = DriverManager.getConnection("$dbHeader/tables.db")
        statement = connection.createStatement()

        loop = {
            if (queries.isEmpty()) {
                sleep()
            } else {
                val query = queries.removeAt(0)

                if (query.needResult) {
                    try {
                        val resultSet = statement.executeQuery(query.sql)
                        val queryResult = QueryResult(query, resultSet)
                        resultSet.close()
                        results.add(queryResult)
                    } catch (e: SQLException) {
                        System.err.println("Error occurred while running query: ${query.sql}")
                        e.printStackTrace()
                    }
                } else {
                    try {
                        statement.executeUpdate(query.sql)
                    } catch (e: SQLException) {
                        System.err.println("Error occurred while running query: ${query.sql}")
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    fun addQuery(sql: String): UUID {
        val queryId = UUID.randomUUID()
        queries.add(Query(queryId, sql))

        return queryId
    }

    fun getResult(queryId: UUID): QueryResult {
        while (!results.contains(queryId)) {
            sleep()
        }
        return results.remove(queryId)
    }

    override fun getUUID() = userId
}