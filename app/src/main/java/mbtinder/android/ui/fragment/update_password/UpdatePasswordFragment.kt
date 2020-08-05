package mbtinder.android.ui.fragment.update_password

import android.text.Editable
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_update_password.*
import mbtinder.android.R
import mbtinder.android.io.CommandProcess
import mbtinder.android.io.SocketUtil
import mbtinder.android.ui.model.ProgressFragment
import mbtinder.android.util.ThreadUtil
import mbtinder.android.util.ViewUtil
import java.util.*

class UpdatePasswordFragment : ProgressFragment(R.layout.fragment_update_password) {
    override fun initializeView() {
        super.initializeView()

        val userId = UUID.fromString(requireArguments().getString("user_id")!!)

        initializeFocusableEditText(update_password_password, this::onPasswordChanged, this::onLeavePassword)
        initializeFocusableEditText(update_password_password_repeat, this::onPasswordRepeatChanged, this::onLeavePasswordRepeat)

        switchable_next.setOnClickListener {
            switchWaitingStatus()
            ThreadUtil.runOnBackground {
                val password = ViewUtil.getText(update_password_password) // TODO: encrypt
                val updateResult = CommandProcess.updatePassword(userId, password)
                if (updateResult.isSucceed) {
                    ThreadUtil.runOnUiThread {
                        Toast.makeText(requireContext(), R.string.update_password_succeed, Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                }
            }
        }
    }

    private fun onPasswordChanged(editable: Editable?) {
        editable?.let {
            if (it.length in 8..16) {
                setFormStatus(update_password_password, true)
                update_password_password.isErrorEnabled = false
            } else if (getFocusCount(update_password_password) != 1) {
                onPasswordIssued()
            }
            onPasswordRepeatChanged(it)
        }
    }

    private fun onLeavePassword() {
        if (ViewUtil.getText(update_password_password).length !in 8..16) {
            onPasswordIssued()
        }
    }

    private fun onPasswordIssued() {
        setFormStatus(update_password_password, false)
        update_password_password.isErrorEnabled = true
        update_password_password.error = getString(R.string.update_password_password_error)
    }

    private fun onPasswordRepeatChanged(editable: Editable?) {
        val focusCount = getFocusCount(update_password_password_repeat)

        if (ViewUtil.hasSameText(update_password_password, update_password_password_repeat)) {
            setFormStatus(update_password_password_repeat, true)
            update_password_password_repeat.isErrorEnabled = false
        } else if (focusCount > 1) {
            onPasswordRepeatIssued()
        }
        enableNextButton()
    }

    private fun onLeavePasswordRepeat() {
        if (!ViewUtil.hasSameText(update_password_password, update_password_password_repeat)) {
            onPasswordRepeatIssued()
        }
    }

    private fun onPasswordRepeatIssued() {
        setFormStatus(update_password_password_repeat, false)
        update_password_password_repeat.isErrorEnabled = true
        update_password_password_repeat.error = getString(R.string.update_password_password_repeat_error)
    }
}