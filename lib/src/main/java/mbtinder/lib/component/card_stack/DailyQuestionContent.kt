package mbtinder.lib.component.card_stack

import mbtinder.lib.component.IDContent
import mbtinder.lib.component.database.Row
import mbtinder.lib.component.json.JSONParsable
import mbtinder.lib.util.getUUID
import org.json.JSONObject
import java.sql.Date
import java.util.*

class DailyQuestionContent: BaseCardStackContent, JSONParsable, IDContent, Comparable<DailyQuestionContent> {
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

    class SaveForm(row: Row) {
        val questionId: UUID = row.getUUID("question_id")
        val isPicked: Int = row.getInt("is_picked")
    }
}