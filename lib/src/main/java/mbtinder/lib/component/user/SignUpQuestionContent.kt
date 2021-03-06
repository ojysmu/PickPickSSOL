package mbtinder.lib.component.user

import mbtinder.lib.component.IDContent
import mbtinder.lib.component.json.JSONParsable
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class SignUpQuestionContent: JSONParsable,
    IDContent, Comparable<SignUpQuestionContent> {
    lateinit var categoryId: UUID
    lateinit var questionId: UUID
    lateinit var question: String
    lateinit var selectable: JSONArray
    var selected: Int = -1

    constructor(jsonObject: JSONObject) {
        categoryId = UUID.fromString(jsonObject.getString("category_id"))
        questionId = UUID.fromString(jsonObject.getString("question_id"))
        question = jsonObject.getString("question")
        selectable = jsonObject.getJSONArray("selectable")
        selected = jsonObject.getInt("selected")

        updateJSONObject()
    }

    constructor(categoryId: UUID, questionId: UUID, question: String, selectable: JSONArray) {
        this.categoryId = categoryId
        this.questionId = questionId
        this.question = question
        this.selectable = selectable

        updateJSONObject()
    }

    override fun getUUID() = questionId

    fun toConnectionForm() = ConnectionForm(this)

    class ConnectionForm: JSONParsable,
        IDContent, Comparable<ConnectionForm> {
        lateinit var categoryId: UUID
        lateinit var questionId: UUID
        var selected: Int = -1

        constructor(jsonObject: JSONObject) {
            categoryId = UUID.fromString(jsonObject.getString("category_id"))
            questionId = UUID.fromString(jsonObject.getString("question_id"))
            selected = jsonObject.getInt("selected")

            updateJSONObject()
        }

        constructor(categoryId: UUID, questionId: UUID, selected: Int) {
            this.categoryId = categoryId
            this.questionId = questionId
            this.selected = selected

            updateJSONObject()
        }

        constructor(signUpQuestionContent: SignUpQuestionContent) {
            this.categoryId = signUpQuestionContent.categoryId
            this.questionId = signUpQuestionContent.questionId
            this.selected = signUpQuestionContent.selected

            updateJSONObject()
        }

        override fun getUUID() = questionId

        override fun compareTo(other: ConnectionForm) = questionId.compareTo(other.questionId)
    }

    override fun compareTo(other: SignUpQuestionContent) = questionId.compareTo(other.questionId)
}