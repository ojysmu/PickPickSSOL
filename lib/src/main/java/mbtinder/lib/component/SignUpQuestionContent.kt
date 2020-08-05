package mbtinder.lib.component

import mbtinder.lib.component.json.JSONParsable
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

class SignUpQuestionContent: JSONParsable, IDContent {
    lateinit var categoryId: UUID
    lateinit var questionId: UUID
    lateinit var question: String
    lateinit var selectable: JSONArray
    var selected = -1

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(categoryId: UUID, questionId: UUID, question: String, selectable: JSONArray) {
        this.categoryId = categoryId
        this.questionId = questionId
        this.question = question
        this.selectable = selectable

        updateJSONObject()
    }

    override fun getUUID() = questionId
}