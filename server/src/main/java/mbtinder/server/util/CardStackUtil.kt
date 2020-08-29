package mbtinder.server.util

import mbtinder.lib.component.card_stack.CardStackContent
import mbtinder.lib.component.database.Row
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.component.user.SearchFilter
import mbtinder.lib.util.ImmutableList
import mbtinder.lib.util.toImmutableList
import mbtinder.lib.util.toJSONList
import mbtinder.server.io.database.MySQLServer
import mbtinder.server.util.UserUtil.findDailyQuestion
import mbtinder.server.util.UserUtil.findMBTI
import mbtinder.server.util.UserUtil.findScore
import mbtinder.server.util.UserUtil.findSignUpQuestion
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

    fun findAll(finderId: UUID, missList: List<UUID>, finderCoordinator: Coordinator, filter: SearchFilter): List<CardStackContent> {
        ensureUpdate()
        // 최대 10개 index
        var index = 0
        return cardStacks!!.clone()
            .asSequence()
            .filter {
                // 자기 자신 제외
                it.userId != finderId
                        && (index < 10)
                        // 이미 만난 사용자 제외
                        && !missList.contains(it.userId)
                        // 거리, 나이, 성별이 맞지 않으면 제외
                        && filter.isInRange(finderCoordinator, it)
                        // 매칭 점수가 30점 미만일 때 제외
                        && run {
                    // 기본 점수(MBTI, 가입 질문) 계산
                    val basicScore = findScore(finderId, it.userId)
                    if (basicScore >= 30) {
                        // 30점 넘으면 일일 질문 필요없음
                        it.score = basicScore
                        index++
                        true
                    } else if (basicScore < 2) {
                        // 2점 미만이면 일일 질문 전부 일치해도 매칭 불가
                        false
                    } else {
                        // (0,30)점 일일질문 계산
                        val dailyScore = UserUtil.getDailyScore(findDailyQuestion(finderId), it.userId)
                        it.score = basicScore + dailyScore
                        if (it.score >= 30) {
                            index++
                            true
                        } else {
                            false
                        }
                    }
                }
            }
            .sortedBy { it.score } // 점수 정렬
            .toList()
    }

    private fun buildCardStackContent(row: Row) = CardStackContent(
        row.getUUID("user_id"),
        row.getString("name"),
        row.getInt("age"),
        row.getInt("gender"),
        Coordinator(row.getDouble("last_location_lng"), row.getDouble("last_location_lat")),
        row.getString("description"),
        findMBTI(row.getUUID("user_id")),
        SignUpQuestionUtil.parseFilled(findSignUpQuestion(row.getUUID("user_id"))).toJSONList()
    )
}