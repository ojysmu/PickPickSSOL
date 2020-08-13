package mbtinder.server.util

import mbtinder.lib.component.CardStackContent
import mbtinder.lib.component.user.SignUpQuestionContent
import mbtinder.lib.component.user.UserContent
import mbtinder.lib.constant.MBTI
import mbtinder.lib.util.*
import mbtinder.server.constant.LocalFile
import mbtinder.server.io.database.MySQLServer
import mbtinder.lib.component.database.Row
import mbtinder.lib.component.user.SearchFilter
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
        userId = row.getUUID("user_id"),
        email = row.getString("email"),
        password = row.getString("password"),
        name = row.getString("name"),
        age = row.getInt("age"),
        gender = row.getInt("gender"),
        notification = row.getBoolean("notification"),
        description = row.getString("description"),
        lastLocationLng = row.getDouble("last_location_lng"),
        lastLocationLat = row.getDouble("last_location_lat"),
        passwordQuestionId = row.getInt("password_question"),
        passwordAnswer = row.getString("password_answer"),
        searchFilter = SearchFilter(row.getJSONObject("search_filter"))
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

    fun getAllUsers() = users.getCloned()

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