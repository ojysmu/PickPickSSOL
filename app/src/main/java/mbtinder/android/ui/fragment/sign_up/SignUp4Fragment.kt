package mbtinder.android.ui.fragment.sign_up

import android.content.Intent
import android.widget.Toast
import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import kotlinx.android.synthetic.main.fragment_sign_up4.*
import mbtinder.android.R
import mbtinder.android.WebViewActivity
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.ThreadUtil
import mbtinder.android.util.ViewUtil
import mbtinder.lib.component.MBTIContent

class SignUp4Fragment : Fragment(R.layout.fragment_sign_up4) {
    override fun initializeView() {
        val mbtiTitles = requireContext().resources.getStringArray(R.array.common_mbti_titles)
        val mbtiContents = requireContext().resources.getStringArray(R.array.common_mbti_contents)
        val mbtis = ArrayList<MBTIContent>().apply {
            (0 until 16).forEach { add(MBTIContent(mbtiTitles[it], mbtiContents[it])) }
        }

        sign_up4_mbti_recycler_view.layoutManager = GridLayoutManager(requireContext(), 2, HORIZONTAL, false)
        sign_up4_mbti_recycler_view.itemAnimator = DefaultItemAnimator()
        sign_up4_mbti_recycler_view.adapter = MBTIAdapter(mbtis)

        val signUpQuestions = CommandProcess.getSignUpQuestion().result!!
        val signUpQuestionAdapter = SignUpQuestionAdapter(signUpQuestions)
        sign_up4_questions_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        sign_up4_questions_recycler_view.itemAnimator = DefaultItemAnimator()
        sign_up4_questions_recycler_view.adapter = signUpQuestionAdapter
        sign_up4_questions_recycler_view.isNestedScrollingEnabled = false

        sign_up4_go_test.setOnClickListener {
            val intent = Intent(requireContext(), WebViewActivity::class.java)
            intent.putExtra("url", URL_MBTI_TEST)
            startActivity(intent)
        }

        switchable_next.setOnClickListener {
            val positions = signUpQuestionAdapter.getCheckedItemPositions()
            if (positions.contains(-1)) {
                Toast.makeText(requireContext(), R.string.sign_up4_empty_question, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            ViewUtil.switchNextButton(layout_sign_up4)
            ThreadUtil.runOnBackground {
                (0 until signUpQuestions.size).forEach { signUpQuestions[it].selected = positions[it] }

                val setResult = CommandProcess.setSignUpQuestions(
                    userId = StaticComponent.user.userId,
                    signUpQuestions = signUpQuestions
                )

                if (setResult.isSucceed) {

                } else {
                    ThreadUtil.runOnUiThread {
                        ViewUtil.switchNextButton(layout_sign_up4)
                        Toast.makeText(requireContext(), R.string.sign_up4_failed, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private companion object {
        const val URL_MBTI_TEST = "https://www.16personalities.com/ko/%EB%AC%B4%EB%A3%8C-%EC%84%B1%EA%B2%A9-%EC%9C%A0%ED%98%95-%EA%B2%80%EC%82%AC"
    }
}