package mbtinder.lib.component.database

import mbtinder.lib.component.IDContent
import java.util.*

class Query(private val queryId: UUID, val sql: String): IDContent {
    companion object {
        private val NEED_RESULT = arrayOf("SHOW", "SELECT")
    }

    val needResult = needResult(sql.split(" ").toTypedArray()[0])

    private fun needResult(command: String) = NEED_RESULT.contains(command.toUpperCase())

    override fun getUUID() = queryId
}