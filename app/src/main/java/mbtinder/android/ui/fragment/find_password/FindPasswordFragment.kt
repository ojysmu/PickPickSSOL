package mbtinder.android.ui.fragment.find_password

import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_find_password.*
import mbtinder.android.R
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.*
import mbtinder.lib.constant.PasswordQuestion

class FindPasswordFragment : Fragment(R.layout.fragment_find_password) {
    private val formStateChecker by lazy { FormStateChecker(find_password_email, find_password_answer) }

    override fun initializeView() {
        val questionSelector = find_password_question_selector.findViewById<Spinner>(R.id.spinner)

        initializeFocusableEditText(find_password_email, this::onEmailChanged, this::onLeaveEmail)
        initializeFocusableEditText(find_password_answer, this::onAnswerChanged)

        questionSelector.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, PasswordQuestion.values().map { it.question })

        switchable_next.setOnClickListener {
            ViewUtil.switchNextButton(layout_find_password)
            val email = find_password_email.getText()
            val questionId = PasswordQuestion.findQuestion(questionSelector.selectedItem as String)!!.questionId
            val answer = find_password_answer.getText()

            runOnBackground {
                val findResult = CommandProcess.findPassword(email, questionId, answer)

                if (findResult.isSucceed) {
                    val bundle = Bundle().apply { putString("user_id", findResult.result!!.toString()) }
                    runOnUiThread { findNavController().navigate(R.id.action_to_update_password, bundle) }
                } else {
                    runOnUiThread {
                        ViewUtil.switchNextButton(layout_find_password)
                        Toast.makeText(requireContext(), R.string.find_password_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun enableNextButton() {
        switchable_next.isEnabled = !formStateChecker.hasFalse()
    }

    private fun onEmailChanged(editable: Editable?) {
        editable?.let {
            if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                formStateChecker.setState(find_password_email, true)
                find_password_email.isErrorEnabled = false
            } else if (getFocusCount(find_password_email) != 1) {
                onEmailIssued()
            }
            enableNextButton()
        }
    }

    private fun onLeaveEmail() {
        val email = find_password_email.getText()

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onEmailIssued()
        }
    }

    private fun onEmailIssued() {
        formStateChecker.setState(find_password_email, false)
        find_password_email.isErrorEnabled = true
        find_password_email.error = getString(R.string.find_password_email_error)
    }

    private fun onAnswerChanged(editable: Editable?) {
        formStateChecker.setState(find_password_answer, editable?.isNotBlank() ?: false)
        enableNextButton()
    }
}