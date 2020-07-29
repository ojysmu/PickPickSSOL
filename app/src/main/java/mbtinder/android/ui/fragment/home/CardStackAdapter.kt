package mbtinder.android.ui.fragment.home

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import mbtinder.android.R
import mbtinder.android.ui.model.Adapter
import mbtinder.android.ui.view.AsyncImageView
import mbtinder.android.util.ImageUtil
import mbtinder.lib.component.CardStackContent

class CardStackViewHolder(view: View): RecyclerView.ViewHolder(view) {
    var imageView: AsyncImageView = view.findViewById(R.id.card_main_stack_image)
    var nameTextView: TextView = view.findViewById(R.id.card_main_stack_name)
    var ageTextView: TextView = view.findViewById(R.id.card_main_stack_age)
    var contentTextView: TextView = view.findViewById(R.id.card_main_stack_content)
}

class CardStackAdapter(private val contents: MutableList<CardStackContent>): Adapter<CardStackContent>(
    R.layout.card_main_stack,
    contents,
    CardStackViewHolder::class.java) {

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder !is CardStackViewHolder) {
            error("Assertion failed")
        }

        val content = contents[position]
        holder.imageView.setImageDrawable(ImageUtil.byteArrayToDrawable(context, content.getImage()))
        holder.nameTextView.text = content.name
        holder.ageTextView.text = content.age.toString()
        holder.contentTextView.text = content.content
    }
}