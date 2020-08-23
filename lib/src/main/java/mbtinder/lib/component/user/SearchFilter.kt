package mbtinder.lib.component.user

import mbtinder.lib.component.card_stack.CardStackContent
import mbtinder.lib.component.IDContent
import mbtinder.lib.component.database.Row
import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.util.*

class SearchFilter: JSONParsable, IDContent {
    lateinit var userId: UUID
    var gender: Int = 2
    var ageStart: Int = 20
    var ageEnd: Int = 100
    var distance: Int = 100

    constructor(jsonObject: JSONObject): super(jsonObject) {
        this.gender = jsonObject.getInt("gender")
        this.ageStart = jsonObject.getInt("age_start")
        this.ageEnd = jsonObject.getInt("age_end")
        this.distance = jsonObject.getInt("distance")
    }

    constructor(userId: UUID) {
        this.userId = userId

        updateJSONObject()
    }

    constructor(userId: UUID, gender: Int, ageStart: Int, ageEnd: Int, distance: Int) {
        this.userId = userId
        this.gender = gender
        this.ageStart = ageStart
        this.ageEnd = ageEnd
        this.distance = distance

        updateJSONObject()
    }

    constructor(row: Row) {
        this.userId = row.getUUID("user_id")
        this.gender = row.getInt("filter_gender")
        this.ageStart = row.getInt("filter_age_start")
        this.ageEnd = row.getInt("filter_age_end")
        this.distance = row.getInt("filter_distance")

        updateJSONObject()
    }

    fun isInRange(userCoordinator: Coordinator, cardStackContent: CardStackContent) =
        // 모두 또는 해당하는 성별
        (gender == 2 || gender == cardStackContent.gender)
                // 최소 나이 이상인지
                && ageStart <= cardStackContent.age // FIXME
                // 최대 나이 이하인지
                && ageEnd >= cardStackContent.age
                // 설정한 거리 이내인지
                && distance > (userCoordinator.getDistance(cardStackContent.coordinator) / 1000)

    fun getUpdateSql() = "UPDATE pickpick.user " +
            "SET filter_gender=$gender, filter_age_start=$ageStart, filter_age_end=$ageEnd, filter_distance=$distance " +
            "WHERE user_id='$userId'"

    override fun getUUID() = userId
}