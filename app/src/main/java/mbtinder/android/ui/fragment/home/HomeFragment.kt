package mbtinder.android.ui.fragment.home

import android.view.View
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
import mbtinder.android.util.SharedPreferencesUtil
import mbtinder.android.util.runOnBackground
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.card_stack.BaseCardStackContent
import mbtinder.lib.component.card_stack.DailyQuestionContent
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.util.findAll
import org.json.JSONObject
import java.sql.Date

class HomeFragment : Fragment(R.layout.fragment_home) {
    companion object {
        val cardStackContents = arrayListOf<BaseCardStackContent>()
    }

    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private val cardStackAdapter by lazy { CardStackAdapter(this) }
    private var currentPosition = 0
    private val todayQuestions by lazy { getNotAnsweredQuestion(getTodayDailyQuestions()) }

    override fun initializeView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE
        getCardStacks()
        getDailyQuestions()

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

        home_card_stack_view.layoutManager = cardStackLayoutManager
        home_card_stack_view.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
        home_card_stack_view.adapter = cardStackAdapter

        home_rewind.setOnClickListener {
            home_card_stack_view.rewind()
        }
    }

    /**
     * 카드 스택 최초 생성
     * 카드 스택이 존재하지 않고, 최초 로딩 시 호출
     */
    @AnyThread
    private fun getCardStacks() {
        runOnBackground {
            val getResult = CommandProcess.getMatchableUsers(
                userId = StaticComponent.user.userId,
                coordinator = Coordinator(StaticComponent.user.lastLocationLng, StaticComponent.user.lastLocationLat),
                searchFilter = StaticComponent.user.searchFilter
            )

            if (getResult.isSucceed) {
                val mapped = getResult.result!!
                    .mapTo(ArrayList<BaseCardStackContent>()) { it }
                    .apply { add(EmptyContent()) }
                cardStackContents.addAll(mapped)
                runOnUiThread {
                    cardStackAdapter.notifyDataSetChanged()
                    home_waiting.visibility = View.INVISIBLE
                    home_card_stack_view.visibility = View.VISIBLE
                }
            }
        }
    }

    /**
     * 카드 스택 업데이트
     * 이미 카드 스택이 존재하고, 추가로 필요할 떄 호출
     * 오늘의 질문이 남았을 경우 맨 뒤에 추가
     */
    @AnyThread
    private fun refreshCardStacks() {
        runOnBackground {
            val refreshResult = CommandProcess.refreshMatchableUsers(
                userId = StaticComponent.user.userId,
                coordinator = Coordinator(StaticComponent.user.lastLocationLng, StaticComponent.user.lastLocationLat),
                searchFilter = StaticComponent.user.searchFilter,
                currentMetList = cardStackAdapter.getUserIds()
            )

            if (refreshResult.isSucceed && refreshResult.result!!.isNotEmpty()) {
                cardStackContents.removeAll { it is EmptyContent }
                cardStackContents.addAll(refreshResult.result!!)
                if (todayQuestions.isNotEmpty()) {
                    cardStackContents.add(todayQuestions.removeAt(0))
                }
                cardStackContents.add(EmptyContent())
                runOnUiThread { cardStackAdapter.notifyDataSetChanged() }
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
                ?.date ?: Date(System.currentTimeMillis())

            val getResult = CommandProcess.getDailyQuestions(lastDate)
            if (getResult.isSucceed) {
                saved.addAll(getResult.result!!.map { it.toJSONObject().toString() })
                context.put("questions", saved)
            }
        }
    }

    /**
     * 인수로 전달된 질문 중 답변되지 않은 질문 반환. 반드시 background에서 실행되어야 함
     * @param questions: 답변되었는지 확인되지 않은 질문
     * @return 답변되지 않은 질문
     */
    @WorkerThread
    private fun getNotAnsweredQuestion(questions: List<DailyQuestionContent>) =
        questions.findAll {
            val isAnsweredResult = CommandProcess.isAnsweredQuestion(StaticComponent.user.userId, it.questionId)
            isAnsweredResult.isSucceed && !isAnsweredResult.result!!
        }

    /**
     * 로컬에서 오늘의 일일 질문 반환. [getDailyQuestions]이 같은 날 선행되었음이 보장되어야 함
     * @return 오늘의 일일 질문
     */
    @AnyThread
    private fun getTodayDailyQuestions(): List<DailyQuestionContent> {
        val context = SharedPreferencesUtil.getContext(requireContext(), SharedPreferencesUtil.PREF_QUESTIONS)
        val saved = context.getStringList("questions").map { DailyQuestionContent(JSONObject(it)) }
        return saved.findAll { it.date == Date(System.currentTimeMillis()) }
    }

    /**
     * 카드 이벤트 callback listener
     * onCardAppeared -> onCardDragging -> onCardDisappeared -> onCardSwiped
     */
    private val cardStackListener = object : CardStackListener {
        /**
         * 카드가 나타났을 때 callback
         * 일반 카드일 경우 swipe 가능, 일일 질문일 경우 swipe 가능, 목록의 끝일 경우 swipe 불가능
         */
        override fun onCardAppeared(view: View?, position: Int) {
            val holder = cardStackAdapter.cardStackViewHolders[position]

            cardStackLayoutManager.setCanScrollHorizontal(holder != null)
            currentPosition = position
        }

        /**
         * 카드 드래그할 때 callback
         * 일반 카드일 경우 배경 어둡게, PICK, NOPE 노출, 일일 질문일 경우 답변 highlight
         */
        override fun onCardDragging(direction: Direction?, ratio: Float) {
            cardStackAdapter.cardStackViewHolders[currentPosition]?.let {
                when {
                    ratio == 0.0f -> it.setDefaultTransparency()
                    direction == Direction.Left -> it.setNopeTransparency(ratio)
                    direction == Direction.Right -> it.setPickTransparency(ratio)
                }
            } ?: cardStackAdapter.dailyQuestionViewHolders[currentPosition]?.let {
                when {
                    ratio == 0.0f -> it.disableAll()
                    direction == Direction.Left -> it.enableNope()
                    direction == Direction.Right -> it.enablePick()
                }
            }
        }

        /**
         * 카드가 사라졌을 때 callback
         * 카드가 세장 남았을 경우 refresh
         */
        override fun onCardDisappeared(view: View?, position: Int) {
            val holder = cardStackAdapter.cardStackViewHolders[currentPosition]
            holder?.setDefaultTransparency()

            if (cardStackAdapter.getLeftContents(position) == 3) {
                refreshCardStacks()
            }
        }

        /**
         * 카드가 swipe 됐을 때 callback
         * 일반 카드일 경우 PICK, NOPE 여부 서버 전송, PICK일 경우 목록에서 삭제(rewind 방지)
         * 일일 질문일 경우 PICK, NOPE 여부 서버 전송, 목록에서 삭제
         */
        override fun onCardSwiped(direction: Direction?) {
            val currentPosition = currentPosition
            val viewType = cardStackAdapter.getItemViewType(currentPosition)
            if (viewType == CardStackAdapter.TYPE_CARD_STACK_CONTENT) {
                runOnBackground {
                    // pick nope 여부 서버 업데이트
                    val opponentId = cardStackAdapter.getUserId(currentPosition)
                    val isPicked = CommandProcess.pick(
                        userId = StaticComponent.user.userId,
                        opponentId = opponentId,
                        isPick = direction == Direction.Right
                    ).result!!

                    // 서로 pick했을 때 서버에 채팅 생성 요청
                    if (isPicked) {
                        CommandProcess.createChat(StaticComponent.user.userId, opponentId)
                    }

                    // nope된 사용자는 pool에 추가
                    if (direction == Direction.Right) {
                        runOnUiThread { cardStackAdapter.removeAt(currentPosition) }
                    }
                }
            } else if (viewType == CardStackAdapter.TYPE_DAILY_QUESTION_CONTENT) {
                runOnBackground {
                    val questionContent = cardStackContents[currentPosition] as DailyQuestionContent

                }
            }
        }

        /**
         * 카드 드래그 취소할 때 callback. Threshold 넘지 않고 손 뗐을 때 invoke
         */
        override fun onCardCanceled() {
            val holder = cardStackAdapter.cardStackViewHolders[currentPosition]
            holder?.setDefaultTransparency()
        }

        /**
         * 카드가 rewind 됐을 떄 callback
         */
        override fun onCardRewound() {
            val holder = cardStackAdapter.cardStackViewHolders[currentPosition]
            holder?.setDefaultTransparency()
        }
    }
}