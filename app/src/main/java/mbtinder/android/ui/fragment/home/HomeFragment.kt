package mbtinder.android.ui.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.DecelerateInterpolator
import android.view.animation.LinearInterpolator
import androidx.recyclerview.widget.DefaultItemAnimator
import com.yuyakaido.android.cardstackview.*
import kotlinx.android.synthetic.main.fragment_home.*
import mbtinder.android.R
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.ImageUtil
import mbtinder.lib.component.CardStackContent
import java.util.*

class HomeFragment : Fragment() {
    private val cardStackLayoutManager by lazy { CardStackLayoutManager(requireContext()) }

    override fun initializeView() {
        val contents = arrayListOf<CardStackContent>()
        for (i in 0 until 10) {
            val content = CardStackContent(UUID.randomUUID(), "이름$i", Random().nextInt(20) + 20, "내용 라인1\n내용 라인2\n내용 라인3", "name", "url")
            content.setImage(ImageUtil.drawableToByteArray(requireContext().getDrawable(R.drawable.image)!!))
            contents.add(content)
        }

        cardStackLayoutManager.setStackFrom(StackFrom.None)
        cardStackLayoutManager.setVisibleCount(3)
        cardStackLayoutManager.setTranslationInterval(8.0f)
        cardStackLayoutManager.setScaleInterval(0.95f)
        cardStackLayoutManager.setSwipeThreshold(0.3f)
        cardStackLayoutManager.setMaxDegree(20.0f)
        cardStackLayoutManager.setDirections(Direction.HORIZONTAL)
        cardStackLayoutManager.setCanScrollHorizontal(true)
        cardStackLayoutManager.setCanScrollVertical(true)
        cardStackLayoutManager.setSwipeableMethod(SwipeableMethod.AutomaticAndManual)
        cardStackLayoutManager.setOverlayInterpolator(LinearInterpolator())

        main_card_stack_view.layoutManager = cardStackLayoutManager
        main_card_stack_view.adapter =
            CardStackAdapter(contents)
        main_card_stack_view.itemAnimator.apply {
            if (this is DefaultItemAnimator) {
                supportsChangeAnimations = false
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.fragment_home, inflater, container)
    }
}