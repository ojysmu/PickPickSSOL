package mbtinder.android.ui.fragment.home

import android.view.View
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.annotation.AnyThread
import androidx.annotation.WorkerThread
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.fragment_home.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.Log
import mbtinder.android.util.SharedPreferencesUtil
import mbtinder.android.util.runOnBackground
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.card_stack.BaseCardStackContent
import mbtinder.lib.component.card_stack.CardStackContent
import mbtinder.lib.component.card_stack.DailyQuestionContent
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.util.IDList
import org.json.JSONObject
import java.sql.Date
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {
    companion object {
        private val leftContents = arrayListOf<CardStackContent>()
        private val rewindableContents = arrayListOf<CardStackContent>()
        private val swipedContents = IDList<CardStackContent>()
        val visibleContents = arrayListOf<BaseCardStackContent>()
    }

    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private val cardStackAdapter by lazy { CardStackAdapter(this) }
    private val todayQuestions by lazy { getNotAnsweredQuestion(getTodayDailyQuestions()).toMutableList() }
    private var isRefreshing = false
    private var rewindMap = hashMapOf<UUID, Boolean>()

    override fun initializeView() {
        leftContents.clear()
        rewindableContents.clear()
        swipedContents.clear()
        visibleContents.clear()

        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE
        getCardStacks()
        getDailyQuestions()
        initializeCardStackLayoutManager()
        initializeCardStackView()
        home_rewind.setOnClickListener { onRewindClicked() }
    }

    private fun initializeCardStackLayoutManager() {
        cardStackLayoutManager = CardStackLayoutManager(requireContext(), cardStackListener)
        cardStackLayoutManager.setStackFrom(StackFrom.None)
        cardStackLayoutManager.setVisibleCount(3)
        cardStackLayoutManager.setTranslationInterval(8.0f)
        cardStackLayoutManager.setScaleInterval(0.95f)
        cardStackLayoutManager.setSwipeThreshold(0.3f)
        cardStackLayoutManager.setMaxDegree(20.0f)
        cardStackLayoutManager.setDirections(Direction.HORIZONTAL)
        cardStackLayoutManager.setCanScrollHorizontal(true)
        cardStackLayoutManager.setCanScrollVertical(false)
        cardStackLayoutManager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        cardStackLayoutManager.setOverlayInterpolator(LinearInterpolator())
    }

    private fun initializeCardStackView() {
        home_card_stack_view.layoutManager = cardStackLayoutManager
        home_card_stack_view.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
        home_card_stack_view.adapter = cardStackAdapter
    }

    private fun onRewindClicked() {
        if (rewindableContents.isEmpty()) return

        val setting = RewindAnimationSetting.Builder()
            .setDirection(Direction.Left)
            .setDuration(200)
            .setInterpolator(DecelerateInterpolator())
            .build()
        cardStackLayoutManager.setRewindAnimationSetting(setting)

        val rewoundContent = rewindableContents.removeAt(rewindableContents.lastIndex)
//        visibleContents.add(0, rewoundContent)
//        cardStackAdapter.notifyItemInserted(0)
//        val viewHolder = cardStackAdapter.createViewHolder(home_card_stack_view, CardStackAdapter.TYPE_CARD_STACK_CONTENT)
//        cardStackAdapter.bindViewHolder(viewHolder, 0)
        rewindMap[rewoundContent.getUUID()] = true
        home_card_stack_view.rewind()
//        home_card_stack_view.rewind()
//        home_card_stack_view.smoothScrollToPosition(cardStackLayoutManager.topPosition - 1)
    }

    /**
     * 카드 스택 최초 생성
     * 카드 스택이 존재하지 않고, 최초 로딩 시 호출
     */
    @AnyThread
    private fun getCardStacks() {
        runOnBackground {
            val isFirst = arguments?.getBoolean("is_first") ?: false

            val getResult = CommandProcess.getMatchableUsers(
                userId = StaticComponent.user.userId,
                coordinator = Coordinator(StaticComponent.user.lastLocationLng, StaticComponent.user.lastLocationLat),
                searchFilter = StaticComponent.user.searchFilter
            )

            if (getResult.isSucceed) {
                leftContents.addAll(getResult.result!!)
                validateLeftContents()

                if (isFirst) {
                    visibleContents.add(0, TutorialContent())
                }

                validateLeftContents()
                when {
                    leftContents.size >= 2 -> {
                        visibleContents.add(leftContents.removeAt(0))
                        visibleContents.add(leftContents.removeAt(0))
                    }
                    leftContents.size == 1 -> {
                        visibleContents.add(leftContents.removeAt(0))
                    }
                    else -> {
                        visibleContents.add(EmptyContent())
                    }
                }

                runOnUiThread {
                    cardStackAdapter.notifyDataSetChanged()
                    home_waiting?.let { it.visibility = View.INVISIBLE }
                    home_card_stack_view?.let { it.visibility = View.VISIBLE }
                }
            }
        }
    }

    /**
     * 카드 스택 업데이트
     * 이미 카드 스택이 존재하고, 추가로 필요할 떄 호출
     * 오늘의 질문이 남았을 경우 하나만 맨 뒤에 추가
     */
    @AnyThread
    private fun refreshCardStacks() {
        if (isRefreshing) return else isRefreshing = true

        runOnBackground {
            val refreshResult = CommandProcess.refreshMatchableUsers(
                userId = StaticComponent.user.userId,
                coordinator = Coordinator(StaticComponent.user.lastLocationLng, StaticComponent.user.lastLocationLat),
                searchFilter = StaticComponent.user.searchFilter,
                currentMetList = swipedContents.map { it.userId }
            )

            if (refreshResult.isSucceed && refreshResult.result!!.isNotEmpty()) {
                leftContents.addAll(refreshResult.result!!)
                validateLeftContents()

                if (visibleContents.removeIf { it is EmptyContent }) {
                    runOnUiThread { cardStackAdapter.notifyDataSetChanged() }
                }

                if (todayQuestions.isNotEmpty()) {
                    visibleContents.add(todayQuestions.removeAt(0))
                    runOnUiThread {
                        cardStackAdapter.notifyItemInserted(visibleContents.size - 1)
                        isRefreshing = false
                    }
                } else {
                    isRefreshing = false
                }
            }
        }
    }

    /**
     * 로컬 일일 질문 갱신
     * 마지막 저장된 날짜 이후의 모든 질문 다운로드
     */
    @AnyThread
    private fun getDailyQuestions() {
        runOnBackground {
            val context = SharedPreferencesUtil.getContext(requireContext(), SharedPreferencesUtil.PREF_QUESTIONS)
            val saved = context.getStringList("questions")
            val lastDate = saved
                .map { DailyQuestionContent(JSONObject(it)) }
                .max()
                ?.date ?: Date(System.currentTimeMillis() - 86400000)

            val getResult = CommandProcess.getDailyQuestions(lastDate)
            if (getResult.isSucceed) {
                saved.addAll(getResult.result!!.map { it.toJSONObject().toString() })
                context.put("questions", saved)
            }
        }
    }

    private fun validateLeftContents() {
        var removed = 0
        leftContents.removeIf { content ->
            if (swipedContents.contains(content.getUUID())) {
                removed++
                true
            } else {
                false
            }
        }
        Log.v("HomeFragment.validateLeftContents(): from=${Thread.currentThread().stackTrace[3]}  removed=$removed")
    }

    /**
     * 인수로 전달된 질문 중 답변되지 않은 질문 반환. 반드시 background에서 실행되어야 함
     * @param questions: 답변되었는지 확인되지 않은 질문
     * @return 답변되지 않은 질문
     */
    @WorkerThread
    private fun getNotAnsweredQuestion(questions: List<DailyQuestionContent>) =
        questions.filter { !CommandProcess.isAnsweredQuestion(it.questionId) }

    /**
     * 로컬에서 오늘의 일일 질문 반환. [getDailyQuestions]이 같은 날 선행되었음이 보장되어야 함
     * @return 오늘의 일일 질문
     */
    @AnyThread
    private fun getTodayDailyQuestions(): List<DailyQuestionContent> {
        return SharedPreferencesUtil.getContext(requireContext(), SharedPreferencesUtil.PREF_QUESTIONS)
            .getStringList("questions")
            .map { DailyQuestionContent(JSONObject(it)) }
            .filter { it.date.toString() == Date(System.currentTimeMillis()).toString() }
    }

    /**
     * 카드 이벤트 callback listener
     * onCardAppeared -> onCardDragging -> onCardDisappeared -> onCardSwiped
     */
    private val cardStackListener = object : CardStackListener {
        private var currentViewHolder: BaseCardStackViewHolder? = null
        private var head = 0

        /**
         * 카드가 나타났을 때 callback
         * 일반 카드일 경우 swipe 가능, 일일 질문일 경우 swipe 가능, 목록의 끝일 경우 swipe 불가능
         */
        override fun onCardAppeared(view: View?, position: Int) {
            Log.v("HomeFragment.validateLeftContents(): from=${Thread.currentThread().stackTrace[3]}  leftContents=${leftContents.size}, visibleContents=${visibleContents.size}, swipedContents=${swipedContents.size}")
            when (visibleContents[0]) {
                is TutorialContent -> {
                    view?.let { currentViewHolder = TutorialViewHolder(it) }
                    cardStackLayoutManager.setCanScrollHorizontal(true)
                }
                is CardStackContent -> {
                    view?.let { currentViewHolder = CardStackViewHolder(it) }
                    cardStackLayoutManager.setCanScrollHorizontal(true)
                }
                is DailyQuestionContent -> {
                    view?.let { currentViewHolder = DailyQuestionViewHolder(it) }
                    cardStackLayoutManager.setCanScrollHorizontal(true)
                }
                else -> {
                    view?.let { currentViewHolder = EmptyViewHolder(it, this@HomeFragment) }
                    cardStackLayoutManager.setCanScrollHorizontal(false)
                }
            }
        }

        /**
         * 카드 드래그할 때 callback
         * 일반 카드일 경우 배경 어둡게, PICK, NOPE 노출, 일일 질문일 경우 답변 highlight
         */
        override fun onCardDragging(direction: Direction?, ratio: Float) {
            when (val viewHolder = currentViewHolder) {
                is CardStackViewHolder -> {
                    when {
                        ratio < 0.1f -> viewHolder.setDefaultTransparency()
                        direction == Direction.Left -> viewHolder.setNopeTransparency(ratio)
                        direction == Direction.Right -> viewHolder.setPickTransparency(ratio)
                    }
                }
                is DailyQuestionViewHolder -> {
                    when {
                        ratio < 0.1f -> viewHolder.disableAll()
                        direction == Direction.Left -> viewHolder.enableNope()
                        direction == Direction.Right -> viewHolder.enablePick()
                    }
                }
            }
        }

        /**
         * 카드가 사라졌을 때 callback
         * 카드가 세장 남았을 경우 refresh
         */
        override fun onCardDisappeared(view: View?, position: Int) {
            when (val viewHolder = currentViewHolder) {
                is CardStackViewHolder -> viewHolder.setDefaultTransparency()
                is DailyQuestionViewHolder -> viewHolder.disableAll()
            }

            if (leftContents.size < 4) {
                refreshCardStacks()
            }
        }

        /**
         * 카드가 swipe 됐을 때 callback
         * 일반 카드일 경우 PICK, NOPE 여부 서버 전송, PICK일 경우 목록에서 삭제(rewind 방지)
         * 일일 질문일 경우 PICK, NOPE 여부 서버 전송, 목록에서 삭제
         */
        override fun onCardSwiped(direction: Direction?) {
            val isRewindable = visibleContents[head] is CardStackContent && direction == Direction.Left
            val item = if (isRewindable) {
                visibleContents[head++]
            } else {
                visibleContents.removeAt(head)
            }

            when (item) {
                is CardStackContent -> onCardContentSwiped(direction, item)
                is DailyQuestionContent -> onDailyQuestionSwiped(direction, item)
            }

            if (!isRewindable) {
                cardStackAdapter.notifyItemRemoved(0)
            }

            if (leftContents.size == 0) {
                visibleContents.add(EmptyContent())
            } else {
                validateLeftContents()
                visibleContents.add(leftContents.removeAt(0))
                cardStackAdapter.notifyItemInserted(visibleContents.size - 1)
            }

        }

        /**
         * 카드 드래그 취소할 때 callback. Threshold 넘지 않고 손 뗐을 때 invoke
         */
        override fun onCardCanceled() {
            when (val viewHolder = currentViewHolder) {
                is CardStackViewHolder -> viewHolder.setDefaultTransparency()
                is DailyQuestionViewHolder -> viewHolder.disableAll()
            }
        }

        /**
         * 카드가 rewind 됐을 떄 callback
         */
        override fun onCardRewound() {
            head--
            (currentViewHolder as? CardStackViewHolder)?.setDefaultTransparency()
        }

        private fun onCardContentSwiped(direction: Direction?, cardStackContent: CardStackContent) {
            swipedContents.add(cardStackContent)
            runOnBackground {
                // pick nope 여부 서버 업데이트
                val picked = direction == Direction.Right
                val isPicked = CommandProcess.pick(
                    userId = StaticComponent.user.userId,
                    opponentId = cardStackContent.userId,
                    isPick = picked
                ).result!!

                // 서로 pick했을 때 서버에 채팅 생성 요청
                if (picked && isPicked) {
                    CommandProcess.createChat(
                        userId = StaticComponent.user.userId,
                        userName = StaticComponent.user.name,
                        opponentId = cardStackContent.userId,
                        opponentName = cardStackContent.userName
                    )
                }

                if (!picked) {
                    rewindableContents.add(cardStackContent)
                }
            }
        }

        private fun onDailyQuestionSwiped(direction: Direction?, dailyQuestionContent: DailyQuestionContent) {
            runOnBackground {
                CommandProcess.answerQuestion(
                    userId = StaticComponent.user.userId,
                    questionId = dailyQuestionContent.questionId,
                    isPick = direction == Direction.Right
                )
            }
        }
    }
}