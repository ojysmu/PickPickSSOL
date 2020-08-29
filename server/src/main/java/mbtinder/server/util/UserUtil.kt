package mbtinder.server.util

import mbtinder.lib.component.card_stack.DailyQuestionContent
import mbtinder.lib.component.database.Row
import mbtinder.lib.component.user.SearchFilter
import mbtinder.lib.component.user.SignUpQuestionContent
import mbtinder.lib.component.user.UserContent
import mbtinder.lib.constant.MBTI
import mbtinder.lib.util.*
import mbtinder.server.constant.LocalFile
import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.database.SQLiteConnection
import java.util.*

object UserUtil {
    // 사용자 ID에 따라 MBTI 정보가 저장되는 캐시
    // 가입 이후 변경될 수 없으니 캐시 활용에 적합
    private val mbtis = hashMapOf<UUID, MBTI>()
    // 사용자 ID에 따라 가입 취향 질문 정보가 저장되는 캐시
    // 가입 이후 변경될 수 없으니 캐시 활용에 적합
    private val signUpQuestions = hashMapOf<UUID, JSONList<SignUpQuestionContent.ConnectionForm>>()
    // 두 사용자 ID에 따라 MBTI, 가입 취향 질문을 바탕으로 산출된 점수가 저장되는 캐시
    // 가입 이후 변경될 수 없으니 캐시 활용에 적합
    private val basicScores = hashMapOf<Pair<UUID, UUID>, Int>()
    // 사용자 ID에 따라 일일 질문 정보가 저장되는 캐시
    // 가입 이후 변경될 수 있으나 답변은 추가만 가능하므로 추가만 적용, public 유지 (CommandProcess.answerQuestion)
    val dailyQuestions = hashMapOf<UUID, IDList<DailyQuestionContent.SaveForm>>()

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

    /**
     * 기본 매칭 점수(MBTI, 가입 질문) 반환
     *
     * 점수는 MBTI * 10, 가입 질문 각 4점 계산
     *
     * @param userId 요청자 ID
     * @param opponentId 점수 산출 대상 사용자 ID
     * @return 계산된 점수
     */
    private fun getBasicMatchingScore(userId: UUID, opponentId: UUID): Int {
        var sum = 0

        val userMBTI = findMBTI(userId)
        val userSignUpQuestions = findSignUpQuestion(userId)

        val opponentMBTI = findMBTI(opponentId)
        val opponentSignUpQuestions = findSignUpQuestion(opponentId)

        val mbtiMatching = userMBTI.getState(opponentMBTI)
        sum += mbtiMatching * 10

        userSignUpQuestions.forEach { userForm ->
            opponentSignUpQuestions.find { opponentForm ->
                opponentForm.questionId == userForm.questionId && opponentForm.selected == userForm.selected
            }?.let { sum += 4 }
        }

        return sum
    }

    /**
     * 일일 질문 매칭 점수 반환
     *
     * 점수는 각 1점
     *
     * @param userDailyQuestions 요청자 일일 질문 답변 목록
     * @param opponentId 점수 산출 대상자 ID
     * @return 계산된 점수
     */
    fun getDailyScore(userDailyQuestions: List<DailyQuestionContent.SaveForm>, opponentId: UUID): Int {
        var sum = 0
        val opponentDailyQuestions = findDailyQuestion(opponentId)
        userDailyQuestions.forEach { userForm ->
            opponentDailyQuestions.find { opponentForm ->
                opponentForm.questionId == userForm.questionId && opponentForm.isPicked == userForm.isPicked
            }?.let { sum++ }
        }

        return sum
    }

    /**
     * 사용자 [MBTI] 검색. 캐시에 저장되어있지 않다면 캐시 저장 후 반환
     *
     * @param userId 탐색할 대상 사용자 ID
     * @return 대상 사용자의 [MBTI]
     */
    fun findMBTI(userId: UUID): MBTI =
        mbtis[userId]
            ?.let { return it }
            ?:let {
                mbtis[userId] = MBTI.findByName(loadJSONObject(LocalFile.getUserMBTIPath(userId)).getString("value"))
                return mbtis[userId]!!
            }

    /**
     * 사용자 [SignUpQuestionContent] 검색. 캐시에 저장되어있지 않다면 캐시 저장 후 반환
     *
     * @param userId 탐색할 대상 사용자 ID
     * @return 대상 사용자의 [SignUpQuestionContent]
     */
    fun findSignUpQuestion(userId: UUID): JSONList<SignUpQuestionContent.ConnectionForm> =
        signUpQuestions[userId]
            ?.let { return it }
            ?:let {
                signUpQuestions[userId] = loadJSONArray(LocalFile.getUserSignUpQuestionPath(userId)).toJSONList()
                return signUpQuestions[userId]!!
            }

    /**
     * 두 사용자의 기본 점수(MBTI, 가입 점수) 검색. 캐시에 저장되어있지 않다면 캐시 저장 후 반환
     *
     * @param userId 사용자1
     * @param opponentId 사용자2
     * @return 계산된 점수
     */
    fun findScore(userId: UUID, opponentId: UUID): Int {
        val found1 = basicScores[Pair(userId, opponentId)]
        found1?.let { return it }
        val found2 = basicScores[Pair(opponentId, userId)]
        found2?.let { return it }
        val basicScore = getBasicMatchingScore(userId, opponentId)
        basicScores[Pair(userId, opponentId)] = basicScore
        return basicScore
    }

    /**
     * 사용자 [DailyQuestionContent] 검색. 캐시에 저장되어있지 않다면 캐시 저장 후 반환
     *
     * @param userId 탐색할 대상 사용자 ID
     * @return 대상 사용자의 [DailyQuestionContent]
     */
    fun findDailyQuestion(userId: UUID): IDList<DailyQuestionContent.SaveForm> {
        dailyQuestions[userId]
            ?.let { return it }
            ?:let {
                val connection = SQLiteConnection.getConnection(userId)
                val sql = "SELECT question_id, is_picked FROM daily_questions"
                val queryId = connection.addQuery(sql)
                val queryResult = connection.getResult(queryId)
                dailyQuestions[userId] = queryResult.content.map { DailyQuestionContent.SaveForm(it) }.toIDList()
                return dailyQuestions[userId]!!
            }
    }
}

fun UserContent.hidePassword() = getCloned().apply {
    password = ""
    passwordAnswer = ""
}