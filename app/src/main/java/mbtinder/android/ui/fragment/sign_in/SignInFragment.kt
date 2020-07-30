package mbtinder.android.ui.fragment.sign_in

import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_in.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.SocketUtil
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.SharedPreferencesUtil
import mbtinder.android.util.ThreadUtil
import mbtinder.android.util.ViewUtil

class SignInFragment : Fragment() {
    private val formStatus = arrayOf(false, false)

    override fun initializeView() {
        initializeFocusableEditText(sign_in_email, this::onEmailChanged, this::onLeaveEmail)
        initializeFocusableEditText(sign_in_password, this::onPasswordChanged, this::onLeavePassword)

        sign_in_forgot.setOnClickListener {
            findNavController().navigate(R.id.action_to_find_password)
        }

        switchable_next.setOnClickListener {
            ViewUtil.switchNextButton(layout_sign_in)
            ThreadUtil.runOnBackground {
                val email = ViewUtil.getText(sign_in_email)
                val password = ViewUtil.getText(sign_in_password)

                val signInResult = SocketUtil.signIn(email, password)
                if (signInResult.isSucceed) {
                    SharedPreferencesUtil
                        .getContext(requireContext(), SharedPreferencesUtil.PREF_ACCOUNT)
                        .put("email", email)
                        .put("password", password)
                    StaticComponent.user = signInResult.result!!
                    ThreadUtil.runOnUiThread {
                        findNavController().navigate(R.id.action_to_home)
                    }
                } else {
                    ThreadUtil.runOnUiThread {
                        Toast.makeText(requireContext(), R.string.sign_in_failed, Toast.LENGTH_SHORT).show()
                        ViewUtil.switchNextButton(layout_sign_in)
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.fragment_sign_in, inflater, container!!)
    }

    private fun enableNextButton() {
        switchable_next.isEnabled = !formStatus.contains(false)
    }

    private fun onEmailChanged(editable: Editable?) {
        editable?.let {
            if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                formStatus[0] = true
                sign_in_email.isErrorEnabled = false
            } else if (getFocusCount(sign_in_email) != 1) {
                onEmailIssued()
            }

            enableNextButton()
        }
    }

    private fun onLeaveEmail() {
        if (!Patterns.EMAIL_ADDRESS.matcher(ViewUtil.getText(sign_in_email)).matches()) {
            onEmailIssued()
        }
    }

    private fun onEmailIssued() {
        formStatus[0] = false
        sign_in_email.isErrorEnabled = true
        sign_in_email.error = getString(R.string.sign_in_email_error)
    }

    private fun onPasswordChanged(editable: Editable?) {
        editable?.let {
            if (it.length in 8..16) {
                formStatus[1] = true
                sign_in_password.isErrorEnabled = false
            } else if (getFocusCount(sign_in_password) != 1) {
                onPasswordIssued()
            }

            enableNextButton()
        }
    }

    private fun onLeavePassword() {
        if (ViewUtil.getText(sign_in_password).length !in 8..16) {
            onPasswordIssued()
        }
    }

    private fun onPasswordIssued() {
        formStatus[1] = false
        sign_in_password.isErrorEnabled = true
        sign_in_password.error = getString(R.string.sign_up_password_error)
    }
}