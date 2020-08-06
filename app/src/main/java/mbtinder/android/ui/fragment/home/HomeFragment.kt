package mbtinder.android.ui.fragment.home

import android.view.View
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.fragment_home.*
import mbtinder.android.R
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.ImageUtil
import mbtinder.android.util.Log
import mbtinder.lib.component.CardStackContent
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {
    private val sampleContents = arrayListOf<CardStackContent>()

    private lateinit var cardStackLayoutManager: CardStackLayoutManager
    private val cardStackAdapter by lazy { CardStackAdapter(sampleContents, this) }
    private var currentPosition = 0

    override fun initializeView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.VISIBLE

        for (i in 0 until 3) {
            val content = CardStackContent(UUID.randomUUID(), listOf("내용1", "내용2"), "name", "url")
            content.setImage(ImageUtil.drawableToByteArray(requireContext().getDrawable(R.drawable.image)!!))
            sampleContents.add(content)
        }

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

    fun updateCardStack() {

    }

    private val cardStackListener = object : CardStackListener {
        override fun onCardDisappeared(view: View?, position: Int) {
            val holder = cardStackAdapter.holders[currentPosition]
            holder.setNopeTransparency(0.0f)
            holder.setPickTransparency(0.0f)

            if (position == cardStackAdapter.itemCount - 2) {
                Log.v("onCardDisappeared() Empty: position=$position")
                cardStackAdapter.addContent(CardStackContent())
            }
        }

        override fun onCardDragging(direction: Direction?, ratio: Float) {
            val holder = cardStackAdapter.holders[currentPosition]
            if (!holder.isEmpty) {
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
        }

        override fun onCardCanceled() {
            val holder = cardStackAdapter.holders[currentPosition]
            if (!holder.isEmpty) {
                holder.setNopeTransparency(0.0f)
                holder.setPickTransparency(0.0f)
            }
        }

        override fun onCardAppeared(view: View?, position: Int) {
            currentPosition = position
            if (cardStackAdapter.holders[currentPosition].isEmpty) {
                cardStackLayoutManager.setCanScrollHorizontal(false)
            }
        }

        override fun onCardRewound() {
            val holder = cardStackAdapter.holders[currentPosition]
            holder.setNopeTransparency(0.0f)
            holder.setPickTransparency(0.0f)
            cardStackLayoutManager.setCanScrollHorizontal(true)
        }
    }
}