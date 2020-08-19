package mbtinder.server.util

import mbtinder.lib.component.card_stack.DailyQuestionContent
import mbtinder.lib.component.database.Row

object DailyQuestionUtil {
    fun buildDailyQuestion(row: Row) = DailyQuestionContent(
        row.getUUID("question_id"),
        row.getString("title"),
        row.getString("nope_content"),
        row.getString("pick_content"),
        row.getDate("date")
    )
}