package mbtinder.android.component

import mbtinder.android.io.database.RowContent
import mbtinder.lib.component.card_stack.DailyQuestionContent
import org.sqldroid.SQLDroidResultSet
import java.util.*

class LocalDailyQuestionContent(resultSet: SQLDroidResultSet): DailyQuestionContent(
    UUID.fromString(resultSet.getString("question_id")),
    resultSet.getString("title"),
    resultSet.getString("nope_content"),
    resultSet.getString("pick_content"),
    resultSet.getDate("date")
), RowContent {
    val _id = resultSet.getInt("_id")
}