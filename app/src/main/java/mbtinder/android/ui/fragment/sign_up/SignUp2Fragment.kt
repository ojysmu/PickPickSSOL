package mbtinder.android.ui.fragment.sign_up

import android.os.Bundle
import android.text.Editable
import android.widget.ArrayAdapter
import android.widget.Spinner
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_sign_up2.*
import mbtinder.android.R
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.FormStateChecker
import mbtinder.android.util.ViewUtil
import mbtinder.lib.constant.PasswordQuestion

class SignUp2Fragment : Fragment(R.layout.fragment_sign_up2) {
    private val formStateChecker = FormStateChecker()
    private val questionSelector by lazy { sign_up2_question_selector.findViewById<Spinner>(R.id.spinner) }

    override fun initializeView() {
        formStateChecker.addViews(sign_up2_password_answer)

        questionSelector.adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_dropdown_item_1line,
            PasswordQuestion.values().map { it.question }
        )

        initializeFocusableEditText(sign_up2_password_answer, this::onAnswerChanged)

        sign_up2_next.setOnClickListener {
            val arguments = Bundle()
            arguments.putString("email", requireArguments().getString("email")!!)
            arguments.putString("password", requireArguments().getString("password")!!)
            arguments.putInt("password_question_id", questionSelector.selectedItemPosition)
            arguments.putString("password_answer", ViewUtil.getText(sign_up2_password_answer))

            findNavController().navigate(R.id.action_to_sign_up3, arguments)
        }
    }

    private fun enableNextButton() {
        sign_up2_next.isEnabled = !formStateChecker.hasFalse()
    }

    private fun onAnswerChanged(editable: Editable?) {
        editable?.let {
            if (editable.isNotBlank()) {
                formStateChecker.setState(sign_up2_password_answer, true)
                sign_up2_password_answer.isErrorEnabled = false
            } else {
                onInputIssued(sign_up2_password_answer, R.string.sign_up2_answer_error, formStateChecker)
            }
            enableNextButton()
        }
    }
}