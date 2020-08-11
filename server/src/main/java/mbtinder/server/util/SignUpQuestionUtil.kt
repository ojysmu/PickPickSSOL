package mbtinder.server.util

import mbtinder.lib.component.SignUpQuestionContent
import mbtinder.lib.util.IDList
import mbtinder.lib.util.toIDList
import mbtinder.server.io.database.MySQLServer
import mbtinder.lib.component.database.Row
import java.util.*

object SignUpQuestionUtil {
    private var questions: IDList<SignUpQuestionContent>? = null

    private fun initializeQuestions() {
        if (questions == null) {
            val sql = "SELECT * FROM mbtinder.sign_up_question"
            val rows = MySQLServer.getInstance().getResult(MySQLServer.getInstance().addQuery(sql)).content
            questions = rows.map { buildSignUpQuestion(it) }.sorted().toIDList()
        }
    }

    fun getQuestions(): IDList<SignUpQuestionContent> {
        if (questions == null) {
            initializeQuestions()
        }

        return questions!!
    }

    fun parseFilled(list: List<SignUpQuestionContent.ConnectionForm>) =
        list.map { findByQuestionId(it.questionId).apply { selected = it.selected }}

    fun findByQuestionId(questionId: UUID): SignUpQuestionContent {
        if (questions == null) {
            initializeQuestions()
        }

        return questions!![questions!!.binarySearch { it.questionId.compareTo(questionId) }]
    }

    fun buildSignUpQuestion(row: Row) = SignUpQuestionContent(
        categoryId = row.getUUID("category_id"),
        questionId = row.getUUID("question_id"),
        question = row.getString("question"),
        selectable = row.getJSONArray("selectable")
    )
}