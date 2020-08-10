package mbtinder.android.io

import mbtinder.android.io.component.ServerResult
import mbtinder.lib.component.CardStackContent
import mbtinder.lib.component.SignUpQuestionContent
import mbtinder.lib.component.UserContent
import mbtinder.lib.io.constant.Command
import mbtinder.lib.util.JSONList
import org.json.JSONObject
import java.util.*

object CommandProcess {
    fun checkEmailDuplicated(email: String): ServerResult<Void> {
        val arguments = JSONObject().apply { put("email", email) }

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.CHECK_EMAIL_DUPLICATED, arguments)
        )
    }

    /**
     * @see Command.ADD_USER
     */
    fun signUp(email: String, password: String, name: String, age: Int, gender: Int, passwordQuestionId: Int, passwordAnswer: String): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.put("email", email)
        arguments.put("password", password)
        arguments.put("name", name)
        arguments.put("age", age)
        arguments.put("gender", gender)
        arguments.put("password_question_id", passwordQuestionId)
        arguments.put("password_answer", passwordAnswer)

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.ADD_USER, arguments)
        )
    }

    fun signIn(email: String, password: String): ServerResult<UserContent> {
        val arguments = JSONObject()
        arguments.put("email", email)
        arguments.put("password", password)

        return SocketUtil.getSingleResult(
            SocketUtil.getServerResult(Command.SIGN_IN, arguments),
            "user"
        )
    }

    fun findPassword(email: String, passwordQuestionId: Int, passwordAnswer: String): ServerResult<UUID> {
        val arguments = JSONObject()
        arguments.put("email", email)
        arguments.put("password_question_id", passwordQuestionId)
        arguments.put("password_answer", passwordAnswer)

        val result = SocketUtil.getServerResult(Command.FIND_PASSWORD, arguments)
        return if (result.getBoolean("result")) {
            ServerResult(true, 0, UUID.fromString(result.getString("user_id")))
        } else {
            ServerResult(false, result.getInt("code"))
        }
    }

    fun updatePassword(userId: UUID, password: String): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("password", password)

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.UPDATE_PASSWORD, arguments)
        )
    }

    fun getSignUpQuestion() = SocketUtil.getJSONListResult<SignUpQuestionContent>(
        SocketUtil.getServerResult(Command.GET_SIGN_UP_QUESTIONS, JSONObject()), "questions"
    )

    fun setSignUpQuestions(userId: UUID, signUpQuestions: JSONList<SignUpQuestionContent>): ServerResult<Void> {
        val forms: JSONList<SignUpQuestionContent.ConnectionForm> = signUpQuestions.mapTo(JSONList()) {
            it.toConnectionForm()
        }

        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("sign_up_questions", forms.toJSONArray())

        val result = SocketUtil.getServerResult(Command.SET_SIGN_UP_QUESTIONS, arguments)
        return SocketUtil.getVoidResult(result)
    }

    fun setMBTI(userId: UUID, mbti: String): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("mbti", mbti)

        val result = SocketUtil.getServerResult(Command.SET_MBTI, arguments)
        return SocketUtil.getVoidResult(result)
    }

    fun getMatchableUsers(userId: UUID): ServerResult<JSONList<CardStackContent>> {
        val arguments = JSONObject().apply { put("user_id", userId.toString()) }
        val result = SocketUtil.getServerResult(Command.GET_MATCHABLE_USERS, arguments)

        return SocketUtil.getJSONListResult(result, "users")
    }

    fun pick(userId: UUID, opponentId: UUID, isPick: Boolean): ServerResult<Boolean> {
        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("opponent_id", opponentId.toString())
        arguments.put("is_pick", isPick)

        val result = SocketUtil.getServerResult(Command.PICK, arguments)
        return if (result.getBoolean("result")) {
            ServerResult(true, 0, result.getBoolean("is_picked"))
        } else {
            ServerResult(false, result.getInt("code"))
        }
    }


}