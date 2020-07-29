package mbtinder.android.util

import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout

object ViewUtil {
    fun getText(editText: EditText) = editText.text.toString()

    fun getText(textInputLayout: TextInputLayout) = getText(textInputLayout.editText!!)

    fun hasSameText(textInputLayout1: TextInputLayout, textInputLayout2: TextInputLayout) =
        getText(textInputLayout1) == getText(textInputLayout2)
}