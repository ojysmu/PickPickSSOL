package mbtinder.android.ui.fragment.sign_up

import android.text.Editable
import android.util.Patterns
import androidx.annotation.StringRes
import kotlinx.android.synthetic.main.fragment_sign_up1.*
import mbtinder.android.R
import mbtinder.android.io.SocketUtil
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.FormStateChecker
import mbtinder.android.util.ThreadUtil
import mbtinder.android.util.ViewUtil

class SignUp1Fragment : Fragment(R.layout.fragment_sign_up1) {
    private val formStateChecker = FormStateChecker()

    override fun initializeView() {
        formStateChecker.addViews(sign_up1_email, sign_up1_password, sign_up1_password_repeat)

        initializeFocusableEditText(sign_up1_email, this::onEmailChanged, this::onLeaveEmail)
        initializeFocusableEditText(sign_up1_password, this::onPasswordChanged, this::onLeavePassword)
        initializeFocusableEditText(sign_up1_password_repeat, this::onPasswordRepeatChanged, this::onLeavePasswordRepeat)
    }

    private fun enableNextButton() {
        sign_up1_next.isEnabled = !formStateChecker.hasFalse()
    }

    private fun onEmailChanged(editable: Editable?) {
        editable?.let {
            if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                formStateChecker.setState(sign_up1_email, true)
                sign_up1_email.isErrorEnabled = false
            } else if (getFocusCount(sign_up1_email) != 1) {
                onEmailIssued(R.string.sign_up_email_error)
            }
            enableNextButton()
        }
    }

    private fun onLeaveEmail() {
        val email = ViewUtil.getText(sign_up1_email)

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onEmailIssued(R.string.sign_up_email_error)
        } else {
            ThreadUtil.runOnBackground {
                if (!SocketUtil.checkEmailDuplicated(email).isSucceed) {
                    ThreadUtil.runOnUiThread { onEmailIssued(R.string.sign_up_email_duplicated) }
                }
            }
        }
    }

    private fun onEmailIssued(@StringRes error: Int) {
        formStateChecker.setState(sign_up1_email, false)
        sign_up1_email.isErrorEnabled = true
        sign_up1_email.error = getString(error)
    }

    private fun onPasswordChanged(editable: Editable?) {
        editable?.let {
            if (it.length in 8..16) {
                formStateChecker.setState(sign_up1_password, true)
                sign_up1_password.isErrorEnabled = false
            } else if (getFocusCount(sign_up1_password) != 1) {
                onPasswordIssued()
            }
            onPasswordRepeatChanged(it)
        }
    }

    private fun onLeavePassword() {
        if (ViewUtil.getText(sign_up1_password).length !in 8..16) {
            onPasswordIssued()
        }
    }

    private fun onPasswordIssued() {
        formStateChecker.setState(sign_up1_password, false)
        sign_up1_password.isErrorEnabled = true
        sign_up1_password.error = getString(R.string.sign_up_password_error)
    }

    private fun onPasswordRepeatChanged(editable: Editable?) {
        val focusCount = getFocusCount(sign_up1_password_repeat)

        if (ViewUtil.hasSameText(sign_up1_password, sign_up1_password_repeat)) {
            formStateChecker.setState(sign_up1_password_repeat, true)
            sign_up1_password_repeat.isErrorEnabled = false
        } else if (focusCount > 1) {
            onPasswordRepeatIssued()
        }
        enableNextButton()
    }

    private fun onLeavePasswordRepeat() {
        if (!ViewUtil.hasSameText(sign_up1_password, sign_up1_password_repeat)) {
            onPasswordRepeatIssued()
        }
    }

    private fun onPasswordRepeatIssued() {
        formStateChecker.setState(sign_up1_password_repeat, false)
        sign_up1_password_repeat.isErrorEnabled = true
        sign_up1_password_repeat.error = getString(R.string.sign_up_password_repeat_error)
    }
}