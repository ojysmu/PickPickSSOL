package mbtinder.android.io.socket

import mbtinder.android.io.component.ServerResult
import mbtinder.lib.component.*
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.component.user.SearchFilter
import mbtinder.lib.component.user.SignUpQuestionContent
import mbtinder.lib.component.user.UserContent
import mbtinder.lib.io.component.CommandContent
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

    fun updateUserDescription(userId: UUID, description: String): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("description", description)

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.UPDATE_USER_DESCRIPTION, arguments)
        )
    }

    fun updateSearchFilter(searchFilter: SearchFilter) {
        SocketClient.getInstance().addCommand(CommandContent(
            UUID.randomUUID(),
            Command.UPDATE_SEARCH_FILTER.name,
            JSONObject().apply { put("search_filter", searchFilter.toJSONObject()) }
        ))
    }

    fun updateUserNotification(userId: UUID, isEnabled: Boolean): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("is_enabled", isEnabled)

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.UPDATE_USER_NOTIFICATION, arguments)
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

    fun setCoordinator(userId: UUID, coordinator: Coordinator): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("coordinator", coordinator.toJSONObject())

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.SET_COORDINATOR, arguments)
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

    fun getSignUpQuestion() =
        SocketUtil.getJSONListResult<SignUpQuestionContent>(
            SocketUtil.getServerResult(
                Command.GET_SIGN_UP_QUESTIONS,
                JSONObject()
            ), "questions"
        )

    fun setSignUpQuestions(userId: UUID, signUpQuestions: JSONList<SignUpQuestionContent>): ServerResult<Void> {
        val forms: JSONList<SignUpQuestionContent.ConnectionForm> = signUpQuestions.mapTo(JSONList()) {
            it.toConnectionForm()
        }

        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("sign_up_questions", forms.toJSONArray())

        val result =
            SocketUtil.getServerResult(Command.SET_SIGN_UP_QUESTIONS, arguments)
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
        val result =
            SocketUtil.getServerResult(Command.GET_MATCHABLE_USERS, arguments)

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

    fun createChat(userId: UUID, opponentId: UUID): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.put("sender_id", userId.toString())
        arguments.put("receiver_id", opponentId.toString())

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.CREATE_CHAT, arguments)
        )
    }

    fun getMessages(userId: UUID, chatId: UUID): ServerResult<JSONList<MessageContent>> {
        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("chat_id", chatId.toString())

        return SocketUtil.getJSONListResult(
            SocketUtil.getServerResult(Command.GET_MESSAGES, arguments),
            "messages"
        )
    }

    fun refreshMessage(userId: UUID, chatId: UUID, lastTimestamp: Long): ServerResult<JSONList<MessageContent>> {
        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("chat_id", chatId.toString())
        arguments.put("last_timestamp", lastTimestamp)

        return SocketUtil.getJSONListResult(
            SocketUtil.getServerResult(Command.REFRESH_MESSAGES, arguments),
            "messages"
        )
    }

    fun getLastMessages(userId: UUID): ServerResult<JSONList<MessageContent>> {
        return SocketUtil.getJSONListResult(
            SocketUtil.getServerResult(
                Command.GET_LAST_MESSAGES,
                JSONObject().apply { put("user_id", userId.toString()) }),
            "messages"
        )
    }
}