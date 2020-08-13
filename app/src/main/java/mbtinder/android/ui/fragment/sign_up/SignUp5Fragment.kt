package mbtinder.android.ui.fragment.sign_up

import android.content.Intent
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import kotlinx.android.synthetic.main.fragment_sign_up3.*
import kotlinx.android.synthetic.main.fragment_sign_up5.*
import mbtinder.android.R
import mbtinder.android.WebViewActivity
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.database.SQLiteConnection
import mbtinder.android.io.http.SQLiteDownloader
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.SharedPreferencesUtil
import mbtinder.android.util.ThreadUtil
import mbtinder.android.util.ViewUtil
import mbtinder.lib.component.MBTIContent
import mbtinder.lib.component.SignUpQuestionContent
import mbtinder.lib.constant.MBTI
import mbtinder.lib.util.JSONList

class SignUp5Fragment : Fragment(R.layout.fragment_sign_up5) {
    private lateinit var mbtiAdapter: MBTIAdapter
    private lateinit var signUpQuestionAdapter: SignUpQuestionAdapter
    private lateinit var signUpQuestions: JSONList<SignUpQuestionContent>

    private var isSignInDone = false

    override fun initializeView() {
        mbtiAdapter = MBTIAdapter(MBTI.toMBTIContents() as MutableList<MBTIContent>)
        sign_up5_mbti_recycler_view.layoutManager = GridLayoutManager(requireContext(), 2, HORIZONTAL, false)
        sign_up5_mbti_recycler_view.itemAnimator = DefaultItemAnimator()
        sign_up5_mbti_recycler_view.adapter = mbtiAdapter

        signUpQuestions = CommandProcess.getSignUpQuestion().result!!
        signUpQuestionAdapter = SignUpQuestionAdapter(signUpQuestions)
        sign_up5_questions_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        sign_up5_questions_recycler_view.itemAnimator = DefaultItemAnimator()
        sign_up5_questions_recycler_view.adapter = signUpQuestionAdapter
        sign_up5_questions_recycler_view.isNestedScrollingEnabled = false

        sign_up5_go_test.setOnClickListener {
            val intent = Intent(requireContext(), WebViewActivity::class.java)
            intent.putExtra("url", URL_MBTI_TEST)
            startActivity(intent)
        }

        switchable_next.setOnClickListener {
            val positions = signUpQuestionAdapter.getCheckedItemPositions()
            if (positions.contains(-1)) {
                Toast.makeText(requireContext(), R.string.sign_up5_empty_question, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ViewUtil.switchNextButton(layout_sign_up5)
            ThreadUtil.runOnBackground {
                if (isSignInDone || signUp()) {
                    isSignInDone = true

                    (0 until signUpQuestions.size).forEach { signUpQuestions[it].selected = positions[it] }
                    val mbtiResult = setMBTI()
                    val questionResult = setSignUpQuestion()

                    if (mbtiResult && questionResult) {
                        findNavController().navigate(R.id.action_to_home)
                    } else {
                        ThreadUtil.runOnUiThread {
                            ViewUtil.switchNextButton(layout_sign_up5)
                            Toast.makeText(requireContext(), R.string.sign_up5_failed, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }

    private fun signUp(): Boolean {
        val signUpResult = CommandProcess.signUp(
            email = requireArguments().getString("email")!!,
            password = requireArguments().getString("password")!!,
            name = requireArguments().getString("name")!!,
            age = requireArguments().getInt("age"),
            gender = requireArguments().getInt("gender"),
            passwordQuestionId = requireArguments().getInt("password_question_id"),
            passwordAnswer = requireArguments().getString("password_answer")!!
        )

        return if (signUpResult.isSucceed) {
            signIn()
        } else {
            ThreadUtil.runOnUiThread {
                ViewUtil.switchNextButton(layout_sign_up3)
                Toast.makeText(requireContext(), R.string.sign_up3_sign_up_failed, Toast.LENGTH_SHORT).show()
            }
            false
        }
    }

    private fun signIn() =
        StaticComponent.signIn(this, requireArguments().getString("email")!!, requireArguments().getString("password")!!) {
            Toast.makeText(requireContext(), R.string.sign_up3_sign_in_failed, Toast.LENGTH_SHORT).show()
        }

    private fun setMBTI() = CommandProcess.setMBTI(
        userId = StaticComponent.user.userId,
        mbti = mbtiAdapter.getCheckedIndex().title
    ).isSucceed

    private fun setSignUpQuestion() = CommandProcess.setSignUpQuestions(
        userId = StaticComponent.user.userId,
        signUpQuestions = signUpQuestions
    ).isSucceed

    private companion object {
        const val URL_MBTI_TEST = "https://www.16personalities.com/ko/%EB%AC%B4%EB%A3%8C-%EC%84%B1%EA%B2%A9-%EC%9C%A0%ED%98%95-%EA%B2%80%EC%82%AC"
    }
}