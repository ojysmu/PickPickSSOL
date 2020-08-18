package mbtinder.android.ui.fragment.home

import android.view.View
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.fragment_home.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.Log
import mbtinder.android.util.runOnBackground
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.card_stack.BaseCardStackContent
import mbtinder.lib.component.card_stack.CardStackContent
import mbtinder.lib.component.user.Coordinator

class HomeFragment : Fragment(R.layout.fragment_home) {
    companion object {
        val cardStackContents = arrayListOf<BaseCardStackContent>()
        private val nopeContents = arrayListOf<CardStackContent>()
    }

    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private val cardStackAdapter by lazy { CardStackAdapter(this) }
    private var currentPosition = 0

    override fun initializeView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE
        getCardStacks()

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
            if (nopeContents.isNotEmpty()) {
                cardStackAdapter.addAt(0, nopeContents.removeAt(nopeContents.size - 1))
                home_card_stack_view.smoothScrollToPosition(currentPosition - 1)
            }
        }
    }

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

    private fun refreshCardStacks() {
        runOnBackground {
            val refreshResult = CommandProcess.refreshMatchableUsers(
                userId = StaticComponent.user.userId,
                coordinator = Coordinator(StaticComponent.user.lastLocationLng, StaticComponent.user.lastLocationLat),
                searchFilter = StaticComponent.user.searchFilter,
                currentMetList = cardStackAdapter.getUserIds()
            )

            if (refreshResult.isSucceed) {
                cardStackContents.removeAt(cardStackContents.size - 1)
                cardStackContents.addAll(refreshResult.result!!)
                cardStackContents.add(EmptyContent())
                runOnUiThread { cardStackAdapter.notifyDataSetChanged() }
            }
        }
    }

    private val cardStackListener = object : CardStackListener {
        override fun onCardAppeared(view: View?, position: Int) {
            val holder = cardStackAdapter.holders[position]
            Log.v("HomeFragment.onCardAppeared(): position=$position, isEmpty=${holder is EmptyViewHolder}, currentPosition=$currentPosition")
            currentPosition = position
            // 마지막(EmptyContent)일 때 스크롤 불가능
            cardStackLayoutManager.setCanScrollHorizontal(cardStackAdapter.holders[currentPosition] !is EmptyViewHolder)
            // 3개 남았을 때 refresh
            if (cardStackAdapter.getLeftContents(position) == 3) {
                refreshCardStacks()
            }
        }

        override fun onCardDragging(direction: Direction?, ratio: Float) {
            val holder = cardStackAdapter.holders[currentPosition]
            if (holder is CardStackViewHolder) {
                if (ratio == 0.0f) {
                    holder.setNopeTransparency(0.0f)
                    holder.setPickTransparency(0.0f)
                }
                if (direction == Direction.Left) {
                    holder.setNopeTransparency(ratio)
                } else {
                    holder.setPickTransparency(ratio)
                }
            }
        }

        override fun onCardDisappeared(view: View?, position: Int) {
            val holder = cardStackAdapter.holders[currentPosition]
            if (holder is CardStackViewHolder) {
                holder.setNopeTransparency(0.0f)
                holder.setPickTransparency(0.0f)
            }
        }

        override fun onCardSwiped(direction: Direction?) {
            if (cardStackAdapter.getItemViewType(cardStackLayoutManager.topPosition) == CardStackAdapter.TYPE_CARD_STACK_CONTENT) {
                runOnBackground {
                    // pick nope 여부 서버 업데이트
                    val opponentId = cardStackAdapter.getUserId(cardStackLayoutManager.topPosition)
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
                    if (direction == Direction.Left) {
                        nopeContents.add(cardStackContents[cardStackLayoutManager.topPosition] as CardStackContent)
                    }

                    runOnUiThread { cardStackAdapter.removeAt(cardStackLayoutManager.topPosition) }
                }
            }
        }

        override fun onCardCanceled() {
            val holder = cardStackAdapter.holders[currentPosition]
            if (holder is CardStackViewHolder) {
                holder.setNopeTransparency(0.0f)
                holder.setPickTransparency(0.0f)
            }
        }

        override fun onCardRewound() {
            val holder = cardStackAdapter.holders[currentPosition]
            if (holder is CardStackViewHolder) {
                holder.setNopeTransparency(0.0f)
                holder.setPickTransparency(0.0f)
            }
        }
    }
}