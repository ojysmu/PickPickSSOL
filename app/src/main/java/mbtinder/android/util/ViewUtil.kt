package mbtinder.android.util

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.view.children
import com.google.android.material.textfield.TextInputLayout
import mbtinder.android.R

object ViewUtil {
    fun getText(editText: EditText) = editText.text.toString()

    fun getText(textInputLayout: TextInputLayout) = getText(textInputLayout.editText!!)

    fun hasSameText(textInputLayout1: TextInputLayout, textInputLayout2: TextInputLayout) =
        getText(textInputLayout1) == getText(textInputLayout2)

    fun enableRecursively(viewGroup: ViewGroup): Unit = viewGroup.children.forEach {
        it.isEnabled = true
        if (it is ViewGroup) {
            enableRecursively(it)
        }
    }

    fun disableRecursively(viewGroup: ViewGroup): Unit = viewGroup.children.forEach {
        it.isEnabled = false
        if (it is ViewGroup) {
            disableRecursively(it)
        }
    }

    fun switchNextButton(viewGroup: ViewGroup) {
        val button = viewGroup.findViewById<Button>(R.id.switchable_next)
        val progressBar = viewGroup.findViewById<ProgressBar>(R.id.switchable_waiting)

        if (button.visibility == View.VISIBLE) {
            button.visibility = View.INVISIBLE
            progressBar.visibility = View.VISIBLE
            disableRecursively(viewGroup)
        } else {
            button.visibility = View.VISIBLE
            progressBar.visibility = View.INVISIBLE
            enableRecursively(viewGroup)
        }
    }
}