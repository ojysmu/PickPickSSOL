package mbtinder.android.ui.fragment.sign_up

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import kotlinx.android.synthetic.main.fragment_sign_up4.*
import mbtinder.android.R
import mbtinder.android.io.CommandProcess
import mbtinder.android.ui.model.Fragment
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

        val signUpQuestions = CommandProcess.getSignUpQuestion()
        sign_up4_questions_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        sign_up4_questions_recycler_view.itemAnimator = DefaultItemAnimator()
        sign_up4_questions_recycler_view.adapter = SignUpQuestionAdapter(signUpQuestions.result!!)
    }
}