package mbtinder.android.ui.fragment.sign_up

import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_up.*
import mbtinder.android.R
import mbtinder.android.io.CommandProcess
import mbtinder.android.io.SocketUtil
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.ThreadUtil
import mbtinder.android.util.ViewUtil
import mbtinder.lib.constant.PasswordQuestion

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {
    private val formStatus = arrayOf(
        false, // 0: 이메일
        false, // 1: 비밀번호
        false, // 2: 비밀번호 재입력
        false, // 3: 성별
        false, // 4: 나이
        false, // 5: 이름
        false  // 7: 찾기 답변
    )

    private var gender: Int = -1

    override fun initializeView() {
        val passwordQuestionSelector = sign_up_password_question_selector.findViewById<Spinner>(R.id.spinner)

        initializeFocusableEditText(sign_up1_email, this::onEmailChanged, this::onLeaveEmail)
        initializeFocusableEditText(sign_up1_password, this::onPasswordChanged, this::onLeavePassword)
        initializeFocusableEditText(sign_up1_password_repeat, this::onPasswordRepeatChanged, this::onLeavePasswordRepeat)
        initializeFocusableEditText(sign_up_age, this::onAgeChanged)
        initializeFocusableEditText(sign_up_name, this::onNameChanged)
        initializeFocusableEditText(sign_up_password_answer, this::onAnswerChanged)

        sign_up_gender_selector.addOnButtonCheckedListener(this::onGenderSelected)
        passwordQuestionSelector.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, PasswordQuestion.values().map { it.question })

        switchable_next.setOnClickListener {
            ViewUtil.switchNextButton(layout_sign_up)
            ThreadUtil.runOnBackground {
                val signUpResult = CommandProcess.signUp(
                    ViewUtil.getText(sign_up1_email),
                    ViewUtil.getText(sign_up1_password),
                    ViewUtil.getText(sign_up_name),
                    ViewUtil.getText(sign_up_age).toInt(),
                    gender,
                    PasswordQuestion.findQuestion(passwordQuestionSelector.selectedItem as String)!!.ordinal,
                    ViewUtil.getText(sign_up_password_answer)
                )

                if (signUpResult.isSucceed) {
                    ThreadUtil.runOnUiThread {
                        Toast.makeText(requireContext(), R.string.sign_up_succeed, Toast.LENGTH_SHORT).show()
                        findNavController().popBackStack()
                    }
                } else {
                    ThreadUtil.runOnUiThread {
                        Toast.makeText(requireContext(), R.string.sign_up_failed, Toast.LENGTH_SHORT).show()
                        ViewUtil.switchNextButton(layout_sign_up)
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.fragment_sign_up, inflater, container!!)
    }

    private fun enableNextButton() {
        switchable_next.isEnabled = !formStatus.contains(false)
    }

    private fun onEmailChanged(editable: Editable?) {
        editable?.let {
            if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                formStatus[0] = true
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
                if (!CommandProcess.checkEmailDuplicated(email).isSucceed) {
                    ThreadUtil.runOnUiThread { onEmailIssued(R.string.sign_up_email_duplicated) }
                }
            }
        }
    }

    private fun onEmailIssued(@StringRes error: Int) {
        formStatus[0] = false
        sign_up1_email.isErrorEnabled = true
        sign_up1_email.error = getString(error)
    }

    private fun onPasswordChanged(editable: Editable?) {
        editable?.let {
            if (it.length in 8..16) {
                formStatus[1] = true
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
        formStatus[1] = false
        sign_up1_password.isErrorEnabled = true
        sign_up1_password.error = getString(R.string.sign_up_password_error)
    }

    private fun onPasswordRepeatChanged(editable: Editable?) {
        val focusCount = getFocusCount(sign_up1_password_repeat)

        if (ViewUtil.hasSameText(sign_up1_password, sign_up1_password_repeat)) {
            formStatus[2] = true
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
        formStatus[2] = false
        sign_up1_password_repeat.isErrorEnabled = true
        sign_up1_password_repeat.error = getString(R.string.sign_up_password_repeat_error)
    }

    private fun onGenderSelected(view: View, checkedId: Int, isChecked: Boolean) {
        hideIme()
        gender = if (isChecked) {
            when (checkedId) {
                R.id.sign_up_gender_male -> 0
                R.id.sign_up_gender_female -> 1
                else -> -1
            }
        } else {
            -1
        }
        formStatus[3] = gender != -1
        enableNextButton()
    }

    private fun onAgeChanged(editable: Editable?) {
        editable?.let {
            formStatus[4] = it.isNotBlank()
            enableNextButton()
        }
    }

    private fun onNameChanged(editable: Editable?) {
        editable?.let {
            formStatus[5] = it.isNotBlank()
            enableNextButton()
        }
    }

    private fun onAnswerChanged(editable: Editable?) {
        editable?.let {
            formStatus[6] = it.isNotBlank()
            enableNextButton()
        }
    }
}
