package mbtinder.android.ui.fragment.sign_up

import android.text.Editable
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_up3.*
import mbtinder.android.R
import mbtinder.android.io.SocketUtil
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.FormStateChecker
import mbtinder.android.util.ThreadUtil
import mbtinder.android.util.ViewUtil

class SignUp3Fragment : Fragment(R.layout.fragment_sign_up3) {
    private val formStateChecker = FormStateChecker()
    private var gender: Int = -1

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

        switchable_next.setOnClickListener {
            findNavController().navigate(R.id.action_to_sign_up4)
//            ViewUtil.switchNextButton(layout_sign_up3)
//
//            ThreadUtil.runOnBackground {
//                val signUpResult = SocketUtil.signUp(
//                    requireArguments().getString("email")!!,
//                    requireArguments().getString("password")!!,
//                    ViewUtil.getText(sign_up3_name),
//                    ViewUtil.getText(sign_up3_age).toInt(),
//                    gender,
//                    requireArguments().getInt("password_question_id"),
//                    requireArguments().getString("password_answer")!!
//                )
//
//                if (signUpResult.isSucceed) {
//
//                } else {
//                    ThreadUtil.runOnUiThread {
//                        ViewUtil.switchNextButton(layout_sign_up3)
//                    }
//                }
//            }
        }
    }

    private fun enableNextButton() {
        switchable_next.isEnabled = !formStateChecker.hasFalse()
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
}