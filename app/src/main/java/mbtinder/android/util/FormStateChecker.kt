package mbtinder.android.util

import android.view.View

class FormStateChecker {
    private val formStatus = HashMap<View, Boolean>()

    fun addViews(vararg views: View) = views.forEach {
        formStatus[ViewUtil.filterEditText(it)] = false
    }

    fun getState(view: View): Boolean = formStatus.getOrDefault(view, false)

    fun setState(view: View, state: Boolean) {
        formStatus[view] = state
    }

    fun hasFalse() = formStatus.containsValue(false)
}