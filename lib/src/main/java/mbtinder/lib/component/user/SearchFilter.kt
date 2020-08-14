package mbtinder.lib.component.user

import mbtinder.lib.component.CardStackContent
import mbtinder.lib.component.IDContent
import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.util.*

class SearchFilter: JSONParsable, IDContent {
    lateinit var userId: UUID
    var gender: Int = 2
    var ageStart: Int = 20
    var ageEnd: Int = 100
    var distance: Int = 0

    constructor(jsonObject: JSONObject): super(jsonObject)

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

    override fun getUUID() = userId

    fun isInRange(userCoordinator: Coordinator, cardStackContent: CardStackContent) =
        gender == cardStackContent.gender
                && ageStart < cardStackContent.age
                && ageEnd > cardStackContent.age
                && distance > userCoordinator.getDistance(cardStackContent.coordinator)
}