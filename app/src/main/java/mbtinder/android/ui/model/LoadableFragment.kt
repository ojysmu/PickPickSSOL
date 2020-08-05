package mbtinder.android.ui.model

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.fragment_loadable.*
import mbtinder.android.R
import mbtinder.android.util.ThreadUtil

abstract class LoadableFragment(@LayoutRes private val layout: Int): Fragment(layout) {
    private lateinit var innerLayout: ViewGroup

    override fun initializeView() {
        initializeOnUiThread()
        ThreadUtil.runOnBackground {
            isBackgroundLoading = true
            initializeOnBackground()
            isBackgroundLoading = false

            ThreadUtil.runOnUiThread {
                attachToRoot()
                loadable_progress_bar.visibility = View.GONE
                innerLayout.visibility = View.VISIBLE
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.fragment_loadable, inflater, container!!)
    }

    override fun <T : View> findViewById(@IdRes id: Int): T {
        return innerLayout.findViewById(id)
    }

    protected fun inflateRootView(@LayoutRes layout: Int): ViewGroup {
        val inflated = LayoutInflater.from(requireContext()).inflate(layout, rootView as ConstraintLayout, false) as ViewGroup
        innerLayout = inflated

        return inflated
    }

    private fun attachToRoot() {
        (rootView as ConstraintLayout).addView(innerLayout)
    }

    abstract fun initializeOnUiThread()

    abstract fun initializeOnBackground()

    companion object {
        var isBackgroundLoading = false
    }
}