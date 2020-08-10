package mbtinder.android.ui.fragment.home

import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.ui.view.AsyncImageView
import mbtinder.android.util.ImageUtil
import mbtinder.android.util.ViewUtil
import mbtinder.lib.component.CardStackContent

class CardStackViewHolder(private val view: View): AdaptableViewHolder<CardStackContent>(view) {
    private var imageView: AsyncImageView = view.findViewById(R.id.card_main_stack_image)
    private var recyclerView: RecyclerView = view.findViewById(R.id.card_main_stack_recycler_view)
    private var pickContainer: FrameLayout = view.findViewById(R.id.card_main_stack_pick_container)
    private var cardPick: MaterialCardView = view.findViewById(R.id.card_main_stack_pick)
    private var cardPickContent: TextView = view.findViewById(R.id.card_main_stack_pick_content)
    private var nopeContainer: FrameLayout = view.findViewById(R.id.card_main_stack_nope_container)
    private var cardNope: MaterialCardView = view.findViewById(R.id.card_main_stack_nope)
    private var cardNopeContent: TextView = view.findViewById(R.id.card_main_stack_nope_content)
    private var emptyTextView: TextView = view.findViewById(R.id.card_main_stack_empty)
    private var refreshButton: Button = view.findViewById(R.id.card_main_stack_refresh)
    private var refreshingProgressBar: ProgressBar = view.findViewById(R.id.card_main_stack_refreshing)

    lateinit var adapter: CardStackAdapter
    var isEmpty = false

    fun setPickTransparency(ratio: Float) {
        pickContainer.setBackgroundColor(ViewUtil.getColor(view.context.getColor(android.R.color.black), 1.0f - ratio / 2))
        cardPick.strokeColor = ViewUtil.getColor(view.context.getColor(R.color.colorPrimary), 1.0f - ratio)
        cardPickContent.setTextColor(ViewUtil.getColor(view.context.getColor(R.color.colorPrimary), 1.0f - ratio))
    }

    fun setNopeTransparency(ratio: Float) {
        nopeContainer.setBackgroundColor(ViewUtil.getColor(view.context.getColor(android.R.color.black), 1.0f - ratio / 2))
        cardNope.strokeColor = ViewUtil.getColor(view.context.getColor(android.R.color.black), 1.0f - ratio)
        cardNopeContent.setTextColor(ViewUtil.getColor(view.context.getColor(android.R.color.black), 1.0f - ratio))
    }

    override fun adapt(content: CardStackContent) {
        isEmpty = content.isEmptyBody

        if (content.isEmptyBody) {
            adaptEmpty()
        } else {
            adaptContent(content)
        }
    }

    private fun adaptContent(content: CardStackContent) {
        emptyTextView.visibility = View.INVISIBLE
        refreshButton.visibility = View.INVISIBLE
        refreshingProgressBar.visibility = View.INVISIBLE
        imageView.visibility = View.VISIBLE
        recyclerView.visibility = View.VISIBLE
        cardPick.visibility = View.VISIBLE
        cardNope.visibility = View.VISIBLE

//        imageView.setImageDrawable(ImageUtil.byteArrayToDrawable(itemView.context, content.getImage()))
        imageView.setImage(content)
        recyclerView.itemAnimator = DefaultItemAnimator()
        recyclerView.layoutManager = LinearLayoutManager(itemView.context)
        recyclerView.adapter = CardStackContentAdapter(content.contents.toMutableList())
        setPickTransparency(0.0f)
        setNopeTransparency(0.0f)
    }

    private fun adaptEmpty() {
        emptyTextView.visibility = View.VISIBLE
        refreshButton.visibility = View.VISIBLE
        refreshingProgressBar.visibility = View.INVISIBLE
        imageView.visibility = View.INVISIBLE
        recyclerView.visibility = View.INVISIBLE
        cardPick.visibility = View.INVISIBLE
        cardNope.visibility = View.INVISIBLE

        refreshButton.setOnClickListener {
            refreshButton.visibility = View.INVISIBLE
            refreshingProgressBar.visibility = View.VISIBLE
            adapter.requestUpdate(this::onUpdateFinished)
        }
    }

    private fun onUpdateFinished(isSucceed: Boolean, content: CardStackContent?) {
        if (isSucceed) {
            adaptContent(content!!)
        } else {
            refreshButton.visibility = View.VISIBLE
            refreshingProgressBar.visibility = View.INVISIBLE
        }
    }
}