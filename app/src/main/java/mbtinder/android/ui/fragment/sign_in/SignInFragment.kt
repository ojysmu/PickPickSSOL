package mbtinder.android.ui.fragment.sign_in

import android.text.Editable
import android.util.Patterns
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_in.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.*

class SignInFragment : Fragment(R.layout.fragment_sign_in) {
    private val formStatus = arrayOf(false, false)

    override fun initializeView() {
        initializeFocusableEditText(sign_in_email, this::onEmailChanged, this::onLeaveEmail)
        initializeFocusableEditText(sign_in_password, this::onPasswordChanged, this::onLeavePassword)

        sign_in_forgot.setOnClickListener {
            findNavController().navigate(R.id.action_to_find_password)
        }

        switchable_next.setOnClickListener {
            ViewUtil.switchNextButton(layout_sign_in)
            val email = sign_in_email.getText()
            val password = sign_in_password.getText()

            StaticComponent.signIn(this, email, password) {
                Toast.makeText(requireContext(), R.string.sign_in_failed, Toast.LENGTH_SHORT).show()
                ViewUtil.switchNextButton(layout_sign_in)
            }
        }
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
        if (!Patterns.EMAIL_ADDRESS.matcher(sign_in_email.getText()).matches()) {
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
        if (sign_in_password.getText().length !in 8..16) {
            onPasswordIssued()
        }
    }

    private fun onPasswordIssued() {
        formStatus[1] = false
        sign_in_password.isErrorEnabled = true
        sign_in_password.error = getString(R.string.sign_in_password_error)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (LocationUtil.isLocationPermissionGranted(requestCode, grantResults)) {
            runOnBackground {
                val coordinator = LocationUtil.onLocationPermissionGranted(requireContext())
                Log.v("SignInFragment.onRequestPermissionsResult(): coordinator=$coordinator")
                StaticComponent.user.lastLocationLng = coordinator.longitude
                StaticComponent.user.lastLocationLat = coordinator.latitude
                CommandProcess.setCoordinator(StaticComponent.user.userId, coordinator)

                runOnUiThread { findNavController().navigate(R.id.action_to_home) }
            }
        } else {
            Toast.makeText(requireContext(), R.string.common_require_location_permission, Toast.LENGTH_SHORT).show()
            requireActivity().finish()
        }
    }
}