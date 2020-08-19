package mbtinder.server.io.socket

import mbtinder.lib.component.ChatContent
import mbtinder.lib.component.MessageContent
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.component.user.SearchFilter
import mbtinder.lib.component.user.UserContent
import mbtinder.lib.constant.Notification
import mbtinder.lib.constant.NotificationForm
import mbtinder.lib.constant.ServerResponse
import mbtinder.lib.io.component.CommandContent
import mbtinder.lib.io.constant.Command
import mbtinder.lib.util.*
import mbtinder.server.constant.LocalFile
import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.database.SQLiteConnection
import mbtinder.server.io.notification.NotificationServer
import mbtinder.server.util.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.*

/**
 * 서버 명령 처리 object
 */
object CommandProcess {
    fun onReceived(command: CommandContent, connection: Connection): JSONObject {
        return when (Command.findCommand(command.name)) {
            Command.CLOSE -> JSONObject()

            Command.CHECK_EMAIL_DUPLICATED -> checkEmailDuplicated(command)
            Command.ADD_USER -> addUser(command)
            Command.GET_USER -> getUser(command)
            Command.UPDATE_USER -> updateUser(command)
            Command.UPDATE_USER_DESCRIPTION -> updateUserDescription(command)
            Command.UPDATE_SEARCH_FILTER -> updateSearchFilter(command)
            Command.UPDATE_USER_NOTIFICATION -> updateUserNotification(command)
            Command.DELETE_USER -> deleteUser(command)
            Command.BLOCK_USER -> blockUser(command)
            Command.GET_USER_IMAGES -> TODO() // getUserImages(command)
            Command.DELETE_USER_IMAGE -> deleteUserImage(command)
            Command.SIGN_IN -> signIn(command, connection)
            Command.SET_COORDINATOR -> setCoordinator(command)
            Command.FIND_PASSWORD -> findPassword(command)
            Command.UPDATE_PASSWORD -> updatePassword(command)
            Command.GET_SIGN_UP_QUESTIONS -> getSignUpQuestion(command)
            Command.SET_SIGN_UP_QUESTIONS -> setSignUpQuestion(command)
            Command.SET_MBTI -> setMBTI(command)
            Command.GET_MATCHABLE_USERS -> getMatchableUsers(command)
            Command.REFRESH_MATCHABLE_USERS -> refreshMatchableUsers(command)
            Command.GET_DAILY_QUESTIONS -> getDailyQuestions(command)
            Command.IS_ANSWERED_QUESTION -> isAnsweredQuestion(command)
            Command.ANSWER_QUESTION -> answerQuestion(command)
            Command.PICK -> pick(command)

            Command.CREATE_CHAT -> createChat(command)
            Command.DELETE_CHAT -> deleteChat(command)
            Command.GET_MESSAGES -> getMessages(command)
            Command.REFRESH_MESSAGES -> refreshMessages(command)
            Command.GET_LAST_MESSAGES -> getLastMessages(command)
            Command.SEND_MESSAGE -> sendMessage(command)
        }
    }

    /**
     * 이메일 중복 체크
     * @param command: 인수는 다음이 포함되어야 함: 중복을 확인할 이메일
     */
    private fun checkEmailDuplicated(command: CommandContent): JSONObject {
        val email = command.arguments.getString("email")
        UserUtil.getUserByEmail(email)?.let {
            return Connection.makeNegativeResponse(command.uuid, ServerResponse.EMAIL_DUPLICATED)
        } ?: let {
            return Connection.makePositiveResponse(command.uuid)
        }
    }

    /**
     * 사용자 추가 (회원가입)
     * @param command: 인수는 다음이 포함되어야 함: 중복되지 않은 이메일, 비밀번호, 이름, 나이, 성별,
     *                                       비밀번호 찾기 질문 ID, 비밀번호 찾기 답변
     */
    private fun addUser(command: CommandContent): JSONObject {
        val userId = UUID.randomUUID()
        val email = command.arguments.getString("email")
        // TODO: Encrypt
        val password = command.arguments.getString("password")
        val name = command.arguments.getString("name")
        val age = command.arguments.getInt("age")
        val gender = command.arguments.getInt("gender")
        val description = command.arguments.getString("description")
        val passwordQuestionId = command.arguments.getInt("password_question_id")
        val passwordAnswer = command.arguments.getString("password_answer")
        val user = UserContent(
            userId = userId,
            email = email,
            password = password,
            name = name,
            age = age,
            gender = gender,
            notification = true,
            description = description,
            lastLocationLng = -1.0,
            lastLocationLat = -1.0,
            passwordQuestionId = passwordQuestionId,
            passwordAnswer = passwordAnswer,
            searchFilter = SearchFilter(userId)
        )

        MySQLServer.getInstance().addQuery(user.getInsertSql())

        // raw/user 경로 생성
        val userRoot = "${LocalFile.userRoot}/$userId"
        val userRootDirectory = File(userRoot)
        userRootDirectory.mkdir()
        Runtime.getRuntime().exec("sudo chmod -R 0777 $userRoot")

        JSONArray().saveJSONArray(LocalFile.getUserInterestPath(userId))

        val sqLiteConnection = SQLiteConnection.getConnection(userId)
        run {
            // 채팅 목록 테이블 생성
            val createTableSql = "CREATE TABLE chat (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "chat_id CHAR(36) NOT NULL, " +
                    "receiver_id CHAR(36) NOT NULL)"
            sqLiteConnection.addQuery(createTableSql)
        }
        run {
            // pick, nope 정보가 저장될 테이블 생성
            val createTableSql = "CREATE TABLE pick (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "opponent_id CHAR(36) NOT NULL, " +
                    "is_picked BOOLEAN NOT NULL)"
            sqLiteConnection.addQuery(createTableSql)
        }
        run {
            // pick, nope한 상대방의 정보가 저장될 테이블 생성
            val createTableSql = "CREATE TABLE picked (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "opponent_id CHAR(36) NOT NULL)"
            sqLiteConnection.addQuery(createTableSql)
        }
        run {
            val createTableSql = "CREATE TABLE block (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "opponent_id CHAR(36) NOT NULL)"
            sqLiteConnection.addQuery(createTableSql)
        }
        run {
            val createTableSql = "CREATE TABLE daily_questions (" +
                    "_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "question_id CHAR(36) NOT NULL, " +
                    "is_picked BOOLEAN NOT NULL)"
            sqLiteConnection.addQuery(createTableSql)
        }

        return Connection.makePositiveResponse(command.uuid, JSONObject().apply { putUUID("user_id", userId) })
    }

    /**
     * 사용자 탐색
     * @param command: 인수는 다음이 포함되어야 함: 사용자 ID
     * @return 탐색된 사용자. 탐색되지 않았을 때 [ServerResponse.USER_ID_NOT_FOUND]
     */
    private fun getUser(command: CommandContent): JSONObject {
        val userId = UUID.fromString(command.arguments.getString("user_id"))
        return UserUtil.getUser(userId)?.let {
            Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("user", it.toJSONObject()) })
        } ?: let {
            Connection.makeNegativeResponse(command.uuid, ServerResponse.USER_ID_NOT_FOUND)
        }
    }

    /**
     * 사용자 정보 갱신
     * @param command: 인수는 다음이 포함되어야 함: 사용자 정보 전체
     * @see UserContent
     */
    private fun updateUser(command: CommandContent): JSONObject {
        val user = UserContent(command.arguments.getJSONObject("user"))
        MySQLServer.getInstance().addQuery(user.getUpdateSql())

        return Connection.makePositiveResponse(command.uuid)
    }

    private fun updateUserDescription(command: CommandContent): JSONObject {
        val userId = command.arguments.getString("user_id")
        val description = command.arguments.getString("description")
        val sql = "UPDATE pickpick.user SET description='$description' WHERE user_id='$userId'"
        MySQLServer.getInstance().addQuery(sql)

        return Connection.makePositiveResponse(command.uuid)
    }

    private fun updateSearchFilter(command: CommandContent): JSONObject {
        val searchFilter = SearchFilter(command.arguments.getJSONObject("search_filter"))
        println("updateSearchFilter(): searchFilter=${searchFilter.userId}, gender=${searchFilter.gender}, age_start=${searchFilter.ageStart}, age_end=${searchFilter.ageEnd}, distance=${searchFilter.distance}")
        MySQLServer.getInstance().addQuery(searchFilter.getUpdateSql())

        return JSONObject()
    }

    private fun updateUserNotification(command: CommandContent): JSONObject {
        val userId = command.arguments.getString("user_id")
        val isEnabled = command.arguments.getBoolean("is_enabled")
        val sql = "UPDATE pickpick.user SET notification=$isEnabled WHERE user_id='$userId'"
        MySQLServer.getInstance().addQuery(sql)

        return Connection.makePositiveResponse(command.uuid)
    }

    /**
     * 사용자 삭제 (회원탈퇴)
     * @param command: 인수는 다음이 포함되어야 함: 삭제할 사용자 ID
     */
    private fun deleteUser(command: CommandContent): JSONObject {
        val userId = UUID.fromString(command.arguments.getString("user_id"))

        // MySQL에서 삭제
        MySQLServer.getInstance().addQuery("DELETE FROM pickpick.user WHERE user_id='$userId'")

        val sqLiteConnection = SQLiteConnection.getConnection(userId)
        // 채팅 목록 불러오기
        val chatIdsSql = "SELECT * FROM chat"
        sqLiteConnection.getResult(sqLiteConnection.addQuery(chatIdsSql)).content
            .map { Pair(it.getUUID("chat_id"), it.getUUID("receiver_id")) }
            .forEach {
                // 채팅 상대방 SQLiteConnection 탐색
                val opponentConnection = SQLiteConnection.getConnection(it.second)
                // 차단 목록에서 삭제
                opponentConnection.addQuery("DELETE FROM block where opponent_id='$userId'")
                // pick 목록에서 삭제
                opponentConnection.addQuery("DELETE FROM pick where opponent_id='$userId'")
                // picked 목록에서 삭제
                opponentConnection.addQuery("DELETE FROM picked where opponent_id='$userId'")
                // 채팅목록에서 삭제
                opponentConnection.addQuery("DELETE FROM chat where chat_id='${it.first}'")
                // 채팅 테이블 삭제
                opponentConnection.addQuery("DROP TABLE '${it.first}'")
            }
        sqLiteConnection.close()
        // 사용자 모든 파일 삭제
        File("${LocalFile.userRoot}/$userId").delete()

        return Connection.makePositiveResponse(command.uuid)
    }

    private fun blockUser(command: CommandContent): JSONObject {
        val userId = command.arguments.getUUID("user_id")
        val opponentId = command.arguments.getUUID("opponent_id")
        val chatId = command.arguments.getUUID("chat_id")

        val userConnection = SQLiteConnection.getConnection(userId)
        val opponentConnection = SQLiteConnection.getConnection(opponentId)
        val userInsertSql = "INSERT INTO block (opponent_id) VALUES ('$opponentId')"
        val opponentInsertSql = "INSERT INTO block (opponent_id) VALUES ('$userId')"
        val deleteSql = "DELETE FROM chat WHERE chat_id='$chatId'"
        val dropSql = "DROP TABLE '$chatId'"
        userConnection.addQuery(userInsertSql)
        userConnection.addQuery(deleteSql)
        userConnection.addQuery(dropSql)
        opponentConnection.addQuery(opponentInsertSql)
        opponentConnection.addQuery(deleteSql)
        opponentConnection.addQuery(dropSql)

        return Connection.makePositiveResponse(command.uuid)
    }

//    /**
//     * 사용자 프로필 이미지 경로 반환
//     * @param command: 인수는 다음이 포함되어야 함: 사용자 ID
//     * @return 이미지 ID 목록. 이미지가 존재하지 않을 때 [ServerResponse.IMAGE_NOT_FOUND]
//     */
//    private fun getUserImages(command: CommandContent): JSONObject {
//        val userId = UUID.fromString(command.arguments.getString("user_id"))
//
//        val images = File(LocalFile.getUserImagePath(userId)).list()?.map {
//            val splited = it.split(File.pathSeparator)
//            val imageName = splited[splited.size - 1]
//            val imageId = UUID.fromString(imageName.split(".")[0])
//
//            UserImageContent(userId, imageId, imageName, ServerPath.getUserImageUrl(userId, imageName))
//        }?.toJSONList()
//
//        return if (images != null && images.isNotEmpty()) {
//            Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("images", images.toJSONArray()) })
//        } else {
//            Connection.makeNegativeResponse(command.uuid, ServerResponse.IMAGE_NOT_FOUND)
//        }
//    }

    /**
     * 사용자 프로필 이미지 삭제
     * @param command: 인수는 다음이 포함되어야 함: 사용자 ID, 삭제할 이미지 ID
     * @return 삭제했을 때 true. 이미지가 존재하지 않을 때 [ServerResponse.IMAGE_ID_NOT_FOUND]
     */
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

    /**
     * 회원가입시 필요한 취향 질문 전체 탐색
     * @param command: 인수가 없는 명령
     * @return 질문 목록 전체
     */
    private fun getSignUpQuestion(command: CommandContent): JSONObject {
        val questions = SignUpQuestionUtil.getQuestions().toJSONList()

        return Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("questions", questions.toJSONArray()) })
    }

    /**
     * 회원가입시 선택한 취향 답변 전체 탐색
     * @param command: 인수는 다음을 포함해야함: 사용자 ID, 답변 목록 전체
     */
    private fun setSignUpQuestion(command: CommandContent): JSONObject {
        val userId = command.arguments.getString("user_id")
        val signUpQuestions = command.arguments.getJSONArray("sign_up_questions")
        signUpQuestions.saveJSONArray(LocalFile.getUserSignUpQuestionPath(userId))

        return Connection.makePositiveResponse(command.uuid)
    }

    /**
     * 회원가입시 선택한 MBTI 결과 저장
     * @param command: 인수는 다음을 포함해야함: 사용자 ID, MBTI 이름
     */
    private fun setMBTI(command: CommandContent): JSONObject {
        val userId = command.arguments.getString("user_id")
        val mbti = command.arguments.getString("mbti")
        JSONObject().apply { put("value", mbti) }.saveJSONObject(LocalFile.getUserMBTIPath(userId))

        return Connection.makePositiveResponse(command.uuid)
    }

    /**
     * 로그인
     * @param command: 인수는 다음을 포함해야함: 이메일, 비밀번호
     * @return 입력한 이메일과 비밀번호에 일치하는 사용자가 있을 경우 [UserContent], 없을 경우 [ServerResponse.EMAIL_NOT_FOUND]
     */
    private fun signIn(command: CommandContent, connection: Connection): JSONObject {
        val email = command.arguments.getString("email")
        // TODO: Encrypt
        val password = command.arguments.getString("password")

        return UserUtil.getUserByEmail(email, true)?.let {
            if (it.password == password) {
                connection.setToken(it.userId)
                Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("user", it.hidePassword().toJSONObject()) })
            } else {
                Connection.makeNegativeResponse(command.uuid, ServerResponse.EMAIL_NOT_FOUND)
            }
        } ?:let {
            Connection.makeNegativeResponse(command.uuid, ServerResponse.EMAIL_NOT_FOUND)
        }
    }

    private fun setCoordinator(command: CommandContent): JSONObject {
        val userId = command.arguments.getString("user_id")
        val coordinator =
            Coordinator(command.arguments.getJSONObject("coordinator"))
        val sql = "UPDATE pickpick.user " +
                "SET last_location_lng=${coordinator.longitude}, last_location_lat=${coordinator.latitude} " +
                "WHERE user_id='$userId'"
        MySQLServer.getInstance().addQuery(sql)

        return Connection.makePositiveResponse(command.uuid)
    }

    /**
     * 비밀번호 찾기
     * @param command: 인수는 다음을 포함해야함: 이메일, 질문 ID, 답변
     * @return 이메일에 해당하는 질문 ID와 답변이 일치할 경우 user_id, 그렇지 않을 경우 [ServerResponse.USER_NOT_FOUND]
     */
    private fun findPassword(command: CommandContent): JSONObject {
        val email = command.arguments.getString("email")
        val passwordQuestionId = command.arguments.getInt("password_question_id")
        // TODO: Encrypt
        val passwordAnswer = command.arguments.getString("password_answer")

        return UserUtil.getUserByEmail(email, true)?.let {
            if (it.passwordQuestionId == passwordQuestionId && it.passwordAnswer == passwordAnswer) {
                Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("user_id", it.userId.toString()) })
            } else {
                Connection.makeNegativeResponse(command.uuid, ServerResponse.USER_NOT_FOUND)
            }
        } ?: let {
            Connection.makeNegativeResponse(command.uuid, ServerResponse.USER_NOT_FOUND)
        }
    }

    /**
     * 비밀번호 변경
     * @param command: 인수는 다음을 포함해야함: 사용자 ID, 새 비밀번호
     */
    private fun updatePassword(command: CommandContent): JSONObject {
        val userId = command.arguments.getString("user_id")
        // TODO: Encrypt
        val password = command.arguments.getString("password")
        val sql = "UPDATE pickpick.user SET `password`='$password' WHERE `user_id`='$userId'"
        MySQLServer.getInstance().addQuery(sql)

        return Connection.makePositiveResponse(command.uuid)
    }

    /**
     * 카드스택 내용 불러오기
     * @param command: 인수는 다음을 포함해야함: 사용자 ID
     * @return 전체 사용자 중 자신, 이미 노출된 적 있는 사용자, 매장 점수가 30점 미만인 사용자, 프로필 사진이 없는 사용자를 제외한 모든 사용자
     */
    private fun getMatchableUsers(command: CommandContent): JSONObject {
        // 탐색을 시도하는 사용자 ID
        val userId = UUID.fromString(command.arguments.getString("user_id"))
        // 사용자 위치
        val userCoordinator = Coordinator(command.arguments.getJSONObject("coordinator"))
        // 탐색 필터
        val searchFilter = SearchFilter(command.arguments.getJSONObject("search_filter"))

        val sqLiteConnection = SQLiteConnection.getConnection(userId)
        // 내가 PICK 또는 NOPE한 사용자 탐색
        val pickSelectSql = "SELECT opponent_id FROM pick"
        val pickQueryId = sqLiteConnection.addQuery(pickSelectSql)
        val pickQueryResult = sqLiteConnection.getResult(pickQueryId)
        // 내가 차단 또는 나를 차단한 사용자 탐색
        val blockSelectSql = "SELECT opponent_id FROM block"
        val blockQueryId = sqLiteConnection.addQuery(blockSelectSql)
        val blockQueryResult = sqLiteConnection.getResult(blockQueryId)

        // 이미 목록에 나타났던 사용자 목록
        val metList = pickQueryResult.content.map { it.getUUID("opponent_id") }
        // 차단된 사용자 목록
        val blockedList = blockQueryResult.content.map { it.getUUID("opponent_id") }
        // 만나선 안 될 사용자 전체
        val missList = metList.merge(blockedList)

        val filteredCardStackContent = CardStackUtil.findAll(userId, missList, userCoordinator, searchFilter).toJSONArray()

        return Connection.makePositiveResponse(
            command.uuid,
            JSONObject().apply { put("card_stack_contents", filteredCardStackContent) }
        )
    }

    private fun refreshMatchableUsers(command: CommandContent): JSONObject {
        val userId = command.arguments.getUUID("user_id")
        val userCoordinator = Coordinator(command.arguments.getJSONObject("coordinator"))
        val searchFilter = SearchFilter(command.arguments.getJSONObject("search_filter"))
        val currentMetList = command.arguments.getJSONArray("current_met_list").toUUIDList()

        val sqLiteConnection = SQLiteConnection.getConnection(userId)
        val sql = "SELECT opponent_id FROM pick"
        val queryId = sqLiteConnection.addQuery(sql)
        val queryResult = sqLiteConnection.getResult(queryId)
        val metList = queryResult.content.mapTo(ArrayList<UUID>()) { it.getUUID("opponent_id") }
        metList.addAll(currentMetList)

        val filteredCardStackContent = CardStackUtil.findAll(userId, metList, userCoordinator, searchFilter).toJSONArray()

        return Connection.makePositiveResponse(
            command.uuid,
            JSONObject().apply { put("card_stack_contents", filteredCardStackContent) }
        )
    }

    private fun getDailyQuestions(command: CommandContent): JSONObject {
        val lastDate = command.arguments.getDate("last_date")
        val sql = "SELECT * FROM `daily_questions` WHERE date > str_to_date('$lastDate', '%Y-%m-%d')"
        val queryId = MySQLServer.getInstance().addQuery(sql)
        val queryResult = MySQLServer.getInstance().getResult(queryId)
        val questions = queryResult.content.map { DailyQuestionUtil.buildDailyQuestion(it) }.sorted().toJSONArray()

        return Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("daily_questions", questions) })
    }

    private fun isAnsweredQuestion(command: CommandContent): JSONObject {
        val userId = command.arguments.getUUID("user_id")
        val questionId = command.arguments.getUUID("question_id")

        val sql = "SELECT _id FROM daily_questions WHERE question_id='$questionId'"
        val sqLiteConnection = SQLiteConnection.getConnection(userId)
        val queryId = sqLiteConnection.addQuery(sql)
        val queryResult = sqLiteConnection.getResult(queryId)

        return Connection.makePositiveResponse(
            command.uuid,
            JSONObject().apply { put("answered", queryResult.getRowCount() != 0) }
        )
    }

    private fun answerQuestion(command: CommandContent): JSONObject {
        val userId = command.arguments.getUUID("user_id")
        val questionId = command.arguments.getUUID("question_id")
        val isPick = command.arguments.getBoolean("is_pick")

        val connection = SQLiteConnection.getConnection(userId)

        val selectSql = "SELECT _id FROM daily_questions"
        // 저장된 답변 수 탐색
        val selectId = connection.addQuery(selectSql)
        val selectResult = connection.getResult(selectId)
        if (selectResult.getRowCount() == 28) {
            // 답변이 28개일 때 앞에서 1개 삭제
            val deleteSql = "DELETE FROM daily_questions WHERE _id=${selectResult.content[0].getInt("_id")}"
            connection.addQuery(deleteSql)
        }

        val insertSql = "INSERT INTO daily_questions (question_id, is_picked) VALUES ('$questionId', $isPick)"
        SQLiteConnection.getConnection(userId).addQuery(insertSql)

        return Connection.makePositiveResponse(command.uuid)
    }

    /**
     * PICK
     * 특정 사용자를 PICK
     * @param command: 인수는 다음을 포함해야함: 사용자 ID, 상대방 ID
     */
    private fun pick(command: CommandContent): JSONObject {
        val userId = UUID.fromString(command.arguments.getString("user_id"))
        val opponentId = UUID.fromString(command.arguments.getString("opponent_id"))
        val isPick = command.arguments.getBoolean("is_pick")

        val userConnection = SQLiteConnection.getConnection(userId)
        // 사용자 pick에 상대방 추가
        val userInsertSql = "INSERT INTO pick (opponent_id, is_picked) VALUES ('$opponentId', $isPick)"
        userConnection.addQuery(userInsertSql)
        // 사용자가 상대방에게 pick됐는지 탐색
        val userSelectSql = "SELECT * FROM picked WHERE opponent_id='$opponentId'"
        val queryId = userConnection.addQuery(userSelectSql)
        val queryResult = userConnection.getResult(queryId)
        // 상대방 pick에 사용자 추가
        val opponentSql = "INSERT INTO picked (opponent_id) VALUES ('$userId')"
        SQLiteConnection.getConnection(opponentId).addQuery(opponentSql)

        return Connection.makePositiveResponse(command.uuid, JSONObject().put("is_picked", queryResult.content.isNotEmpty()))
    }

    /**
     * 채팅방 생성
     *
     * @param command: 인수는 다음을 포함해야함: 사용자 ID, 상대방 ID
     * @return 생성된 채팅 ID
     */
    private fun createChat(command: CommandContent): JSONObject {
        val chatId = UUID.randomUUID()
        val senderId = UUID.fromString(command.arguments.getString("sender_id"))
        val receiverId = UUID.fromString(command.arguments.getString("receiver_id"))

        val senderConnection = SQLiteConnection.getConnection(senderId)
        val receiverConnection = SQLiteConnection.getConnection(receiverId)

        // 사용자 채팅방 생성
        senderConnection.addQuery(SQLiteConnection.getCreateChatSql(chatId))
        // 사용자 채팅방 정보 삽입
        senderConnection.addQuery(SQLiteConnection.getInsertNewChatSql(chatId, receiverId))
        // 첫 메시지 삽입
        senderConnection.addQuery(SQLiteConnection.getInsertFirstMessageSql(chatId, senderId, receiverId))

        // 상대방 채팅방 생성
        receiverConnection.addQuery(SQLiteConnection.getCreateChatSql(chatId))
        // 상대방 채팅방 정보 삽입
        receiverConnection.addQuery(SQLiteConnection.getInsertNewChatSql(chatId, senderId))
        // 첫 메시지 삽입
        receiverConnection.addQuery(SQLiteConnection.getInsertFirstMessageSql(chatId, senderId, receiverId))

        // MySQL 채팅 정보 삽입
        MySQLServer.getInstance().addQuery("INSERT INTO pickpick.chat (" +
                "chat_id, participant1, participant2" +
                ") VALUES (" +
                "'$chatId', '$senderId', '$receiverId')")

        // 사용자 알림 보내기
        NotificationServer.getInstance().addNotification(NotificationForm(
            notification = Notification.MATCHED,
            receiverId = senderId,
            title = "매칭되었습니다.",
            content = "서로 PICK했어요! 메시지를 확인해보세요."
        ))
        // 상대방 알림 보내기
        NotificationServer.getInstance().addNotification(NotificationForm(
            notification = Notification.MATCHED,
            receiverId = receiverId,
            title = "매칭되었습니다.",
            content = "내가 PICK한 사용자가 나를 PICK했어요! 메시지를 확인해보세요."
        ))

        return Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("chat_id", chatId.toString()) })
    }

    private fun deleteChat(command: CommandContent): JSONObject {
        val chatContent = ChatContent(command.arguments.getJSONObject("chat_content"))
        val dropSql = "DROP TABLE ${chatContent.chatId}"
        val deleteSql = "DELETE FROM pickpick.chat WHERE chat_id='${chatContent.chatId}'"

        SQLiteConnection.getConnection(chatContent.participant1).addQuery(dropSql)
        SQLiteConnection.getConnection(chatContent.participant2).addQuery(dropSql)
        MySQLServer.getInstance().addQuery(deleteSql)

        return Connection.makePositiveResponse(command.uuid)
    }

    private fun getMessages(command: CommandContent): JSONObject {
        val userId = UUID.fromString(command.arguments.getString("user_id"))
        val chatId = UUID.fromString(command.arguments.getString("chat_id"))

        val sqlConnection = SQLiteConnection.getConnection(userId)
        // 채팅 전체 열 수를 가져옴
        val rows = sqlConnection.getResult(sqlConnection.addQuery("SELECT count(_id) from '$chatId'")).content[0].getInt("count(_id)")
        val sql = if (rows <= SQLiteConnection.SELECT_MESSAGE_LIMIT) {
            // 전체가 20보다 작거나 같을 때 전부 읽음
            "SELECT * FROM '$chatId'"
        } else {
            // 전체가 20보다 클 때 마지막부터 20개를 읽음
            SQLiteConnection.getSelectMessageSql(chatId, rows)
        }
        val queryResult = sqlConnection.getResult(sqlConnection.addQuery(sql))
        val result = queryResult.content
            .map { MessageUtil.buildMessage(it, chatId, userId) }
            .sorted()
            .toJSONArray()

        return Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("messages", result) })
    }

    private fun refreshMessages(command: CommandContent): JSONObject {
        val userId = UUID.fromString(command.arguments.getString("user_id"))
        val chatId = UUID.fromString(command.arguments.getString("chat_id"))
        val lastTimestamp = command.arguments.getLong("last_timestamp")

        val sqlConnection = SQLiteConnection.getConnection(userId)
        val sql = "SELECT * from '$chatId' where timestamp > $lastTimestamp"
        val queryResult = sqlConnection.getResult(sqlConnection.addQuery(sql))
        val result = queryResult.content
            .map { MessageUtil.buildMessage(it, chatId, userId) }
            .sortedBy { it.timestamp }
            .toJSONArray()

        return Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("messages", result) })
    }

    private fun getLastMessages(command: CommandContent): JSONObject {
        val userId = UUID.fromString(command.arguments.getString("user_id"))

        val connection = SQLiteConnection.getConnection(userId)
        val chatIds = connection.getResult(connection.addQuery("SELECT chat_id from chat")).content.map { it.getUUID("chat_id") }
        val lastMessages = JSONList<MessageContent>().also { list: JSONList<MessageContent> ->
            chatIds.forEach {
                val sql = "SELECT * FROM '$it' where _id = (SELECT MAX(_id) FROM '$it')"
                list.add(MessageUtil.buildMessage(connection.getResult(connection.addQuery(sql)).content[0], it, userId))
            }
        }

        return Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("messages", lastMessages.toJSONArray()) })
    }

    private fun sendMessage(command: CommandContent): JSONObject {
        val timestamp = System.currentTimeMillis()
        val chatId = UUID.fromString(command.arguments.getString("chat_id"))
        val senderId = UUID.fromString(command.arguments.getString("sender_id"))
        val receiverId = UUID.fromString(command.arguments.getString("receiver_id"))
        val opponentName = command.arguments.getString("opponent_name")
        val body = command.arguments.getString("body")
        val messageContent = MessageContent(
            chatId = chatId,
            senderId = senderId,
            receiverId = receiverId,
            opponentName = opponentName,
            timestamp = timestamp,
            body = body
        )

        val senderConnection = SQLiteConnection.getConnection(senderId)
        val receiveConnection = SQLiteConnection.getConnection(receiverId)

        senderConnection.addQuery(messageContent.getLocalInsertMessageSql())
        receiveConnection.addQuery(messageContent.getLocalInsertMessageSql())
        MySQLServer.getInstance().addQuery(messageContent.getServerInsertMessageSql())

        NotificationServer.getInstance().addNotification(NotificationForm(
            notification = Notification.MESSAGE_RECEIVED,
            receiverId = receiverId,
            title = "메시지가 도착했습니다.",
            content = "메시지를 확인해보세요.",
            extra = messageContent.toJSONObject()
        ))

        return Connection.makePositiveResponse(command.uuid, JSONObject().apply { put("timestamp", timestamp) })
    }
}