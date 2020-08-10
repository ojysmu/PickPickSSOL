package mbtinder.server.util

import mbtinder.lib.component.SignUpQuestionContent
import mbtinder.lib.component.UserContent
import mbtinder.lib.constant.MBTI
import mbtinder.lib.util.loadJSONArray
import mbtinder.lib.util.loadJSONObject
import mbtinder.lib.util.toJSONList
import mbtinder.server.constant.LocalFile
import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.database.component.Row
import java.io.File
import java.util.*

object UserUtil {
    private const val UPDATE_DURATION = 60 * 1000

    private var users: List<UserContent> = updateUsers()
    private var lastUpdate: Long = 0

    private fun updateUsers(): List<UserContent> {
        println("UserUtil.updateUsers() START")

        val sql = "SELECT * FROM mbtinder.user"
        val queryId = MySQLServer.getInstance().addQuery(sql)
        val queryResult = MySQLServer.getInstance().getResult(queryId)

        lastUpdate = System.currentTimeMillis()
        users = queryResult.content.map { buildUser(it) }.sorted()
        return users
    }

    private fun ensureUpdate() {
        if (lastUpdate < System.currentTimeMillis() - UPDATE_DURATION) {
            users = updateUsers()
        }
    }

    private fun buildUser(row: Row) = UserContent(
        row.getUUID("user_id"), row.getString("email"), row.getString("password"),
        row.getString("name"), row.getInt("age"), row.getInt("gender"),
        row.getString("description"), row.getDouble("last_location_lng"),
        row.getDouble("last_location_lat"), row.getInt("password_question"),
        row.getString("password_answer")
    )

    fun getUser(userId: UUID, needPassword: Boolean = false): UserContent? {
        ensureUpdate()

        return findBinaryTwice(users, this::updateUsers) { it.userId.compareTo(userId) }?.withPassword(needPassword)
    }

    fun getUserByEmail(email: String, needPassword: Boolean = false): UserContent? {
        ensureUpdate()

        return findAllTwice(users, this::updateUsers) { it.email == email }?.withPassword(needPassword)
    }

    fun getUserIds() = users.map { it.userId }

    fun getMatchingScore(userId: UUID, userMBTI: MBTI, opponentId: UUID, userSignUpQuestions: List<SignUpQuestionContent.ConnectionForm>): Int {
        var sum = 0
        val opponentMBTI = MBTI.findByName(loadJSONObject(LocalFile.getUserMBTIPath(opponentId)).getString("value"))
        val mbtiMatching = userMBTI.getState(opponentMBTI)
        sum += mbtiMatching * 10

        val opponentSignUpQuestions = loadJSONArray(LocalFile.getUserSignUpQuestionPath(userId)).toJSONList<SignUpQuestionContent.ConnectionForm>()
        var signUpQuestionCount = 0
        userSignUpQuestions.forEach { userForm ->
            opponentSignUpQuestions.find { opponentForm ->
                opponentForm.questionId == userForm.questionId &&
                        opponentForm.selected == userForm.selected
            }?.let { signUpQuestionCount++ }
        }
        sum += signUpQuestionCount * 4

        return sum
    }
}

fun hasProfileImage(userId: UUID) = File(LocalFile.getUserImagePath(userId)).exists()

fun UserContent.hasProfileImage() = File(LocalFile.getUserImagePath(userId)).exists()

fun UserContent.withPassword(withPassword: Boolean): UserContent {
    if (!withPassword) {
        password = ""
        passwordAnswer = ""
    }
    
    return this
}

fun UserContent.hidePassword(): UserContent {
    password = ""
    passwordAnswer = ""

    return this
}