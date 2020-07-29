package mbtinder.android.ui.model

import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

open class Activity : AppCompatActivity() {
    protected val safeActionBar
        get() = if (supportActionBar == null) {
            throw AssertionError()
        } else {
            supportActionBar!!
        }

    protected fun hideIme() {
        window.decorView.rootView.findFocus()?.let {
            val editText = it as EditText
            val inputMethodManager = editText.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }
}