package mbtinder.android.util

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import mbtinder.android.R

object DialogFactory {
    fun getContentedDialog(context: Context, @StringRes id: Int, onNeutral: () -> Unit) =
        AlertDialog.Builder(context).apply {
            setCancelable(false)
            setMessage(id)
            setNeutralButton(R.string.common_confirm) { dialog, _ -> dialog.dismiss(); onNeutral() }
        }

    fun getContentedDialog(context: Context, @StringRes id: Int, onPositive: () -> Unit, onNegative: () -> Unit = {}) =
        AlertDialog.Builder(context).apply {
            setCancelable(false)
            setMessage(id)
            setPositiveButton(R.string.common_confirm) { dialog, _ -> dialog.dismiss(); onPositive() }
            setNegativeButton(R.string.common_cancel) { dialog, _ -> dialog.dismiss(); onNegative() }
        }

    @SuppressLint("InflateParams")
    fun getWaitDialog(context: Context) = AlertDialog.Builder(context).apply {
        setView(LayoutInflater.from(context).inflate(R.layout.dialog_wait, null, false) as ViewGroup);
        setCancelable(false)
    }.create()
}