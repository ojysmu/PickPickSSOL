package mbtinder.android.ui.fragment.sign_up

import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ToggleButton
import androidx.annotation.IdRes
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.fragment_sign_up.*
import mbtinder.android.R
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.Log
import mbtinder.android.util.ViewUtil
import kotlin.reflect.KFunction2

class SignUpFragment : Fragment() {
    private val formStatus = arrayOf(
        false, // 0: 이메일
        false, // 1: 비밀번호
        false, // 2: 비밀번호 재입력
        false, // 3: 성별
        false, // 4: 나이
        false, // 5: 이름
        false, // 6: 찾기 질문
        false  // 7: 찾기 답변
    )

    private val focusCount = HashMap<View, Int>()

    private var gender: Int = -1

    override fun initializeView() {
        sign_up_email.editText!!.setOnFocusChangeListener(this::onFocusChanged)
        sign_up_email.editText!!.addTextChangedListener(afterTextChanged = this::onEmailChanged)

        sign_up_password.editText!!.setOnFocusChangeListener(this::onFocusChanged)
        sign_up_password.editText!!.addTextChangedListener(afterTextChanged = this::onPasswordChanged)

        sign_up_password_repeat.editText!!.setOnFocusChangeListener(this::onFocusChanged)
        sign_up_password_repeat.editText!!.addTextChangedListener(afterTextChanged = this::onPasswordRepeatChanged)

        sign_up_gender_selector.addOnButtonCheckedListener(this::onGenderSelected)

        sign_up_age.editText!!.setOnFocusChangeListener(this::onFocusChanged)
        sign_up_age.editText!!.addTextChangedListener(afterTextChanged = this::onAgeChanged)

        sign_up_name.editText!!.setOnFocusChangeListener(this::onFocusChanged)
        sign_up_name.editText!!.addTextChangedListener(afterTextChanged = this::onNameChanged)

        sign_up_password_question.editText!!.setOnFocusChangeListener(this::onFocusChanged)
        sign_up_password_question.editText!!.addTextChangedListener(afterTextChanged = this::onQuestionChanged)

        sign_up_name.editText!!.setOnFocusChangeListener(this::onFocusChanged)
        sign_up_password_answer.editText!!.addTextChangedListener(afterTextChanged = this::onAnswerChanged)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.fragment_sign_up, inflater, container!!)
    }

    private fun enableNextButton() {
        sign_up_next.isEnabled = !formStatus.contains(false)
    }

    private fun onFocusChanged(view: View, hasFocus: Boolean) {
        if (hasFocus) {
            focusCount[view] = focusCount.getOrDefault(view, 0) + 1
        } else {
            when (view) {
                sign_up_email.editText!! -> onLeaveEmail()
                sign_up_password.editText!! -> onLeavePassword()
                sign_up_password_repeat.editText!! -> onLeavePasswordRepeat()
            }
        }
    }

    private fun onEmailChanged(editable: Editable?) {
        editable?.let {
            if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                formStatus[0] = true
                sign_up_email.isErrorEnabled = false
            } else if (focusCount[sign_up_email.editText!!] != 1) {
                onEmailIssued()
            }
            enableNextButton()
        }
    }

    private fun onLeaveEmail() {
        if (!Patterns.EMAIL_ADDRESS.matcher(ViewUtil.getText(sign_up_email)).matches()) {
            onEmailIssued()
        }
    }

    private fun onEmailIssued() {
        formStatus[0] = false
        sign_up_email.isErrorEnabled = true
        sign_up_email.error = getString(R.string.sign_up_email_error)
    }

    private fun onPasswordChanged(editable: Editable?) {
        editable?.let {
            if (it.length in 8..16) {
                formStatus[1] = true
                sign_up_password.isErrorEnabled = false
            } else if (focusCount[sign_up_password.editText!!] != 1) {
                onPasswordIssued()
            }
            onPasswordRepeatChanged(it)
        }
    }

    private fun onLeavePassword() {
        if (ViewUtil.getText(sign_up_password).length !in 8..16) {
            onPasswordIssued()
        }
    }

    private fun onPasswordIssued() {
        formStatus[1] = false
        sign_up_password.isErrorEnabled = true
        sign_up_password.error = getString(R.string.sign_up_password_error)
    }

    private fun onPasswordRepeatChanged(editable: Editable?) {
        val focusCount = focusCount[sign_up_password_repeat.editText!!]

        if (ViewUtil.hasSameText(sign_up_password, sign_up_password_repeat)) {
            formStatus[2] = true
            sign_up_password_repeat.isErrorEnabled = false
        } else if (focusCount != null && focusCount != 1) {
            onPasswordRepeatIssued()
        }
        enableNextButton()
    }

    private fun onLeavePasswordRepeat() {
        if (!ViewUtil.hasSameText(sign_up_password, sign_up_password_repeat)) {
            onPasswordRepeatIssued()
        }
    }

    private fun onPasswordRepeatIssued() {
        formStatus[2] = false
        sign_up_password_repeat.isErrorEnabled = true
        sign_up_password_repeat.error = getString(R.string.sign_up_password_repeat_error)
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

    private fun onQuestionChanged(editable: Editable?) {
        editable?.let {
            formStatus[6] = it.isNotBlank()
            enableNextButton()
        }
    }

    private fun onAnswerChanged(editable: Editable?) {
        editable?.let {
            formStatus[7] = it.isNotBlank()
            enableNextButton()
        }
    }
}
