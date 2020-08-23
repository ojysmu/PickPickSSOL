package mbtinder.android.io.database

import mbtinder.android.util.Log
import mbtinder.lib.component.database.Query
import mbtinder.lib.component.database.QueryResult
import mbtinder.lib.util.CloseableThread
import java.sql.Driver
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.*

class SQLiteConnection private constructor(filesDir: String): CloseableThread() {
    companion object {
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
            }

            return instance!!
        }

        fun isAlive() = instance != null
    }

    private val statement: Statement

    init {
        Log.v("SQLiteConnection.init 0")
        DriverManager.registerDriver(Class.forName("org.sqldroid.SQLDroidDriver").newInstance() as Driver)
        Log.v("SQLiteConnection.init 1")
        val connection = DriverManager.getConnection("jdbc:sqldroid:$filesDir/tables.db")
        Log.v("SQLiteConnection.init 2")
        statement = connection.createStatement()
        Log.v("SQLiteConnection.init 3")
    }

    fun executeQuery(sql: String): QueryResult {
        val query = Query(UUID.randomUUID(), sql)
        val resultSet = try {
            statement.executeQuery(sql)
        } catch (e: SQLException) {
            Log.e("Error occurred while running query: ${query.sql}", e)
            throw e
        }

        val queryResult = QueryResult(query, resultSet)
        resultSet.close()

        return queryResult
    }

    fun executeUpdate(sql: String) = statement.executeUpdate(sql)

    override fun close() {
        super.close()

        statement.close()
    }
}