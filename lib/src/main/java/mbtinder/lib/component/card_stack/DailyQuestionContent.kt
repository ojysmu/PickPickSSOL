package mbtinder.lib.component.card_stack

import mbtinder.lib.component.IDContent
import mbtinder.lib.component.database.Row
import mbtinder.lib.component.json.JSONContent
import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.sql.Date
import java.util.*

open class DailyQuestionContent: BaseCardStackContent, JSONParsable, IDContent, Comparable<DailyQuestionContent> {
    lateinit var questionId: UUID
    lateinit var title: String
    lateinit var nopeContent: String
    lateinit var pickContent: String
    lateinit var date: Date

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(questionId: UUID, title: String, nopeContent: String, pickContent: String, date: Date) {
        this.questionId = questionId
        this.title = title
        this.nopeContent = nopeContent
        this.pickContent = pickContent
        this.date = date

        updateJSONObject()
    }

    override fun getUUID() = questionId

    override fun compareTo(other: DailyQuestionContent) = date.compareTo(other.date)

    class SaveForm(val questionId: UUID, val isPicked: Int): IDContent {
        constructor(row: Row): this(row.getUUID("question_id"), row.getInt("is_picked"))

        override fun getUUID() = questionId
    }
}