package mbtinder.server.io.socket

import mbtinder.lib.component.*
import mbtinder.lib.constant.MBTI
import mbtinder.lib.constant.ServerPath
import mbtinder.lib.constant.ServerResponse
import mbtinder.lib.io.component.CommandContent
import mbtinder.lib.io.constant.Command
import mbtinder.lib.util.*
import mbtinder.server.constant.LocalFile
import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.database.SQLiteConnection
import mbtinder.server.util.MessageUtil
import mbtinder.server.util.SignUpQuestionUtil
import mbtinder.server.util.UserUtil
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

object CommandProcess {
    fun onReceived(command: CommandContent): JSONObject {
        return when (Command.findCommand(command.name)) {
            Command.CLOSE -> JSONObject()

            Command.CHECK_EMAIL_DUPLICATED -> checkEmailDuplicated(command)
            Command.ADD_USER -> addUser(command)
            Command.GET_USER -> getUser(command)
            Command.UPDATE_USER -> updateUser(command)
            Command.DELETE_USER -> deleteUser(command)
            Command.GET_USER_IMAGES -> getUserImages(command)
            Command.DELETE_USER_IMAGE -> deleteUserImage(command)
            Command.SIGN_IN -> signIn(command)
            Command.FIND_PASSWORD -> findPassword(command)
            Command.UPDATE_PASSWORD -> updatePassword(command)
            Command.GET_SIGN_UP_QUESTIONS -> getSignUpQuestion(command)
            Command.SET_SIGN_UP_QUESTIONS -> setSignUpQuestion(command)
            Command.SET_MBTI -> setMBTI(command)
            Command.GET_MATCHABLE_USERS -> getMatchableUsers(command)
            Command.PICK -> TODO()
            Command.NOPE -> TODO()

            Command.CREATE_CHAT -> createChat(command)
            Command.DELETE_CHAT -> deleteChat(command)
            Command.GET_MESSAGES -> getMessages(command)
            Command.SEND_MESSAGE_TO_SERVER -> sendMessageToServer(command)
        }
    }

    private fun checkEmailDuplicated(command: CommandContent): JSONObject {
        val email = command.arguments.getString("email")
        UserUtil.getUserByEmail(email)?.let {
            return Connection.makeNegativeResponse(command.uuid, ServerResponse.EMAIL_DUPLICATED)
        } ?: let {
            return Connection.makePositiveResponse(command.uuid)
        }
    }

    private fun addUser(command: CommandContent): JSONObject {
        val userId = UUID.randomUUID()
        val email = command.arguments.getString("email")
        // TODO: Encrypt
        val password = command.arguments.getString("password")
        val name = command.arguments.getString("name")
        val age = command.arguments.getInt("age")
        val gender = command.arguments.getInt("gender")
        val passwordQuestionId = command.arguments.getInt("password_question_id")
        val passwordAnswer = command.arguments.getString("password_answer")
        val user = UserContent(
            userId = userId,
            email = email,
            password = password,
            name = name,
            age = age,
            gender = gender,
            description = "",
            lastLocationLng = -1.0,
            lastLocationLat = -1.0,
            passwordQuestionId = passwordQuestionId,
            passwordAnswer = passwordAnswer
        )

        MySQLServer.getInstance().addQuery(user.getInsertSql())

        val userRoot = "${LocalFile.userRoot}/$userId"
        val userRootDirectory = File(userRoot)
        userRootDirectory.mkdir()

        JSONArray().saveJSONArray(LocalFile.getUserInterestPath(userId))

        val sqLiteConnection = SQLiteConnection.getConnection(userId)
        run {
            val createTableSql = "CREATE TABLE chat (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "receiver_id CHAR(36) NOT NULL)"
            sqLiteConnection.addQuery(createTableSql)
        }
        run {
            val createTableSql = "CREATE TABLE pick (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "opponent_id CHAR(36) NOT NULL, " +
                    "is_picked BOOLEAN NOT NULL)"
            sqLiteConnection.addQuery(createTableSql)
        }

        return Connection.makePositiveResponse(command.uuid)
    }

    private fun getUser(command: CommandContent): JSONObject {
        val userId = UUID.fromString(command.arguments.getString("user_id"))
        return UserUtil.getUser(userId)?.let {
            Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("user", it.toJSONObject()) })
        } ?: let {
            Connection.makeNegativeResponse(command.uuid, ServerResponse.USER_ID_NOT_FOUND)
        }
    }

    private fun updateUser(command: CommandContent): JSONObject {
        val user = UserContent(command.arguments.getJSONObject("user"))
        MySQLServer.getInstance().addQuery(user.getUpdateSql())

        return Connection.makePositiveResponse(command.uuid)
    }

    private fun deleteUser(command: CommandContent): JSONObject {
        val userId = UUID.fromString(command.arguments.getString("user_id"))

        val sql = "DELETE FROM mbtinder.user WHERE user_id='$userId'"
        MySQLServer.getInstance().addQuery(sql)

        return Connection.makePositiveResponse(command.uuid)
    }

    private fun getUserImages(command: CommandContent): JSONObject {
        val userId = UUID.fromString(command.arguments.getString("user_id"))

        val images = File(LocalFile.getUserImagePath(userId)).list()?.map {
            val splited = it.split(File.pathSeparator)
            val imageName = splited[splited.size - 1]
            val imageId = UUID.fromString(imageName.split(".")[0])

            UserImageContent(userId, imageId, imageName, ServerPath.getUserImageUrl(userId, imageName))
        }?.toJSONList()

        return if (images != null && images.isNotEmpty()) {
            Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("images", images.toJSONArray()) })
        } else {
            Connection.makeNegativeResponse(command.uuid, ServerResponse.IMAGE_ID_NOT_FOUND)
        }
    }

    private fun deleteUserImage(command: CommandContent): JSONObject {
        val userId = UUID.fromString(command.arguments.getString("user_id"))
        val imageId = UUID.fromString(command.arguments.getString("image_id"))

        File(LocalFile.getUserImagePath(userId)).listFiles()?.forEach {
            val name = it.absolutePath
            val splited = name.split(File.pathSeparator)
            val imageName = splited[splited.size - 1]
            if (imageName.contains(imageId.toString())) {
                it.delete()
                return Connection.makePositiveResponse(command.uuid)
            }
        }

        return Connection.makeNegativeResponse(command.uuid, ServerResponse.IMAGE_ID_NOT_FOUND)
    }

    private fun getSignUpQuestion(command: CommandContent): JSONObject {
        val questions = SignUpQuestionUtil.getQuestions().toJSONList()

        return Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("questions", questions.toJSONArray()) })
    }

    private fun setSignUpQuestion(command: CommandContent): JSONObject {
        val userId = command.arguments.getString("user_id")
        val signUpQuestions = command.arguments.getJSONArray("sign_up_questions")
        signUpQuestions.saveJSONArray(LocalFile.getUserSignUpQuestionPath(userId))

        return Connection.makePositiveResponse(command.uuid)
    }

    private fun setMBTI(command: CommandContent): JSONObject {
        val userId = command.arguments.getString("user_id")
        val mbti = command.arguments.getString("mbti")
        JSONObject().apply { put("value", mbti) }.saveJSONObject(LocalFile.getUserMBTIPath(userId))

        return Connection.makePositiveResponse(command.uuid)
    }

    private fun signIn(command: CommandContent): JSONObject {
        val email = command.arguments.getString("email")
        // TODO: Encrypt
        val password = command.arguments.getString("password")

        val sql = "SELECT `user_id` FROM `mbtinder`.`user` WHERE `email`='$email' AND `password`='$password'"
        val queryId = MySQLServer.getInstance().addQuery(sql)
        val queryResult = MySQLServer.getInstance().getResult(queryId)

        return if (queryResult.content.isEmpty()) {
            Connection.makeNegativeResponse(command.uuid, ServerResponse.EMAIL_NOT_FOUND)
        } else {
            val user = UserUtil.getUser(queryResult.content[0].getUUID("user_id"))!!
            Connection.makePositiveResponse(
                command.uuid,
                JSONObject().apply { put("user", user.toJSONObject()) }
            )
        }
    }

    private fun findPassword(command: CommandContent): JSONObject {
        val email = command.arguments.getString("email")
        val passwordQuestionId = command.arguments.getInt("password_question_id")
        // TODO: Encrypt
        val passwordAnswer = command.arguments.getString("password_answer")

        return UserUtil.getUserByEmail(email)?.let {
            if (it.passwordQuestionId == passwordQuestionId && it.passwordAnswer == passwordAnswer) {
                Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("user_id", it.userId.toString()) })
            } else {
                Connection.makeNegativeResponse(command.uuid, ServerResponse.USER_NOT_FOUND)
            }
        } ?: let {
            Connection.makeNegativeResponse(command.uuid, ServerResponse.USER_NOT_FOUND)
        }
    }

    private fun updatePassword(command: CommandContent): JSONObject {
        val userId = command.arguments.getString("user_id")
        // TODO: Encrypt
        val password = command.arguments.getString("password")
        val sql = "UPDATE mbtinder.user SET `password`='$password' WHERE `user_id`='$userId'"
        MySQLServer.getInstance().addQuery(sql)

        return Connection.makePositiveResponse(command.uuid)
    }

    private fun getMatchableUsers(command: CommandContent): JSONObject {
        // 탐색을 시도하는 사용자 ID
        val userId = UUID.fromString(command.arguments.getString("user_id"))
        // 사용자 MBTI
        val userMBTI = MBTI.findByName(
            loadJSONObject(LocalFile.getUserMBTIPath(userId)).getString("value")
        )
        // 가입 시 입력한 취향
        val userSignUpQuestions = loadJSONArray(LocalFile.getUserSignUpQuestionPath(userId))
            .toJSONList<SignUpQuestionContent.ConnectionForm>()

        val sqLiteConnection = SQLiteConnection.getConnection(userId)
        val sql = "SELECT opponent_id FROM pick"
        val queryId = sqLiteConnection.addQuery(sql)
        val queryResult = sqLiteConnection.getResult(queryId)
        // 이미 목록에 나타났던 사용자 목록
        val metList = queryResult.content.map { it.getUUID("opponent_id") }

        val filteredUsers = UserUtil.getUserIds()
            // 본인과 이미 목록이 표시되었던 사용자 제외
            .filter { it != userId && !metList.contains(it) }
            // 매칭 점수가 70점 이하인 사용자 제외
            .filter { UserUtil.getMatchingScore(userId, userMBTI, it, userSignUpQuestions) > 70 }
            // 실제 사용자 정보 반환
            .map { UserUtil.getUser(it)!! }
            .toJSONList()
            .toJSONArray()

        // STOPSHIP: 2020/08/06 NEED TEST

        return Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("users", filteredUsers) })
    }

    private fun createChat(command: CommandContent): JSONObject {
        val chatId = UUID.randomUUID()
        val senderId = UUID.fromString(command.arguments.getString("sender_id"))
        val receiverId = UUID.fromString(command.arguments.getString("receiver_id"))

        val senderConnection = SQLiteConnection.getConnection(senderId)
        val receiverConnection = SQLiteConnection.getConnection(receiverId)

        senderConnection.addQuery(SQLiteConnection.getCreateChatSql(chatId))
        senderConnection.addQuery(SQLiteConnection.getInsertNewChatSql(chatId, receiverId))

        receiverConnection.addQuery(SQLiteConnection.getCreateChatSql(chatId))
        receiverConnection.addQuery(SQLiteConnection.getInsertNewChatSql(chatId, senderId))

        MySQLServer.getInstance().addQuery("INSERT INTO mbtinder.chat (" +
                "chat_id, participant1, participant2" +
                ") VALUES (" +
                "'$chatId', '$senderId', '$receiverId')")

        return Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("chat_id", chatId.toString()) })
    }

    private fun deleteChat(command: CommandContent): JSONObject {
        val chatContent = ChatContent(command.arguments.getJSONObject("chat_content"))
        val dropSql = "DROP TABLE ${chatContent.chatId}"
        val deleteSql = "DELETE FROM mbtinder.chat WHERE chat_id='${chatContent.chatId}'"

        SQLiteConnection.getConnection(chatContent.participant1).addQuery(dropSql)
        SQLiteConnection.getConnection(chatContent.participant2).addQuery(dropSql)
        MySQLServer.getInstance().addQuery(deleteSql)

        return Connection.makePositiveResponse(command.uuid)
    }

    private fun getMessages(command: CommandContent): JSONObject {
        val chatId = UUID.fromString(command.arguments.getString("chat_id"))
        val userId = UUID.fromString(command.arguments.getString("user_id"))
        val endIndex = command.arguments.getInt("end_index")

        val sql = SQLiteConnection.getSelectMessageSql(chatId, endIndex)
        val queryResult = SQLiteConnection.getConnection(userId).let { it.getResult(it.addQuery(sql)) }
        val messageList: JSONList<MessageContent> = queryResult.content.mapTo(JSONList()) {
            MessageUtil.buildMessage(it, chatId)
        }
        val arguments = JSONObject()
        arguments.put("messages", messageList.toJSONArray())

        return Connection.makePositiveResponse(command.uuid, arguments)
    }

    private fun sendMessageToServer(command: CommandContent): JSONObject {
        val timestamp = System.currentTimeMillis()

        val chatId = UUID.fromString(command.arguments.getString("chat_id"))
        val senderId = UUID.fromString(command.arguments.getString("sender_id"))
        val receiverId = UUID.fromString(command.arguments.getString("receiver_id"))
        val body = command.arguments.getString("body")
        val messageContent = MessageContent(
            chatId,
            senderId,
            receiverId,
            timestamp,
            body
        )

        val senderConnection = SQLiteConnection.getConnection(senderId)
        val receiveConnection = SQLiteConnection.getConnection(receiverId)

        senderConnection.addQuery(messageContent.getLocalInsertMessageSql())
        receiveConnection.addQuery(messageContent.getLocalInsertMessageSql())
        MySQLServer.getInstance().addQuery(messageContent.getServerInsertMessageSql())

        return Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("timestamp", timestamp) })
    }
}