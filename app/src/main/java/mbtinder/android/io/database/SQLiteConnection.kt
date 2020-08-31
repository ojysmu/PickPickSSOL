package mbtinder.android.io.database

import mbtinder.android.util.Log
import mbtinder.lib.component.database.Query
import mbtinder.lib.component.database.QueryResult
import org.sqldroid.SQLDroidConnection
import org.sqldroid.SQLDroidResultSet
import org.sqldroid.SQLDroidStatement
import java.io.File
import java.sql.*
import java.util.*

class SQLiteConnection private constructor(databaseDir: File): AutoCloseable {
    companion object {
        private var instance: SQLiteConnection? = null

        fun releaseInstance() {
            instance?.close()
            instance = null
        }

        fun getInstance(): SQLiteConnection {
            if (instance == null) {
                throw RuntimeException("Instance never initialized")
            }

            return instance!!
        }

        fun createInstance(databaseDir: File): SQLiteConnection {
            instance?.let {
                it.close()
                instance = null
            }
            instance = SQLiteConnection(databaseDir)

            return instance!!
        }
    }

    private val connection: SQLDroidConnection
    private val statement: SQLDroidStatement

    init {
        DriverManager.registerDriver(Class.forName("org.sqldroid.SQLDroidDriver").newInstance() as Driver)
        connection = DriverManager.getConnection("jdbc:sqldroid:${databaseDir.absolutePath}") as SQLDroidConnection
        statement = connection.createStatement() as SQLDroidStatement
    }

    fun <T: RowContent> executeQuery(sql: String, clazz: Class<T>): SQLiteResult<T> {
        val query = Query(UUID.randomUUID(), sql)
        val resultSet = try {
            statement.executeQuery(sql) as SQLDroidResultSet
        } catch (e: SQLException) {
            Log.e("Error occurred while running query: $sql", e)
            throw e
        }

        val queryResult = SQLiteResult(query, resultSet, clazz)
        resultSet.close()

        return queryResult
    }

    inline fun <reified T: RowContent> executeQuery(sql: String): SQLiteResult<T> {
        return executeQuery(sql, T::class.java)
    }

    fun getRowId(sql: String): Int {
        val resultSet = try {
            statement.executeQuery(sql) as SQLDroidResultSet
        } catch (e: SQLException) {
            Log.e("Error occurred while running query: $sql", e)
            throw e
        }

        val rowId = if (resultSet.next()) resultSet.getInt("_id") else -1
        resultSet.close()

        return rowId
    }

    fun getInts(table: String, column: String): List<Int> {
        val sql = "SELECT $column FROM $table"
        val resultSet = try {
            statement.executeQuery(sql)
        } catch (e: SQLException) {
            Log.e("Error occurred while running query: $sql", e)
            throw e
        }

        val result = ArrayList<Int>()
        while (resultSet.next()) {
            result.add(resultSet.getInt(column))
        }
        resultSet.close()

        return result
    }

    fun executeUpdate(sql: String) = statement.executeUpdate(sql)

    override fun close() {
        statement.close()
        connection.close()
    }
}