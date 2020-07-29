package mbtinder.android.util

import android.view.ViewGroup
import android.widget.EditText
import androidx.core.view.children
import com.google.android.material.textfield.TextInputLayout

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
}