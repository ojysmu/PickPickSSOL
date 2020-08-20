package mbtinder.android.ui.fragment.home

import mbtinder.lib.component.card_stack.BaseCardStackContent
import java.util.*

class EmptyContent : BaseCardStackContent {
    private val uuid = UUID.randomUUID()

    override fun getUUID() = this.uuid!!
}