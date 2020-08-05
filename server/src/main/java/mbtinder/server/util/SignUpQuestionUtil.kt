package mbtinder.server.util

import mbtinder.lib.component.SignUpQuestionContent
import mbtinder.lib.util.IDList
import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.database.component.Row

object SignUpQuestionUtil {
    private var questions: IDList<SignUpQuestionContent>? = null

    fun getQuestions(): IDList<SignUpQuestionContent> {
        if (questions == null) {
            val sql = "SELECT * FROM mbtinder.sign_up_questions"
            val rows = MySQLServer.getInstance().getResult(MySQLServer.getInstance().addQuery(sql)).content
            questions = rows.mapTo(IDList()) { buildSignUpQuestion(it) }
        }

        return questions!!
    }

    fun buildSignUpQuestion(row: Row) = SignUpQuestionContent(
        categoryId = row.getUUID("category_id"),
        questionId = row.getUUID("question_id"),
        question = row.getString("question"),
        selectable = row.getJSONArray("selectable")
    )
}