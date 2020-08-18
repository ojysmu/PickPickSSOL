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
import mbtinder.android.util.runOnBackground
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.card_stack.CardStackContent
import mbtinder.lib.component.user.Coordinator

class HomeFragment : Fragment(R.layout.fragment_home) {
    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private val cardStackAdapter by lazy { CardStackAdapter(arrayListOf(), this) }
    private var currentPosition = 0

    override fun initializeView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE
        updateCardStack()

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
        home_card_stack_view.adapter = cardStackAdapter
        home_card_stack_view.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }

        home_rewind.setOnClickListener {
            home_card_stack_view.rewind()
        }
    }

    private fun updateCardStack() {
        runOnBackground {
            val getResult = CommandProcess.getMatchableUsers(
                userId = StaticComponent.user.userId,
                coordinator = Coordinator(StaticComponent.user.lastLocationLng, StaticComponent.user.lastLocationLat),
                searchFilter = StaticComponent.user.searchFilter
            )

            if (getResult.isSucceed) {
                val contents = getResult.result!!

                runOnUiThread {
                    cardStackAdapter.addContents(contents)
                    home_waiting.visibility = View.INVISIBLE
                    home_card_stack_view.visibility = View.VISIBLE
                }
            }
        }
    }

    private val cardStackListener = object : CardStackListener {
        override fun onCardDisappeared(view: View?, position: Int) {
            val holder = cardStackAdapter.holders[currentPosition]
            if (holder is CardStackViewHolder) {
                holder.setNopeTransparency(0.0f)
                holder.setPickTransparency(0.0f)
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

        override fun onCardSwiped(direction: Direction?) {
            runOnBackground {
                val content = cardStackAdapter.contents[currentPosition]
                if (content is CardStackContent) {
                    // pick된 사용자는 rewind 되지 않도록 함
                    if (direction == Direction.Right) {
                        runOnUiThread {
                            cardStackAdapter.removeContent(currentPosition)
                        }
                    }

                    // pick nope 여부 서버 업데이트
                    val opponentId = content.userId
                    val isPicked = CommandProcess.pick(
                        userId = StaticComponent.user.userId,
                        opponentId = opponentId,
                        isPick = direction == Direction.Right
                    ).result!!

                    // 서로 pick했을 때 서버에 채팅 생성 요청
                    if (isPicked) {
                        CommandProcess.createChat(StaticComponent.user.userId, opponentId)
                    }
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

        override fun onCardAppeared(view: View?, position: Int) {
            currentPosition = position

            cardStackLayoutManager.setCanScrollHorizontal(cardStackAdapter.holders[currentPosition] !is EmptyViewHolder)
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