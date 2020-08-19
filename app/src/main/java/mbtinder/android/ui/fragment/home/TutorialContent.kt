package mbtinder.android.ui.fragment.home

import mbtinder.android.ui.model.recycler_view.MultiTypeContent
import mbtinder.lib.component.card_stack.BaseCardStackContent
import java.util.*

class TutorialContent : BaseCardStackContent, MultiTypeContent {
    private val uuid = UUID.randomUUID()

    override fun getUUID() = this.uuid!!

    override fun getId() = uuid.leastSignificantBits + uuid.mostSignificantBits
}