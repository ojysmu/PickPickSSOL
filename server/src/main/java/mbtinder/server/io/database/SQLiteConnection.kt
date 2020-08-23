package mbtinder.server.io.database

import mbtinder.lib.component.ChatContent
import mbtinder.lib.component.IDContent
import mbtinder.lib.util.CloseableThread
import mbtinder.lib.util.IDList
import mbtinder.lib.util.block
import mbtinder.lib.util.sync
import mbtinder.server.constant.LocalFile
import mbtinder.lib.component.database.Query
import mbtinder.lib.component.database.QueryResult
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.*

class SQLiteConnection private constructor(val userId: UUID): CloseableThread(), IDContent {
    companion object {
        const val SELECT_MESSAGE_LIMIT = 20

        val dbHeader = "jdbc:sqlite:${LocalFile.userRoot}"

        private val connections = IDList<SQLiteConnection>()

        fun getConnection(userId: UUID) = sync(connections) { connections: IDList<SQLiteConnection> ->
            connections.find { it.userId == userId } ?:
                    SQLiteConnection(userId).apply { connections.add(this); start() }
        }

        fun getCreateChatSql(chatId: UUID) = "CREATE TABLE '$chatId' (" +
                "_id         INTEGER PRIMARY KEY AUTOINCREMENT , " +
                "sender_id   CHAR(36) NOT NULL , " +
                "receiver_id CHAR(36) NOT NULL , " +
                "timestamp   BIGINT NOT NULL , " +
                "body        VARCHAR(200) NOT NULL)"

        fun getInsertNewChatSql(chatContent: ChatContent) =
            "INSERT INTO chat (" +
                    "chat_id, receiver_id, receiver_name) VALUES (" +
                    "'${chatContent.chatId}', '${chatContent.opponentId}', '${chatContent.opponentName}')"

    }

    private val statement: Statement
    private val queries = IDList<Query>()
    private val results = IDList<QueryResult>()

    private var alive: Int = 0

    init {
        val connection = DriverManager.getConnection("$dbHeader/$userId/tables.db")
        statement = connection.createStatement()

        loop = {
            if (alive > 60 * 1000) {
                connections.remove(this)
                close()
            }

            if (queries.isEmpty()) {
                sleep()
                alive += intervalInMillis.toInt()
            } else {
                alive = 0
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

    fun getResult(queryId: UUID) = block(results, intervalInMillis) { !it.contains(queryId) }.remove(queryId)

    override fun close() {
        super.close()

        statement.close()
    }

    override fun getUUID() = userId
}