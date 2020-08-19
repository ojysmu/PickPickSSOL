package mbtinder.android.io.socket

import mbtinder.android.io.component.ServerResult
import mbtinder.android.io.http.ImageUploader
import mbtinder.lib.component.*
import mbtinder.lib.component.card_stack.CardStackContent
import mbtinder.lib.component.card_stack.DailyQuestionContent
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.component.user.SearchFilter
import mbtinder.lib.component.user.SignUpQuestionContent
import mbtinder.lib.component.user.UserContent
import mbtinder.lib.io.component.CommandContent
import mbtinder.lib.io.constant.Command
import mbtinder.lib.util.*
import org.json.JSONObject
import java.sql.Date
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
    fun signUp(email: String, password: String, name: String, age: Int, gender: Int, description: String, passwordQuestionId: Int, passwordAnswer: String): ServerResult<UUID> {
        val arguments = JSONObject()
        arguments.put("email", email)
        arguments.put("password", password)
        arguments.put("name", name)
        arguments.put("age", age)
        arguments.put("gender", gender)
        arguments.put("description", description)
        arguments.put("password_question_id", passwordQuestionId)
        arguments.put("password_answer", passwordAnswer)

        val result = SocketUtil.getServerResult(Command.ADD_USER, arguments)
        return if (result.getBoolean("result")) {
            ServerResult(true, 0, result.getUUID("user_id"))
        } else {
            ServerResult(false, result.getInt("code"))
        }
    }

    fun uploadProfileImage(userId: UUID, rawImage: ByteArray): ServerResult<Void> {
        val uploader = ImageUploader(userId, rawImage, true)
        uploader.start()

        val result = uploader.getResult()
        return if (result.getBoolean("result")) {
            ServerResult(true)
        } else {
            ServerResult(false, result.getInt("code"))
        }
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

    fun deleteUser(userId: UUID): ServerResult<Void> {
        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.DELETE_USER, JSONObject().apply { put("user_id", userId) })
        )
    }

    fun blockUser(userId: UUID, opponentId: UUID, chatId: UUID): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.putUUID("user_id", userId)
        arguments.putUUID("opponent_id", opponentId)
        arguments.putUUID("chat_id", chatId)

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.BLOCK_USER, arguments)
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

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.SET_MBTI, arguments)
        )
    }

    fun getMatchableUsers(userId: UUID, coordinator: Coordinator, searchFilter: SearchFilter): ServerResult<JSONList<CardStackContent>> {
        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("coordinator", coordinator.toJSONObject())
        arguments.put("search_filter", searchFilter.toJSONObject())

        return SocketUtil.getJSONListResult(
            SocketUtil.getServerResult(Command.GET_MATCHABLE_USERS, arguments),
            "card_stack_contents"
        )
    }

    fun refreshMatchableUsers(userId: UUID, coordinator: Coordinator, searchFilter: SearchFilter, currentMetList: List<UUID>): ServerResult<JSONList<CardStackContent>> {
        val arguments = JSONObject()
        arguments.putUUID("user_id", userId)
        arguments.put("coordinator", coordinator.toJSONObject())
        arguments.put("search_filter", searchFilter.toJSONObject())
        arguments.put("current_met_list", currentMetList.toJSONArray())

        return SocketUtil.getJSONListResult(
            SocketUtil.getServerResult(Command.REFRESH_MATCHABLE_USERS, arguments),
            "card_stack_contents"
        )
    }

    fun getDailyQuestions(lastDate: Date): ServerResult<JSONList<DailyQuestionContent>> {
        return SocketUtil.getJSONListResult(
            SocketUtil.getServerResult(Command.GET_DAILY_QUESTIONS, JSONObject().apply { putDate("last_date", lastDate) }),
            "daily_questions"
        )
    }

    fun isAnsweredQuestion(userId: UUID, questionId: UUID): ServerResult<Boolean> {
        val arguments = JSONObject()
        arguments.putUUID("user_id", userId)
        arguments.putUUID("question_id", questionId)

        val result = SocketUtil.getServerResult(Command.IS_ANSWERED_QUESTION, arguments)
        return if (result.getBoolean("result")) {
            ServerResult(true, 0, result.getBoolean("answered"))
        } else {
            ServerResult(false, result.getInt("code"))
        }
    }

    fun answerQuestion(userId: UUID, questionId: UUID, isPick: Boolean): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.putUUID("user_id", userId)
        arguments.putUUID("question_id", questionId)
        arguments.put("is_pick", isPick)

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.ANSWER_QUESTION, arguments)
        )
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

    fun sendMessage(chatId: UUID, senderId: UUID, receiverId: UUID, opponentName: String, body: String): ServerResult<Long> {
        val arguments = JSONObject()
        arguments.put("chat_id", chatId.toString())
        arguments.put("sender_id", senderId.toString())
        arguments.put("receiver_id", receiverId.toString())
        arguments.put("opponent_name", opponentName)
        arguments.put("body", body)

        val result = SocketUtil.getServerResult(Command.SEND_MESSAGE, arguments)
        return if (result.getBoolean("result")) {
            ServerResult(true, 0, result.getLong("timestamp"))
        } else {
            ServerResult(false, result.getInt("code"))
        }
    }
}