package mbtinder.lib.component.database

import mbtinder.lib.component.IDContent
import mbtinder.lib.component.database.Query
import mbtinder.lib.component.database.Row
import java.sql.ResultSet
import java.util.ArrayList

class QueryResult(private val query: Query, resultSet: ResultSet): IDContent {
    val content = parseContent(resultSet)

    override fun getUUID() = query.getUUID()

    private fun parseContent(resultSet: ResultSet): ArrayList<Row> {
        val metadata = resultSet.metaData
        val columns = metadata.columnCount
        val contents = ArrayList<Row>()

        while (resultSet.next()) {
            val row = Row()
            for (i in 1 .. columns) {
                row[metadata.getColumnName(i)] = resultSet.getObject(i)
            }
            contents.add(row)
        }

        return contents
    }

    fun getRowCount() = content.size

    fun getRow(rowIndex: Int) = content[rowIndex]
}