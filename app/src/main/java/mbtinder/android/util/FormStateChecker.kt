package mbtinder.android.util

import android.view.View

class FormStateChecker(vararg views: View) {
    private val formStatus = HashMap<View, Boolean>()

    init {
        views.forEach {
            formStatus[ViewUtil.filterEditText(it)] = false
        }
    }

    fun addViews(vararg views: View) = views.forEach {
        formStatus[ViewUtil.filterEditText(it)] = false
    }

    fun setState(view: View, state: Boolean) {
        formStatus[ViewUtil.filterEditText(view)] = state
    }

    fun hasFalse() = formStatus.containsValue(false)
}