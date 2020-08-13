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
import mbtinder.android.ui.model.ProgressFragment
import mbtinder.android.util.ViewUtil
import mbtinder.android.util.runOnBackground
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.constant.PasswordQuestion

class FindPasswordFragment : ProgressFragment(R.layout.fragment_find_password) {
    override fun initializeView() {
        super.initializeView()

        val questionSelector = find_password_question_selector.findViewById<Spinner>(R.id.spinner)

        initializeFocusableEditText(find_password_email, this::onEmailChanged, this::onLeaveEmail)
        initializeFocusableEditText(find_password_answer, this::onAnswerChanged)

        questionSelector.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, PasswordQuestion.values().map { it.question })

        switchable_next.setOnClickListener {
            switchWaitingStatus()
            val email = ViewUtil.getText(find_password_email)
            val questionId = PasswordQuestion.findQuestion(questionSelector.selectedItem as String)!!.questionId
            val answer = ViewUtil.getText(find_password_answer)

            runOnBackground {
                val findResult = CommandProcess.findPassword(email, questionId, answer)

                if (findResult.isSucceed) {
                    val bundle = Bundle().apply { putString("user_id", findResult.result!!.toString()) }
                    findNavController().navigate(R.id.action_to_update_password, bundle)
                } else {
                    runOnUiThread {
                        switchWaitingStatus()
                        Toast.makeText(requireContext(), R.string.find_password_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun onEmailChanged(editable: Editable?) {
        editable?.let {
            if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                setFormStatus(find_password_email, true)
                find_password_email.isErrorEnabled = false
            } else if (getFocusCount(find_password_email) != 1) {
                onEmailIssued()
            }
            enableNextButton()
        }
    }

    private fun onLeaveEmail() {
        val email = ViewUtil.getText(find_password_email)

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            onEmailIssued()
        }
    }

    private fun onEmailIssued() {
        setFormStatus(find_password_email, false)
        find_password_email.isErrorEnabled = true
        find_password_email.error = getString(R.string.find_password_email_error)
    }

    private fun onAnswerChanged(editable: Editable?) {
        editable?.let {
            setFormStatus(find_password_answer, it.isNotBlank())
            enableNextButton()
        }
    }
}