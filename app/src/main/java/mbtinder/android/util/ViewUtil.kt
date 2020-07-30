package mbtinder.android.util

import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.annotation.ColorRes
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

    fun getColor(color: Int, transparency: Float): Int {
        val b: Int = color and 0xFF
        val g: Int = color shr 8 and 0xFF
        val r: Int = color shr 16 and 0xFF
        val a: Int = (255 * (1.0f - transparency)).toInt()

        return a and 0xff shl 24 or (r and 0xff shl 16) or (g and 0xff shl 8) or (b and 0xff)
    }
}