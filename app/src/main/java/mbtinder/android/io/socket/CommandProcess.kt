package mbtinder.android.io.socket

import mbtinder.android.component.LocalChatContent
import mbtinder.android.component.LocalDailyQuestionContent
import mbtinder.android.component.LocalMessageContent
import mbtinder.android.io.component.ServerResult
import mbtinder.android.io.database.SQLiteConnection
import mbtinder.android.io.http.ImageUploader
import mbtinder.android.ui.fragment.account.AccountFragment
import mbtinder.android.ui.fragment.chat.ChatFragment
import mbtinder.android.ui.fragment.sign_up.SignUp5Fragment
import mbtinder.android.util.CryptoUtil
import mbtinder.lib.component.ChatContent
import mbtinder.lib.component.MessageContent
import mbtinder.lib.component.card_stack.CardStackContent
import mbtinder.lib.component.card_stack.DailyQuestionContent
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.component.user.SearchFilter
import mbtinder.lib.component.user.SignUpQuestionContent
import mbtinder.lib.component.user.UserContent
import mbtinder.lib.constant.PasswordQuestion
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
     * 회원가입
     * @param email: 이메일
     * @param password: 암호화되지 않은 비밀번호
     * @param name: 이름
     * @param age: 나이
     * @param gender: 성별
     * @param description: 자기소개
     * @param passwordQuestionId: 비밀번호 질문 ID [PasswordQuestion] 확인
     * @param passwordAnswer: 비밀번호 답변
     * @return 가입된 사용자의 ID
     * @see SignUp5Fragment.signUp
     */
    fun signUp(email: String, password: String, name: String, age: Int, gender: Int, description: String, passwordQuestionId: Int, passwordAnswer: String): ServerResult<UUID> {
        val arguments = JSONObject()
        arguments.put("email", email)
        arguments.put("password", CryptoUtil.encrypt(password))
        arguments.put("name", name)
        arguments.put("age", age)
        arguments.put("gender", gender)
        arguments.put("description", description)
        arguments.put("password_question_id", passwordQuestionId)
        arguments.put("password_answer", CryptoUtil.encrypt(passwordAnswer))

        val result = SocketUtil.getServerResult(Command.ADD_USER, arguments)
        return if (result.getBoolean("result")) {
            ServerResult(true, 0, result.getUUID("user_id"))
        } else {
            ServerResult(false, result.getInt("code"))
        }
    }

    /**
     * 사용자 프로필 이미지 등록
     * @param userId: 사용자 ID
     * @param rawImage: 이미지 바이트 배열
     * @return 성공 여부
     * @see SignUp5Fragment.signUp
     * @see AccountFragment.onActivityResult
     */
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

    /**
     * 사용자 자기소개 갱신
     * @param userId: 사용자 ID
     * @param description: 변경할 자기소개
     * @return 성공 여부
     * @see AccountFragment.onDescriptionEditClicked
     */
    fun updateUserDescription(userId: UUID, description: String): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("description", description)

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.UPDATE_USER_DESCRIPTION, arguments)
        )
    }

    /**
     * 매칭 검색 필터 갱신
     * @param searchFilter: 변경할 검색 필터
     * @return 반환하지 않는 명령
     * @see AccountFragment.updateSearchFilter
     */
    fun updateSearchFilter(searchFilter: SearchFilter) {
        SocketClient.getInstance().addCommand(CommandContent(
            UUID.randomUUID(),
            Command.UPDATE_SEARCH_FILTER.name,
            JSONObject().apply { put("search_filter", searchFilter.toJSONObject()) }
        ))
    }

    /**
     * 알림 수신 여부 갱신
     * @param userId: 사용자 ID
     * @param isEnabled: 일림 수신 여부
     * @return 성공 여부
     * @see AccountFragment.onNotificationChanged
     */
    fun updateUserNotification(userId: UUID, isEnabled: Boolean): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.put("user_id", userId.toString())
        arguments.put("is_enabled", isEnabled)

        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.UPDATE_USER_NOTIFICATION, arguments)
        )
    }

    /**
     * 회원 탈퇴
     * @param userId: 탈퇴할 사용자 ID
     * @return 성공 여부
     * @see AccountFragment.onDeleteClicked
     */
    fun deleteUser(userId: UUID): ServerResult<Void> {
        return SocketUtil.getVoidResult(
            SocketUtil.getServerResult(Command.DELETE_USER, JSONObject().apply { put("user_id", userId) })
        )
    }

    /**
     * 사용자 차단
     * @param userId: 사용자 ID
     * @param opponentId: 차단할 상대방 ID
     * @param chatId: 상대방과 포함된 채팅 ID
     * @return 성공여부
     * @see ChatFragment.onBlockClicked
     */
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
        arguments.put("password", CryptoUtil.encrypt(password))

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
        arguments.put("password_answer", CryptoUtil.encrypt(passwordAnswer))

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
        arguments.put("password", CryptoUtil.encrypt(password))

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

    fun isAnsweredQuestion(questionId: UUID): Boolean {
        val connection = SQLiteConnection.getInstance()
        val sql = "SELECT _id FROM daily_questions WHERE question_id='$questionId'"
        val rowId = connection.getRowId(sql)

        return rowId != -1
    }

    fun answerQuestion(userId: UUID, questionId: UUID, isPick: Boolean): ServerResult<Void> {
        val connection = SQLiteConnection.getInstance()

        val (length, id) = getAnsweredHead()
        if (length == 28) {
            connection.executeUpdate("DELETE FROM daily_questions WHERE _id=$id")
        }

        val insertSql = "INSERT INTO daily_questions (question_id, is_picked) VALUES ('$questionId', ${if (isPick) 1 else 0})"
        connection.executeUpdate(insertSql)

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

    fun createChat(userId: UUID, userName: String, opponentId: UUID, opponentName: String): ServerResult<Void> {
        val arguments = JSONObject()
        arguments.put("sender_id", userId.toString())
        arguments.put("sender_name", userName)
        arguments.put("receiver_id", opponentId.toString())
        arguments.put("receiver_name", opponentName)

        val result = SocketUtil.getServerResult(Command.CREATE_CHAT, arguments)
        return if (result.getBoolean("result")) {
            val chatContent = ChatContent(result.getUUID("chat_id"), opponentId, opponentName)
            val messageContent = MessageContent(result.getJSONObject("message_content"))
            val connection = SQLiteConnection.getInstance()
            connection.executeUpdate(chatContent.getCreateSql())
            connection.executeUpdate(chatContent.getInsertSql())
            connection.executeUpdate(messageContent.getLocalInsertMessageSql())

            ServerResult(true)
        } else {
            ServerResult(false, result.getInt("code"))
        }
    }

    fun getMessages(chatId: UUID, opponentName: String): IDList<MessageContent> {
        val sql = "SELECT * FROM '$chatId'"
        val queryResult = SQLiteConnection.getInstance().executeQuery<LocalMessageContent>(sql)

        return queryResult.content
            .map { it.toMessageContent().apply {
                this.chatId = chatId
                this.opponentName = opponentName
            }}
            .sorted()
            .toIDList()
            .apply { uuid = chatId }
    }

    fun getLastMessages(): IDList<MessageContent> {
        val connection = SQLiteConnection.getInstance()
        val chatIds = getChatContents()
        return chatIds.map {
            val sql = "SELECT * FROM '${it.chatId}' where _id = (SELECT MAX(_id) FROM '${it.chatId}')"
            val queryResult = connection.executeQuery<LocalMessageContent>(sql)

            queryResult.content[0].toMessageContent().apply { this.chatId = it.chatId; this.opponentName = it.opponentName }
        }.sorted().reversed().toIDList()
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

    private fun getChatContents(): List<ChatContent> {
        val connection = SQLiteConnection.getInstance()
        val sql = "SELECT * FROM chat"
        val queryResult = connection.executeQuery<LocalChatContent>(sql)

        return queryResult.content.map { it.toChatContent() }
    }

    private fun getAnsweredHead(): Pair<Int, Int> {
        val connection = SQLiteConnection.getInstance()
        val ids = connection.getInts("daily_questions", "_id")
        val firstId = if (ids.isNotEmpty()) ids[0] else -1

        return Pair(ids.size, firstId)
    }
}