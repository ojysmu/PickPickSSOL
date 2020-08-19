package mbtinder.server.util

import mbtinder.lib.component.card_stack.CardStackContent
import mbtinder.lib.component.card_stack.DailyQuestionContent
import mbtinder.lib.component.database.Row
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.component.user.SearchFilter
import mbtinder.lib.component.user.SignUpQuestionContent
import mbtinder.lib.constant.MBTI
import mbtinder.lib.util.*
import mbtinder.server.constant.LocalFile
import mbtinder.server.io.database.MySQLServer
import mbtinder.server.io.database.SQLiteConnection
import java.util.*

object CardStackUtil {
    private const val UPDATE_DURATION = 60 * 1000

    private var cardStacks: ImmutableList<CardStackContent>? = null
    private var lastUpdate: Long = 0

    private fun updateCardStacks(): ImmutableList<CardStackContent> {
        val sql = "SELECT user_id, name, age, gender, last_location_lng, last_location_lat, description FROM pickpick.user"
        val queryId = MySQLServer.getInstance().addQuery(sql)
        val queryResult = MySQLServer.getInstance().getResult(queryId)
        cardStacks = queryResult.content
            .map { buildCardStackContent(it) }
            .sorted()
            .toImmutableList()

        return cardStacks!!
    }

    private fun ensureUpdate() {
        if (lastUpdate < System.currentTimeMillis() - UPDATE_DURATION) {
            updateCardStacks()
        }
    }

    fun findByUserId(userId: UUID): CardStackContent? {
        ensureUpdate()

        return findBinaryTwice(cardStacks!!) { it.userId.compareTo(userId) }
    }

    fun findByUserIds(vararg userIds: UUID): List<CardStackContent> {
        ensureUpdate()

        return userIds.map { userId ->
            cardStacks!![cardStacks!!.binarySearch { card -> card.userId.compareTo(userId) }].getCloned()
        }
    }

    fun findAll(finderId: UUID, missList: List<UUID>, finderCoordinator: Coordinator, filter: SearchFilter): List<CardStackContent> {
        ensureUpdate()
        // 사용자 MBTI
        val finderMBTI = findMBTI(finderId)
        // 가입 시 입력한 취향
        val finderSignUpQuestionContents = findSignUpQuestion(finderId)
        // 일일 질문
        val finderDailyQuestionContents = findDailyQuestion(finderId)

        // 최대 10개 index
        var index = 0

        return cardStacks!!.clone()
            .asSequence()
            .filter {
                // 10명까지만
                if (index == 10) return@filter false
                // 점수 계산
                it.score = UserUtil.getMatchingScore(finderMBTI, finderSignUpQuestionContents, finderDailyQuestionContents, it)
                // 자기 자신 제외
                it.userId != finderId
                        // 이미 만난 사용자 제외
                        && !missList.contains(it.userId)
                        // 거리, 나이, 성별이 맞지 않으면 제외
                        && filter.isInRange(finderCoordinator, it)
                        // 매칭 점수가 30점 미만일 때 제외
                        && it.score >= 30
                        // 10명까지만
                        && index++ < 10
            }
            .sortedBy { it.score } // 점수 정렬
            .toList()
    }

    fun buildCardStackContent(row: Row) = CardStackContent(
        row.getUUID("user_id"),
        row.getString("name"),
        row.getInt("age"),
        row.getInt("gender"),
        Coordinator(row.getDouble("last_location_lng"), row.getDouble("last_location_lat")),
        row.getString("description"),
        findMBTI(row.getUUID("user_id")),
        SignUpQuestionUtil.parseFilled(findSignUpQuestion(row.getUUID("user_id"))).toJSONList()
    )

    private fun findMBTI(userId: UUID) =
        MBTI.findByName(loadJSONObject(LocalFile.getUserMBTIPath(userId)).getString("value"))

    private fun findSignUpQuestion(userId: UUID) =
        loadJSONArray(LocalFile.getUserSignUpQuestionPath(userId)).toJSONList<SignUpQuestionContent.ConnectionForm>()

    fun findDailyQuestion(userId: UUID): List<DailyQuestionContent.SaveForm> {
        val connection = SQLiteConnection.getConnection(userId)
        val sql = "SELECT question_id, is_picked FROM daily_questions"
        val queryId = connection.addQuery(sql)
        val queryResult = connection.getResult(queryId)

        return queryResult.content.map { DailyQuestionContent.SaveForm(it) }
    }
}