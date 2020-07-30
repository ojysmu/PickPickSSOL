package mbtinder.android.ui.model

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import mbtinder.android.R
import mbtinder.android.util.ViewUtil

open class ProgressFragment : Fragment() {
    private val formStatus = HashMap<View, Boolean>()

    private lateinit var nextButton: Button
    private lateinit var waitingProgressBar: ProgressBar

    override fun initializeView() {
        nextButton = findViewById(R.id.switchable_next)
        waitingProgressBar = findViewById(R.id.switchable_waiting)
    }

    protected fun enableNextButton() {
        nextButton.isEnabled = !formStatus.containsValue(false)
    }

    protected fun switchWaitingStatus() {
        ViewUtil.switchNextButton(rootView as ViewGroup)
    }

    protected fun setFormStatus(view: View, status: Boolean) {
        formStatus[view] = status
    }
}