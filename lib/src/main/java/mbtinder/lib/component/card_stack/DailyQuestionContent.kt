package mbtinder.lib.component.card_stack

import mbtinder.lib.component.IDContent
import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.util.*

class DailyQuestionContent: BaseCardStackContent, JSONParsable, IDContent {
    lateinit var questionId: UUID
    lateinit var title: String
    lateinit var nopeContent: String
    lateinit var pickContent: String

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(questionId: UUID, title: String, nopeContent: String, pickContent: String) {
        this.questionId = questionId
        this.title = title
        this.nopeContent = nopeContent
        this.pickContent = pickContent
    }

    override fun getUUID() = questionId
}