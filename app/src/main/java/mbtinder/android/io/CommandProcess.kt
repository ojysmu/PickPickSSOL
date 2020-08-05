package mbtinder.android.io

import mbtinder.android.io.component.ServerResult
import mbtinder.lib.component.SignUpQuestionContent
import mbtinder.lib.component.UserContent
import mbtinder.lib.io.constant.Command
import org.json.JSONObject
import java.util.*

object CommandProcess {
    fun checkEmailDuplicated(email: String): ServerResult<Void> {
        val arguments = JSONObject().apply { put("email", email) }

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(
                Command.CHECK_EMAIL_DUPLICATED,
                arguments
            )
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

        return SocketUtil.getVoidResult(SocketUtil.getServerResult(Command.ADD_USER, arguments))
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
            SocketUtil.getServerResult(
                Command.UPDATE_PASSWORD,
                arguments
            )
        )
    }

    fun getSignUpQuestion() = SocketUtil.getJSONListResult<SignUpQuestionContent>(
        SocketUtil.getServerResult(Command.GET_SIGN_UP_QUESTIONS, JSONObject()), "questions"
    )
}