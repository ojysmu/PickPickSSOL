package mbtinder.android.ui.fragment.update_password

import android.text.Editable
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_update_password.*
import mbtinder.android.R
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.*
import java.util.*

class UpdatePasswordFragment : Fragment(R.layout.fragment_update_password) {
    private val formStateChecker = FormStateChecker()

    override fun initializeView() {
        formStateChecker.addViews(update_password_password, update_password_password_repeat)

        initializeFocusableEditText(update_password_password, this::onPasswordChanged, this::onLeavePassword)
        initializeFocusableEditText(update_password_password_repeat, this::onPasswordRepeatChanged, this::onLeavePasswordRepeat)
        switchable_next.setOnClickListener { onNextClicked() }
    }

    private fun onNextClicked() {
        val userId = UUID.fromString(requireArguments().getString("user_id")!!)

        ViewUtil.switchNextButton(layout_update_password)
        runOnBackground {
            val password = update_password_password.getText()
            val updateResult = CommandProcess.updatePassword(userId, password)
            if (updateResult.isSucceed) {
                runOnUiThread {
                    Toast.makeText(requireContext(), R.string.update_password_succeed, Toast.LENGTH_SHORT).show()
                    findNavController().popBackStack()
                }
            }
        }
    }

    private fun enableNextButton() {
        switchable_next.isEnabled = !formStateChecker.hasFalse()
    }

    private fun onPasswordChanged(editable: Editable?) {
        editable?.let {
            if (it.length in 8..16) {
                formStateChecker.setState(update_password_password, true)
                update_password_password.isErrorEnabled = false
            } else if (getFocusCount(update_password_password) != 1) {
                onPasswordIssued()
            }
            onPasswordRepeatChanged(it)
        }
    }

    private fun onLeavePassword() {
        if (update_password_password.getText().length !in 8..16) {
            onPasswordIssued()
        }
    }

    private fun onPasswordIssued() {
        formStateChecker.setState(update_password_password, false)
        update_password_password.isErrorEnabled = true
        update_password_password.error = getString(R.string.update_password_password_error)
    }

    private fun onPasswordRepeatChanged(editable: Editable?) {
        val focusCount = getFocusCount(update_password_password_repeat)

        if (update_password_password.isSame(update_password_password_repeat)) {
            formStateChecker.setState(update_password_password_repeat, true)
            update_password_password_repeat.isErrorEnabled = false
        } else if (focusCount > 1) {
            onPasswordRepeatIssued()
        }
        enableNextButton()
    }

    private fun onLeavePasswordRepeat() {
        if (!update_password_password.isSame(update_password_password_repeat)) {
            onPasswordRepeatIssued()
        }
    }

    private fun onPasswordRepeatIssued() {
        formStateChecker.setState(update_password_password_repeat, false)
        update_password_password_repeat.isErrorEnabled = true
        update_password_password_repeat.error = getString(R.string.update_password_password_repeat_error)
    }
}