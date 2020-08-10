package mbtinder.server.util

import mbtinder.lib.component.CardStackContent
import mbtinder.lib.constant.MBTI
import mbtinder.lib.util.*
import mbtinder.server.constant.LocalFile
import java.util.*

object CardStackUtil {
    private const val UPDATE_DURATION = 60 * 1000

    private var cardStacks: ImmutableList<CardStackContent>? = null
    private var lastUpdate: Long = 0

    private fun updateCardStacks(): ImmutableList<CardStackContent> {
        val userIds = UserUtil.getUserIds()
        val updated = ImmutableList<CardStackContent>()
        userIds.forEach {
            val mbti = MBTI.findByName(loadJSONObject(LocalFile.getUserMBTIPath(it)).getString("value"))
            val signUpQuestions = SignUpQuestionUtil.parseFilled(
                loadJSONList(LocalFile.getUserSignUpQuestionPath(it))
            ).toJSONList()

            updated.add(CardStackContent(it, mbti, signUpQuestions))
        }

        cardStacks = updated.sorted().toImmutableList()
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
            cardStacks!![cardStacks!!.binarySearch { card: CardStackContent -> card.userId.compareTo(userId) }]
        }
    }
}