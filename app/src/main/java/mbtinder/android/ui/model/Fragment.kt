package mbtinder.android.ui.model

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.fragment.app.Fragment

abstract class Fragment : Fragment() {
    protected lateinit var rootView: View

    protected fun hideIme() {
        rootView.findFocus()?.let {
            val editText = it as EditText
            val inputMethodManager = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    protected open fun <T : View> findViewById(@IdRes id: Int): T {
        return rootView.findViewById(id)
    }

    override fun onStop() {
        super.onStop()

        rootView.findFocus()?.let {
            val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    protected fun inflateView(@LayoutRes layout: Int, inflater: LayoutInflater, container: ViewGroup?): View? {
        rootView = inflater.inflate(layout, container, false)
        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        initializeView()
    }

    protected abstract fun initializeView()

    protected fun finish() {
        requireActivity().finish()
    }
}