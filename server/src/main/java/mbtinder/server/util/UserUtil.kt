package mbtinder.server.util

import mbtinder.lib.component.card_stack.CardStackContent
import mbtinder.lib.component.card_stack.DailyQuestionContent
import mbtinder.lib.component.database.Row
import mbtinder.lib.component.user.SearchFilter
import mbtinder.lib.component.user.SignUpQuestionContent
import mbtinder.lib.component.user.UserContent
import mbtinder.lib.constant.MBTI
import mbtinder.server.io.database.MySQLServer
import java.util.*

object UserUtil {
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
        searchFilter = SearchFilter(row)
    )

    fun getUser(userId: UUID, needPassword: Boolean = false): UserContent? {
        val sql = "SELECT * FROM pickpick.user WHERE user_id='$userId'"
        val queryId = MySQLServer.getInstance().addQuery(sql)
        val queryResult = MySQLServer.getInstance().getResult(queryId)
        if (queryResult.content.isEmpty()) {
            return null
        }

        val user = buildUser(queryResult.content[0])
        if (!needPassword) {
            user.password = ""
            user.passwordAnswer = ""
        }

        return user
    }

    fun getUserByEmail(email: String, needPassword: Boolean = false): UserContent? {
        val sql = "SELECT * FROM pickpick.user WHERE email='$email'"
        val queryId = MySQLServer.getInstance().addQuery(sql)
        val queryResult = MySQLServer.getInstance().getResult(queryId)
        if (queryResult.content.isEmpty()) {
            return null
        }

        val user = buildUser(queryResult.content[0])
        if (!needPassword) {
            user.password = ""
            user.passwordAnswer = ""
        }

        return user
    }

    fun getMatchingScore(
        userMBTI: MBTI,
        userSignUpQuestions: List<SignUpQuestionContent.ConnectionForm>,
        userDailyQuestions: List<DailyQuestionContent.SaveForm>,
        cardStackContent: CardStackContent
    ): Int {
        var sum = 0
        val mbtiMatching = userMBTI.getState(cardStackContent.mbti)
        sum += mbtiMatching * 10

        var signUpQuestionCount = 0
        userSignUpQuestions.forEach { userForm ->
            cardStackContent.contents.find { opponentForm ->
                opponentForm.questionId == userForm.questionId && opponentForm.selected == userForm.selected
            }?.let { signUpQuestionCount++ }
        }
        sum += signUpQuestionCount * 4

        val opponentDailyQuestions = CardStackUtil.findDailyQuestion(cardStackContent.userId)
        var dailyQuestionCount = 0
        userDailyQuestions.forEach { userForm ->
            opponentDailyQuestions.find { opponentForm ->
                opponentForm.questionId == userForm.questionId && opponentForm.isPicked == userForm.isPicked
            }?.let { dailyQuestionCount++ }
        }
        sum += dailyQuestionCount

        return sum
    }
}

fun UserContent.hidePassword() = getCloned().apply {
    password = ""
    passwordAnswer = ""
}