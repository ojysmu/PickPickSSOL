package mbtinder.android.util

import android.content.Context
import android.content.Intent
import android.provider.MediaStore
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.view.children
import com.google.android.material.slider.RangeSlider
import com.google.android.material.textfield.TextInputLayout
import mbtinder.android.R
import mbtinder.android.ui.model.Fragment

object ViewUtil {
    private fun enableRecursively(viewGroup: ViewGroup): Unit = viewGroup.children.forEach {
        it.isEnabled = true
        if (it is ViewGroup) {
            enableRecursively(it)
        }
    }

    private fun disableRecursively(viewGroup: ViewGroup): Unit = viewGroup.children.forEach {
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

    fun filterEditText(view: View) = if (view is TextInputLayout) {
        view.editText!!
    } else {
        view
    }

    fun callAlbum(fragment: Fragment, resultCode: Int) {
        Intent(Intent.ACTION_PICK).apply {
            type = MediaStore.Images.Media.CONTENT_TYPE
            fragment.startActivityForResult(this, resultCode)
        }
    }

    fun dp2px(context: Context, dp: Int) = (dp * context.resources.displayMetrics.density + 0.5f)
}

fun RangeSlider.getStart() = values[0].toInt()

fun RangeSlider.getEnd() = values[1].toInt()

fun TextInputLayout.getText() = editText!!.text.toString()

fun TextInputLayout.isSame(textInputLayout: TextInputLayout) = getText() == textInputLayout.getText()