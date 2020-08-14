package mbtinder.server.util

import mbtinder.lib.component.CardStackContent
import mbtinder.lib.component.database.Row
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.component.user.SearchFilter
import mbtinder.lib.component.user.SignUpQuestionContent
import mbtinder.lib.component.user.UserContent
import mbtinder.lib.constant.MBTI
import mbtinder.lib.util.*
import mbtinder.server.constant.LocalFile
import mbtinder.server.io.database.MySQLServer
import java.util.*

object CardStackUtil {
    private const val UPDATE_DURATION = 60 * 1000

    private var cardStacks: ImmutableList<CardStackContent>? = null
    private var lastUpdate: Long = 0

    private fun updateCardStacks(): ImmutableList<CardStackContent> {
        val sql = CardStackContent.getSelectAllSql()
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

        return findBinaryTwice(cardStacks!!, this::updateCardStacks) { it.userId.compareTo(userId) }
    }

    fun findByUserIds(vararg userIds: UUID): List<CardStackContent> {
        ensureUpdate()

        return userIds.map { userId: UUID ->
            cardStacks!![cardStacks!!.binarySearch { card: CardStackContent -> card.userId.compareTo(userId) }].getCloned()
        }
    }

    fun findAll(finderId: UUID, metList: List<UUID>, finderCoordinator: Coordinator, filter: SearchFilter): List<CardStackContent> {
        ensureUpdate()

        // 사용자 MBTI
        val finderMBTI = findMBTI(finderId)
        // 가입 시 입력한 취향
        val finderSignUpQuestionContents = findSignUpQuestion(finderId)

        return cardStacks!!.clone()
            .filter { val x = it.userId != finderId && !metList.contains(it.userId); println("F1: id=${it.userId}, $x"); x }
            .filter { val x = filter.isInRange(finderCoordinator, it); println("F2: id=${it.userId}, $x"); x }
            .filter { val x = UserUtil.getMatchingScore(finderMBTI, finderSignUpQuestionContents, it) >= 30; println("F2: id=${it.userId}, $x"); x }
            .sorted()
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
}