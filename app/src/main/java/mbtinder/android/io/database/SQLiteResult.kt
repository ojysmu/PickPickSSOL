package mbtinder.android.io.database

import mbtinder.lib.component.IDContent
import mbtinder.lib.component.database.Query
import org.sqldroid.SQLDroidResultSet
import java.util.*

class SQLiteResult<T: RowContent>(private val query: Query, resultSet: SQLDroidResultSet, private val clazz: Class<T>): IDContent {
    val content = parseContent(resultSet)

    override fun getUUID() = query.getUUID()

    private fun parseContent(resultSet: SQLDroidResultSet): ArrayList<T> {
        val contents = ArrayList<T>()
        while (resultSet.next()) {
            contents.add(clazz.getDeclaredConstructor(SQLDroidResultSet::class.java).newInstance(resultSet))
        }

        return contents
    }

    fun getRowCount() = content.size

    fun getRow(rowIndex: Int) = content[rowIndex]
}