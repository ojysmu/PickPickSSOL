package mbtinder.server.io.database

import mbtinder.lib.component.IDContent
import mbtinder.lib.util.CloseableThread
import mbtinder.lib.util.IDList
import mbtinder.lib.util.block
import mbtinder.lib.util.sync
import mbtinder.server.constant.LocalFile
import mbtinder.server.io.database.component.Query
import mbtinder.server.io.database.component.QueryResult
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.*

class SQLiteConnection private constructor(val userId: UUID): CloseableThread(), IDContent {
    companion object {
        private const val SELECT_MESSAGE_LIMIT = 20

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

        fun getInsertNewChatSql(chatId: UUID, participantId: UUID) =
            "INSERT INTO chat (chat_id, receiver_id) VALUES ('$chatId', '$participantId')"

        fun getSelectMessageSql(chatId: UUID, endIndex: Int) =
            "SELECT * FROM '$chatId' LIMIT ${endIndex - SELECT_MESSAGE_LIMIT}, $SELECT_MESSAGE_LIMIT"

        fun getInsertFirstMessageSql(chatId: UUID, senderId: UUID, receiverId: UUID): String {
            return "INSERT INTO '$chatId' " +
                    "(sender_id, receiver_id, timestamp, body) VALUES " +
                    "('$senderId', '$receiverId', ${System.currentTimeMillis()}, '매칭되었습니다.')"
        }
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
                val query = sync(queries) { it.removeAt(0) }

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
        block(results, intervalInMillis) { !it.contains(queryId) }

        return sync(results) { it.remove(queryId) }
    }

    override fun close() {
        super.close()

        statement.close()
    }

    override fun getUUID() = userId
}