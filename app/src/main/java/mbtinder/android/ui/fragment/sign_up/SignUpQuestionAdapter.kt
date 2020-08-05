package mbtinder.android.ui.fragment.sign_up

import mbtinder.android.R
import mbtinder.android.ui.model.recycler_view.AdaptableViewHolder
import mbtinder.android.ui.model.recycler_view.Adapter
import mbtinder.lib.component.SignUpQuestionContent

class SignUpQuestionAdapter(private val contents: MutableList<SignUpQuestionContent>)
    : Adapter<SignUpQuestionContent>(R.layout.card_sign_up4_questions, contents, SignUpQuestionViewHolder::class.java) {

    override fun onBindViewHolder(holder: AdaptableViewHolder<SignUpQuestionContent>, position: Int)
            = holder.adapt(contents[position])
}