package mbtinder.server.util

import mbtinder.lib.component.CardStackContent
import mbtinder.lib.component.SignUpQuestionContent
import mbtinder.lib.component.UserContent
import mbtinder.lib.constant.MBTI
import mbtinder.lib.util.*
import mbtinder.server.constant.LocalFile
import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.database.component.Row
import java.util.*

object UserUtil {
    private const val UPDATE_DURATION = 60 * 1000

    private var users: ImmutableList<UserContent> = updateUsers()
    private var lastUpdate: Long = 0

    private fun updateUsers(): ImmutableList<UserContent> {
        println("UserUtil.updateUsers() START")

        val sql = "SELECT * FROM mbtinder.user"
        val queryId = MySQLServer.getInstance().addQuery(sql)
        val queryResult = MySQLServer.getInstance().getResult(queryId)

        lastUpdate = System.currentTimeMillis()
        users = queryResult.content.map { buildUser(it) }.sorted().toImmutableList()

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

    fun getUserIds() = users.getCloned().map { it.getUUID() }

    fun getAllCardStacks() = users.getCloned().map { userContent: UserContent ->
        CardStackContent(
            userContent,
            MBTI.findByName(loadJSONObject(LocalFile.getUserMBTIPath(userContent.userId)).getString("value")),
            loadJSONList<SignUpQuestionContent.ConnectionForm>(LocalFile.getUserSignUpQuestionPath(userContent.userId))
                .mapTo(JSONList()) { form: SignUpQuestionContent.ConnectionForm ->
                    SignUpQuestionUtil.findByQuestionId(form.questionId)
                }
        )
    }

    fun getMatchingScore(
        userMBTI: MBTI,
        userSignUpQuestions: List<SignUpQuestionContent.ConnectionForm>,
        cardStackContent: CardStackContent
    ): Int {
        var sum = 0
        val mbtiMatching = userMBTI.getState(cardStackContent.mbti)
        sum += mbtiMatching * 10

        var signUpQuestionCount = 0
        userSignUpQuestions.forEach { userForm ->
            cardStackContent.contents.find { opponentForm ->
                opponentForm.questionId == userForm.questionId &&
                        opponentForm.selected == userForm.selected
            }?.let { signUpQuestionCount++ }
        }
        sum += signUpQuestionCount * 4

        return sum
    }
}

fun UserContent.hidePassword() = getCloned().apply {
    password = ""
    passwordAnswer = ""
}

fun UserContent.withPassword(needPassword: Boolean) = getCloned().apply {
    if (!needPassword) {
        password = ""
        passwordAnswer = ""
    }
}