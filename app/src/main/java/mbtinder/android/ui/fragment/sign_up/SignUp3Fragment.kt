package mbtinder.android.ui.fragment.sign_up

import android.text.Editable
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_up3.*
import mbtinder.android.R
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.FormStateChecker
import mbtinder.android.util.ViewUtil
import mbtinder.android.util.getText

class SignUp3Fragment : Fragment(R.layout.fragment_sign_up3) {
    private val formStateChecker = FormStateChecker()
    private var gender: Int = -1

    override fun initializeView() {
        formStateChecker.addViews(sign_up3_name, sign_up3_gender_selector, sign_up3_age)

        initializeFocusableEditText(sign_up3_name, this::onNameChanged)
        initializeFocusableEditText(sign_up3_age, this::onAgeChanged)

        sign_up3_gender_selector.addOnButtonCheckedListener { _, checkedId, isChecked ->
            onGenderSelected(checkedId, isChecked)
        }

        sign_up3_next.setOnClickListener { onNextClicked() }
    }

    private fun onGenderSelected(checkedId: Int, isChecked: Boolean) {
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

    private fun onNextClicked() {
        val arguments = requireArguments()
        arguments.putString("name", sign_up3_name.getText())
        arguments.putInt("gender", gender)
        arguments.putInt("age", sign_up3_age.getText().toInt())

        findNavController().navigate(R.id.action_to_sign_up4, arguments)
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
}