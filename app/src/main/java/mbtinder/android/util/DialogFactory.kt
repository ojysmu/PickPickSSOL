package mbtinder.android.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.LayoutRes
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import mbtinder.android.R

object DialogFactory {
    /**
     * string resource 제목 AlertDialog 생성
     * @param context: 생성 context
     * @param id: 제목 string id
     * @param onNeutral: 확인 버튼 클릭 이벤트
     */
    fun getTitledDialog(context: Context, @StringRes id: Int, onNeutral: () -> Unit): AlertDialog.Builder {
        return getTitledDialog(context, context.getString(id), onNeutral)
    }

    /**
     * string 제목 AlertDialog 생성
     * @param context: 생성 context
     * @param title: 제목 string
     * @param onNeutral: 확인 버튼 클릭 이벤트
     * @see AlertDialog.Builder.setNeutralButton
     */
    fun getTitledDialog(context: Context, title: String, onNeutral: () -> Unit): AlertDialog.Builder {
        return AlertDialog.Builder(context).apply {
            setCancelable(false)
            setTitle(title)
            setNeutralButton(R.string.common_confirm) { dialog, _ -> dialog.dismiss(); onNeutral() }
        }
    }

    /**
     * string resource 제목 AlertDialog 생성
     * @param context: 생성 context
     * @param id: 제목 string id
     * @param onPositive: 긍정 버튼 클릭 이벤트
     * @param onNegative: 부정 버튼 클릭 이벤트
     * @see AlertDialog.Builder.setPositiveButton
     * @see AlertDialog.Builder.setNegativeButton
     */
    fun getTitledDialog(context: Context, @StringRes id: Int, onPositive: () -> Unit, onNegative: () -> Unit): AlertDialog.Builder {
        return getTitledDialog(context, context.getString(id), onPositive, onNegative)
    }

    fun getTitledDialog(context: Context, title: String, onPositive: () -> Unit, onNegative: () -> Unit): AlertDialog.Builder {
        return AlertDialog.Builder(context).apply {
            setCancelable(false)
            setTitle(title)
            setPositiveButton(R.string.common_confirm) { dialog, _ -> dialog.dismiss(); onPositive() }
            setNegativeButton(R.string.common_cancel) { dialog, _ -> dialog.dismiss(); onNegative() }
        }
    }

    fun getContentedDialog(context: Context, @StringRes id: Int, onNeutral: () -> Unit): AlertDialog.Builder {
        return AlertDialog.Builder(context).apply {
            setCancelable(false)
            setMessage(id)
            setNeutralButton(R.string.common_confirm) { dialog, _ -> dialog.dismiss(); onNeutral() }
        }
    }

    fun getContentedDialog(context: Context, content: String, onNeutral: () -> Unit): AlertDialog.Builder {
        return AlertDialog.Builder(context).apply {
            setCancelable(false)
            setMessage(content)
            setNeutralButton(R.string.common_confirm) { dialog, _ -> dialog.dismiss(); onNeutral() }
        }
    }

    fun getContentedDialog(context: Context, @StringRes id: Int, onPositive: () -> Unit, onNegative: () -> Unit = {}): AlertDialog.Builder {
        return AlertDialog.Builder(context).apply {
            setCancelable(false)
            setMessage(id)
            setPositiveButton(R.string.common_confirm) { dialog, _ -> dialog.dismiss(); onPositive() }
            setNegativeButton(R.string.common_cancel) { dialog, _ -> dialog.dismiss(); onNegative() }
        }
    }

    fun getBasicDialog(context: Context, @StringRes title: Int, @StringRes message: Int, onNeutral: () -> Unit): AlertDialog.Builder {
        return getBasicDialog(context, title, context.getString(message), onNeutral)
    }

    fun getBasicDialog(context: Context, @StringRes title: Int, message: String, onNeutral: () -> Unit): AlertDialog.Builder {
        return AlertDialog.Builder(context).apply {
            setCancelable(false)
            setTitle(title)
            setMessage(message)
            setNeutralButton(R.string.common_confirm) { dialog, _ -> dialog.dismiss(); onNeutral() }
        }
    }

    fun getBasicDialog(context: Context, @StringRes title: Int, @StringRes message: Int, onPositive: () -> Unit, onNegative: () -> Unit): AlertDialog.Builder {
        return getBasicDialog(context, title, context.getString(message), onPositive, onNegative)
    }

    fun getBasicDialog(context: Context, @StringRes title: Int, message: String, onPositive: () -> Unit, onNegative: () -> Unit): AlertDialog.Builder {
        return AlertDialog.Builder(context).apply {
            setCancelable(false)
            setTitle(title)
            setMessage(message)
            setPositiveButton(R.string.common_confirm) { dialog, _ -> dialog.dismiss(); onPositive() }
            setNegativeButton(R.string.common_cancel) { dialog, _ -> dialog.dismiss(); onNegative() }
        }
    }

    fun getCustomDialog(context: Context, @LayoutRes id: Int, init: (ViewGroup) -> Unit,
                        onPositive: ((ViewGroup) -> Unit)?, onNegative: ((ViewGroup) -> Unit)? = {},
                        onNeutral: ((ViewGroup) -> Unit)? = null): AlertDialog.Builder {
        val viewGroup = LayoutInflater.from(context).inflate(id, null, false) as ViewGroup
        return AlertDialog.Builder(context).apply {
            setView(viewGroup)
            setCancelable(false)
            init(viewGroup)
            onPositive?.let { setPositiveButton(R.string.common_confirm) { dialog, _ -> dialog.dismiss(); onPositive(viewGroup) } }
            onNegative?.let { setNegativeButton(R.string.common_cancel) { dialog, _ -> dialog.dismiss(); onNegative(viewGroup) } }
            onNeutral?.let { setNeutralButton(R.string.common_confirm) { dialog, _ -> dialog.dismiss(); onNeutral(viewGroup) } }
        }
    }

    @SuppressLint("InflateParams")
    fun getWaitDialog(context: Context): AlertDialog {
        val viewGroup = LayoutInflater.from(context).inflate(R.layout.dialog_wait, null, false) as ViewGroup
        return AlertDialog.Builder(context).apply { setView(viewGroup); setCancelable(false) }.create()
    }

    @SuppressLint("InflateParams")
    fun getWaitDialog(context: Context, @StringRes title: Int): AlertDialog {
        val viewGroup = LayoutInflater.from(context).inflate(R.layout.dialog_wait, null, false) as ViewGroup
        viewGroup.findViewById<TextView>(R.id.dialog_wait_title).setText(title)

        return AlertDialog.Builder(context).apply { setView(viewGroup); setCancelable(false) }.create()
    }

    @SuppressLint("InflateParams")
    fun getWaitDialog(context: Context, title: String): AlertDialog {
        val viewGroup = LayoutInflater.from(context).inflate(R.layout.dialog_wait, null, false) as ViewGroup
        viewGroup.findViewById<TextView>(R.id.dialog_wait_title).text = title

        return AlertDialog.Builder(context).apply { setView(viewGroup); setCancelable(false) }.create()
    }
}