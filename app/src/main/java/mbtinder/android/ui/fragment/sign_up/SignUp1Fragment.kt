package mbtinder.android.ui.fragment.sign_up

import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_up1.*
import mbtinder.android.R
import mbtinder.android.io.socket.CommandProcess
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

        sign_up1_next.setOnClickListener {
            val arguments = Bundle()
            arguments.putString("email", ViewUtil.getText(sign_up1_email))
            arguments.putString("password", ViewUtil.getText(sign_up1_password))

            findNavController().navigate(R.id.action_to_sign_up2, arguments)
        }
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
                onInputIssued(sign_up1_email, R.string.sign_up1_email_error, formStateChecker)
            }
            enableNextButton()
        }
    }

    private fun onLeaveEmail() {
        val email = ViewUtil.getText(sign_up1_email)

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onInputIssued(sign_up1_email, R.string.sign_up1_email_error, formStateChecker)
        } else {
            ThreadUtil.runOnBackground {
                if (!CommandProcess.checkEmailDuplicated(email).isSucceed) {
                    ThreadUtil.runOnUiThread {
                        onInputIssued(sign_up1_email, R.string.sign_up1_email_duplicated, formStateChecker)
                    }
                }
            }
        }
    }

    private fun onPasswordChanged(editable: Editable?) {
        editable?.let {
            if (it.length in 8..16) {
                formStateChecker.setState(sign_up1_password, true)
                sign_up1_password.isErrorEnabled = false
            } else if (getFocusCount(sign_up1_password) != 1) {
                onInputIssued(sign_up1_password, R.string.sign_up1_password_error, formStateChecker)
            }
            onPasswordRepeatChanged(null)
        }
    }

    private fun onLeavePassword() {
        if (ViewUtil.getText(sign_up1_password).length !in 8..16) {
            onInputIssued(sign_up1_password, R.string.sign_up1_password_error, formStateChecker)
        }
    }

    private fun onPasswordRepeatChanged(editable: Editable?) {
        val focusCount = getFocusCount(sign_up1_password_repeat)

        if (ViewUtil.hasSameText(sign_up1_password, sign_up1_password_repeat)) {
            formStateChecker.setState(sign_up1_password_repeat, true)
            sign_up1_password_repeat.isErrorEnabled = false
        } else if (focusCount > 1) {
            onInputIssued(sign_up1_password_repeat, R.string.sign_up1_password_repeat_error, formStateChecker)
        }
        enableNextButton()
    }

    private fun onLeavePasswordRepeat() {
        if (!ViewUtil.hasSameText(sign_up1_password, sign_up1_password_repeat)) {
            onInputIssued(sign_up1_password_repeat, R.string.sign_up1_password_repeat_error, formStateChecker)
        }
    }
}