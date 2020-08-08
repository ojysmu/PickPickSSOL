package mbtinder.lib.component

import mbtinder.lib.component.json.JSONParsable
import mbtinder.lib.constant.PasswordQuestion
import org.json.JSONObject
import java.io.File
import java.sql.Date
import java.util.*

class UserContent: JSONParsable, IDContent, Comparable<UserContent> {
    lateinit var userId: UUID
    lateinit var email: String
    lateinit var password: String
    lateinit var name: String
    var age: Int = 0
    var gender: Int = 0
    lateinit var description: String
    var lastLocationLng: Double = 0.0
    var lastLocationLat: Double = 0.0
    var passwordQuestionId: Int = -1
    lateinit var passwordAnswer: String

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(userId: UUID, email: String, password: String, name: String, age: Int, gender: Int,
                description: String, lastLocationLng: Double, lastLocationLat: Double,
                passwordQuestionId: Int, passwordAnswer: String) {
        this.userId = userId
        this.email = email
        this.password = password
        this.name = name
        this.age = age
        this.gender = gender
        this.description = description
        this.lastLocationLng = lastLocationLng
        this.lastLocationLat = lastLocationLat
        this.passwordQuestionId = passwordQuestionId
        this.passwordAnswer = passwordAnswer

        updateJSONObject()
    }

    fun getInsertSql() = "INSERT INTO mbtinder.user (" +
            "user_id, email, password, name, age, gender, description, last_location_lng, " +
            "last_location_lat, password_question, password_answer" +
            ") VALUES (" +
            "'$userId', '$email', '$password', '$name', '$age', $gender, '$description', $lastLocationLng, " +
            "$lastLocationLng, $passwordQuestionId, '$passwordAnswer')"

    fun getUpdateSql() = "UPDATE mbtinder.user SET " +
            "password='$password', " +
            "name='$name', " +
            "description='$description', " +
            "password_question=$passwordQuestionId, " +
            "password_answer='$passwordAnswer' " +
            "WHERE user_id='$userId'"

    override fun getUUID() = userId

    override fun compareTo(other: UserContent) = userId.compareTo(other.userId)
}