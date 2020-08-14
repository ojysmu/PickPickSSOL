package mbtinder.lib.component.user

import mbtinder.lib.component.CloneableContent
import mbtinder.lib.component.IDContent
import mbtinder.lib.component.json.JSONParsable
import org.json.JSONObject
import java.util.*

class UserContent: JSONParsable,
    IDContent, Comparable<UserContent>,
    CloneableContent<UserContent> {
    lateinit var userId: UUID
    lateinit var email: String
    lateinit var password: String
    lateinit var name: String
    var age: Int = 0
    var gender: Int = 0
    var notification: Boolean = true
    lateinit var description: String
    var lastLocationLng: Double = 0.0
    var lastLocationLat: Double = 0.0
    var passwordQuestionId: Int = -1
    lateinit var passwordAnswer: String
    lateinit var searchFilter: SearchFilter

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(userId: UUID, email: String, password: String, name: String, age: Int, gender: Int,
                notification: Boolean, description: String, lastLocationLng: Double, lastLocationLat: Double,
                passwordQuestionId: Int, passwordAnswer: String, searchFilter: SearchFilter) {
        this.userId = userId
        this.email = email
        this.password = password
        this.name = name
        this.age = age
        this.gender = gender
        this.notification = notification
        this.description = description
        this.lastLocationLng = lastLocationLng
        this.lastLocationLat = lastLocationLat
        this.passwordQuestionId = passwordQuestionId
        this.passwordAnswer = passwordAnswer
        this.searchFilter = searchFilter

        updateJSONObject()
    }

    fun getInsertSql() = "INSERT INTO pickpick.user (" +
            "user_id, email, password, name, age, gender, description, last_location_lng, " +
            "last_location_lat, password_question, password_answer" +
            ") VALUES (" +
            "'$userId', '$email', '$password', '$name', '$age', $gender, '$description', $lastLocationLng, " +
            "$lastLocationLng, $passwordQuestionId, '$passwordAnswer')"

    fun getUpdateSql() = "UPDATE pickpick.user SET " +
            "password='$password', " +
            "name='$name', " +
            "description='$description', " +
            "password_question=$passwordQuestionId, " +
            "password_answer='$passwordAnswer' " +
            "WHERE user_id='$userId'"

    override fun getUUID() = userId

    override fun compareTo(other: UserContent) = userId.compareTo(other.userId)

    override fun getCloned() = UserContent(
        userId = userId,
        email = email,
        password = password,
        name = name,
        age = age,
        gender = gender,
        notification = notification,
        description = description,
        lastLocationLng = lastLocationLng,
        lastLocationLat = lastLocationLat,
        passwordQuestionId = passwordQuestionId,
        passwordAnswer = passwordAnswer,
        searchFilter = searchFilter
    )
}