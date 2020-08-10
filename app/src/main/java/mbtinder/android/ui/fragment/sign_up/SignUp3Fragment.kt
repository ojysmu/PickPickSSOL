package mbtinder.android.ui.fragment.sign_up

import android.text.Editable
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_up3.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.FormStateChecker
import mbtinder.android.util.SharedPreferencesUtil
import mbtinder.android.util.ThreadUtil
import mbtinder.android.util.ViewUtil

class SignUp3Fragment : Fragment(R.layout.fragment_sign_up3) {
    private val formStateChecker = FormStateChecker()
    private var gender: Int = -1

    private val email by lazy { requireArguments().getString("email")!! }
    private val password by lazy { requireArguments().getString("password")!! }

    override fun initializeView() {
        formStateChecker.addViews(sign_up3_name, sign_up3_gender_selector, sign_up3_age)

        initializeFocusableEditText(sign_up3_name, this::onNameChanged)
        initializeFocusableEditText(sign_up3_age, this::onAgeChanged)

        sign_up3_gender_selector.addOnButtonCheckedListener { _, checkedId, isChecked ->
            gender = if (isChecked) {
                when (checkedId) {
                    R.id.sign_up3_gender_male -> 0
                    R.id.sign_up3_gender_female -> 1
                    else -> -1
                }
            } else {
                -1
            }
            formStateChecker.setState(sign_up3_gender_selector, isChecked)
        }

        sign_up3_next.setOnClickListener {
            val arguments = requireArguments()
            arguments.putString("name", ViewUtil.getText(sign_up3_name))
            arguments.putInt("gender", gender)
            arguments.putInt("age", ViewUtil.getText(sign_up3_age).toInt())

            findNavController().navigate(R.id.action_to_sign_up4, arguments)
        }
    }

    private fun enableNextButton() {
        sign_up3_next.isEnabled = !formStateChecker.hasFalse()
    }

    private fun onNameChanged(editable: Editable?) {
        editable?.let {
            if (it.isNotBlank()) {
                formStateChecker.setState(sign_up3_name, true)
                sign_up3_name.isErrorEnabled = false
            } else {
                onInputIssued(sign_up3_name, R.string.sign_up3_name_error, formStateChecker)
            }
            enableNextButton()
        }
    }

    private fun onAgeChanged(editable: Editable?) {
        editable?.let {
            if (it.isNotBlank()) {
                formStateChecker.setState(sign_up3_age, true)
                sign_up3_age.isErrorEnabled = false
            } else {
                onInputIssued(sign_up3_age, R.string.sign_up3_age_error, formStateChecker)
            }
            enableNextButton()
        }
    }

    private fun signUp() {
        val signUpResult = CommandProcess.signUp(
            email = email,
            password = password,
            name = ViewUtil.getText(sign_up3_name),
            age = ViewUtil.getText(sign_up3_age).toInt(),
            gender = gender,
            passwordQuestionId = requireArguments().getInt("password_question_id"),
            passwordAnswer = requireArguments().getString("password_answer")!!
        )

        if (signUpResult.isSucceed) {
            signIn()
        } else {
            ThreadUtil.runOnUiThread {
                ViewUtil.switchNextButton(layout_sign_up3)
                Toast.makeText(requireContext(), R.string.sign_up3_sign_up_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signIn() {
        val signInResult = CommandProcess.signIn(email = email, password = password)

        if (signInResult.isSucceed) {
            SharedPreferencesUtil
                .getContext(requireContext(), SharedPreferencesUtil.PREF_ACCOUNT)
                .put("email", email)
                .put("password", password)
            StaticComponent.user = signInResult.result!!
            findNavController().navigate(R.id.action_to_sign_up4)
        } else {
            ThreadUtil.runOnUiThread {
                Toast.makeText(requireContext(), R.string.sign_up3_sign_in_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}