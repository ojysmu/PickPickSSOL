package mbtinder.lib.component

import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.sql.Date
import java.util.*

class UserContent: JSONParsable, IDContent, Comparable<UserContent> {
    lateinit var userId: UUID
    lateinit var email: String
    lateinit var password: String
    lateinit var nickname: String
    lateinit var birth: Date
    var gender: Int = 0
    lateinit var description: String
    var lastLocationLng: Double = 0.0
    var lastLocationLat: Double = 0.0

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(userId: UUID, email: String, password: String, nickname: String, birth: Date,
                gender: Int, description: String, lastLocationLng: Double, lastLocationLat: Double) {
        this.userId = userId
        this.email = email
        this.password = password
        this.nickname = nickname
        this.birth = birth
        this.gender = gender
        this.description = description
        this.lastLocationLng = lastLocationLng
        this.lastLocationLat = lastLocationLat

        updateJSONObject()
    }

    fun getInsertSql() = "INSERT INTO mbtinder.user (" +
            "user_id, email, password, nickname, birth, gender, description, last_location_lng, last_location_lat" +
            ") VALUES (" +
            "'$userId', '$email', '$password', '$nickname', '$birth', $gender, '$description', $lastLocationLng, $lastLocationLng)"

    fun getUpdateSql() = "UPDATE mbtinder.user SET password='$password', nickname='$nickname', description='$description' WHERE user_id='$userId'"

    override fun getUUID() = userId

    override fun compareTo(other: UserContent) = userId.compareTo(other.userId)
}