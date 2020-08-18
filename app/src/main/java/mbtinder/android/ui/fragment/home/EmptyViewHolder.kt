package mbtinder.android.ui.fragment.home

import android.view.View
import android.widget.Button
import androidx.navigation.fragment.findNavController
import mbtinder.android.R
import mbtinder.lib.component.card_stack.BaseCardStackContent

class EmptyViewHolder(itemView: View, private val fragment: HomeFragment) : BaseCardStackViewHolder(itemView) {
    private val goAccountButton: Button = itemView.findViewById(R.id.card_home_empty_go_account)

    override fun adapt(content: BaseCardStackContent) {
        goAccountButton.setOnClickListener {
            fragment.findNavController().navigate(R.id.action_to_account)
        }
    }
}