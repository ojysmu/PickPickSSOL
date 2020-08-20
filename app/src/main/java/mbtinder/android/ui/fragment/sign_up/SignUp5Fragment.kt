package mbtinder.android.ui.fragment.sign_up

import android.content.Intent
import android.os.Bundle
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
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.*
import mbtinder.lib.component.user.MBTIContent
import mbtinder.lib.component.user.SignUpQuestionContent
import mbtinder.lib.constant.MBTI
import mbtinder.lib.util.JSONList
import mbtinder.lib.util.toJSONList

class SignUp5Fragment : Fragment(R.layout.fragment_sign_up5) {
    private lateinit var mbtiAdapter: MBTIAdapter
    private lateinit var signUpQuestionAdapter: SignUpQuestionAdapter
    private lateinit var signUpQuestions: JSONList<SignUpQuestionContent>

    private var isSignInDone = false

    override fun initializeView() {
        initializeMBTI()
        initializeSignUpQuestions()
        sign_up5_go_test.setOnClickListener { onTestClicked() }
        switchable_next.setOnClickListener { onNextClicked() }
    }

    private fun initializeMBTI() {
        mbtiAdapter = MBTIAdapter(MBTI.toMBTIContents() as MutableList<MBTIContent>)
        sign_up5_mbti_recycler_view.layoutManager = GridLayoutManager(requireContext(), 2, HORIZONTAL, false)
        sign_up5_mbti_recycler_view.itemAnimator = DefaultItemAnimator()
        sign_up5_mbti_recycler_view.adapter = mbtiAdapter
    }

    private fun initializeSignUpQuestions() {
        signUpQuestions = requireArguments().getJSONArray("sign_up_questions")!!.toJSONList()
        signUpQuestionAdapter = SignUpQuestionAdapter(signUpQuestions)
        sign_up5_questions_recycler_view.layoutManager = LinearLayoutManager(requireContext())
        sign_up5_questions_recycler_view.itemAnimator = DefaultItemAnimator()
        sign_up5_questions_recycler_view.adapter = signUpQuestionAdapter
        sign_up5_questions_recycler_view.isNestedScrollingEnabled = false
    }

    private fun onTestClicked() {
        val intent = Intent(requireContext(), WebViewActivity::class.java)
        intent.putExtra("url", URL_MBTI_TEST)
        startActivity(intent)
    }

    private fun onNextClicked() {
        val positions = signUpQuestionAdapter.getCheckedItemPositions()
        if (positions.contains(-1)) {
            Toast.makeText(requireContext(), R.string.sign_up5_empty_question, Toast.LENGTH_SHORT).show()
            return
        }

        ViewUtil.switchNextButton(layout_sign_up5)
        runOnBackground {
            if (isSignInDone || signUp()) {
                isSignInDone = true

                (0 until signUpQuestions.size).forEach { signUpQuestions[it].selected = positions[it] }
                val mbtiResult = setMBTI()
                val questionResult = setSignUpQuestion()

                if (!(mbtiResult && questionResult)) {
                    runOnUiThread {
                        ViewUtil.switchNextButton(layout_sign_up5)
                        Toast.makeText(requireContext(), R.string.sign_up5_failed, Toast.LENGTH_SHORT).show()
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
            description = requireArguments().getString("description")!!,
            passwordQuestionId = requireArguments().getInt("password_question_id"),
            passwordAnswer = requireArguments().getString("password_answer")!!
        )

        return if (signUpResult.isSucceed) {
            CommandProcess.uploadProfileImage(signUpResult.result!!, requireArguments().getByteArray("profile")!!)
            return signIn()
        } else {
            runOnUiThread {
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (LocationUtil.isLocationPermissionGranted(requestCode, grantResults)) {
            runOnBackground {
                val coordinator = LocationUtil.onLocationPermissionGranted(requireContext())
                Log.v("SignInFragment.onRequestPermissionsResult(): coordinator=$coordinator")
                StaticComponent.user.lastLocationLng = coordinator.longitude
                StaticComponent.user.lastLocationLat = coordinator.latitude
                CommandProcess.setCoordinator(StaticComponent.user.userId, coordinator)

                val bundle = Bundle().apply { putBoolean("is_first", true) }
                runOnUiThread { findNavController().navigate(R.id.action_to_home, bundle) }
            }
        } else {
            Toast.makeText(requireContext(), R.string.common_require_location_permission, Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private companion object {
        const val URL_MBTI_TEST = "https://www.16personalities.com/ko/%EB%AC%B4%EB%A3%8C-%EC%84%B1%EA%B2%A9-%EC%9C%A0%ED%98%95-%EA%B2%80%EC%82%AC"
    }
}