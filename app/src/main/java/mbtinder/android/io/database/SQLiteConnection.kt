package mbtinder.android.io.database

import mbtinder.lib.component.database.Query
import mbtinder.lib.component.database.QueryResult
import mbtinder.lib.util.*
import java.sql.Driver
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.*

class SQLiteConnection private constructor(filesDir: String): CloseableThread() {
    companion object {
        const val SELECT_MESSAGE_LIMIT = 20

        private var instance: SQLiteConnection? = null

        fun getInstance(): SQLiteConnection {
            if (instance == null) {
                throw RuntimeException("Instance never initialized")
            }

            return instance!!
        }

        fun createInstance(filesDir: String): SQLiteConnection {
            if (instance == null) {
                instance = SQLiteConnection(filesDir)
                instance!!.start()
                return instance!!
            } else {
//                throw RuntimeException("Instance already initialized")
                return instance!!
            }
        }

        fun isAlive() = instance != null
    }

    private val statement: Statement
    private val queries = IDList<Query>()
    private val results = IDList<QueryResult>()

    init {
        DriverManager.registerDriver(Class.forName("org.sqldroid.SQLDroidDriver").newInstance() as Driver)
        val connection = DriverManager.getConnection("jdbc:sqldroid:$filesDir/tables.db")
        statement = connection.createStatement()

        loop = {
            if (queries.isEmpty()) {
                sleep()
            } else {
                val query = sync(queries) { it.removeAt(0) }

                if (query.needResult) {
                    try {
                        val resultSet = statement.executeQuery(query.sql)
                        val queryResult =
                            QueryResult(query, resultSet)
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
        block(results) { !it.contains(queryId) }

        return sync(results) { it.remove(queryId) }
    }

    override fun close() {
        super.close()

        statement.close()
    }
}