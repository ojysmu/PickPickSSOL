package mbtinder.android.ui.model

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputLayout
import mbtinder.android.util.ViewUtil

abstract class Fragment(@LayoutRes private val layout: Int) : Fragment(), View.OnFocusChangeListener {
    private val focusCount = HashMap<View, Int>()
    private val focusEvent = HashMap<View, () -> Unit>()

    protected lateinit var rootView: View

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) =
        inflateView(layout, inflater, container)

    protected fun inflateView(@LayoutRes layout: Int, inflater: LayoutInflater, container: ViewGroup?): View? {
        rootView = inflater.inflate(layout, container, false)
        return rootView
    }

    protected open fun <T : View> findViewById(@IdRes id: Int): T = rootView.findViewById(id)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) = initializeView()

    override fun onStop() {
        super.onStop()

        rootView.findFocus()?.let {
            val inputMethodManager = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(it.windowToken, 0)
        }
    }

    protected fun hideIme() {
        rootView.findFocus()?.let {
            val editText = it as EditText
            val inputMethodManager = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE)
                    as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    override fun onFocusChange(v: View, hasFocus: Boolean) {
        if (hasFocus) {
            focusCount[v] = focusCount.getOrDefault(v, 0) + 1
        } else {
            focusEvent[v]?.invoke()
        }
    }

    protected fun addFocusEvent(v: View, event: () -> Unit) {
        focusEvent[ViewUtil.filterEditText(v)] = event
    }

    protected fun getFocusCount(v: View): Int = focusCount.getOrDefault(ViewUtil.filterEditText(v), 0)

    protected fun initializeFocusableEditText(textInputLayout: TextInputLayout, afterTextChanged: (Editable?) -> Unit, leaveEvent: (() -> Unit)? = null) {
        textInputLayout.editText!!.onFocusChangeListener = this
        textInputLayout.editText!!.addTextChangedListener(afterTextChanged = afterTextChanged)
        if (leaveEvent != null) {
            addFocusEvent(textInputLayout, leaveEvent)
        }
    }

    protected abstract fun initializeView()

    protected fun finish() = requireActivity().finish()
}