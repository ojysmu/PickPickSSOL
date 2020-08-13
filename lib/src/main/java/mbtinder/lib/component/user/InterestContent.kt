package mbtinder.lib.component.user

import mbtinder.lib.component.IDContent
import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.util.*

class InterestContent: JSONParsable, IDContent {
    lateinit var questionId: UUID
    lateinit var question: String
    var answer: Boolean = false
    var isDaily: Boolean = false

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(questionId: UUID, question: String, answer: Boolean, isDaily: Boolean) {
        this.questionId = questionId
        this.question = question
        this.answer = answer
        this.isDaily = isDaily

        updateJSONObject()
    }

    override fun getUUID() = questionId
}