package mbtinder.android.ui.fragment.find_password

import android.os.Bundle
import android.text.Editable
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import kotlinx.android.synthetic.main.fragment_find_password.*
import mbtinder.android.R
import mbtinder.android.io.SocketUtil
import mbtinder.android.ui.model.ProgressFragment
import mbtinder.android.util.ThreadUtil
import mbtinder.android.util.ViewUtil
import mbtinder.lib.constant.PasswordQuestion

class FindPasswordFragment : ProgressFragment() {
    private val focusCount = HashMap<View, Int>()

    override fun initializeView() {
        super.initializeView()

        val questionSelector = find_password_question_selector.findViewById<Spinner>(R.id.spinner)

        find_password_email.editText!!.setOnFocusChangeListener(this::onFocusChanged)
        find_password_email.editText!!.addTextChangedListener(afterTextChanged = this::onEmailChanged)

        find_password_answer.editText!!.addTextChangedListener(afterTextChanged = this::onAnswerChanged)

        switchable_next.setOnClickListener {
            switchWaitingStatus()
            val email = ViewUtil.getText(find_password_email)
            val questionId = PasswordQuestion.findQuestion(questionSelector.selectedItem as String)!!.questionId
            val answer = ViewUtil.getText(find_password_answer)

            ThreadUtil.runOnBackground {
                val findResult = SocketUtil.findPassword(email, questionId, answer)

                if (findResult.isSucceed) {

                } else {
                    ThreadUtil.runOnUiThread {
                        switchWaitingStatus()
                        Toast.makeText(requireContext(), R.string.find_password_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.fragment_find_password, inflater, container!!)
    }

    private fun onFocusChanged(view: View, hasFocus: Boolean) {
        if (hasFocus) {
            focusCount[view] = focusCount.getOrDefault(view, 0) + 1
        } else {
            when (view) {
                find_password_email.editText!! -> onLeaveEmail()
            }
        }
    }

    private fun onEmailChanged(editable: Editable?) {
        editable?.let {
            if (Patterns.EMAIL_ADDRESS.matcher(it).matches()) {
                setFormStatus(find_password_email, true)
                find_password_email.isErrorEnabled = false
            } else if (focusCount[find_password_email.editText!!] != 1) {
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