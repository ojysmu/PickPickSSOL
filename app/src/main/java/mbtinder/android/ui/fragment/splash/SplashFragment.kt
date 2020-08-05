package mbtinder.android.ui.fragment.splash

import android.animation.ObjectAnimator
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_splash.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.io.CommandProcess
import mbtinder.android.io.SocketUtil
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.Log
import mbtinder.android.util.SharedPreferencesUtil
import mbtinder.android.util.ThreadUtil

class SplashFragment : Fragment(R.layout.fragment_splash) {
    override fun initializeView() {
//        requireActivity().window.statusBarColor = requireContext().getColor(android.R.color.black)
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE

        ThreadUtil.runOnBackground {
            if (hasAccountInfo()) {
                val (email, password) = getAccountInfo()
                val signInResult = CommandProcess.signIn(email, password)
                if (signInResult.isSucceed) {
                    StaticComponent.user = signInResult.result!!
                    findNavController().navigate(R.id.action_to_home)
                } else {
                    ThreadUtil.runOnUiThread {
                        Toast.makeText(requireContext(), R.string.splash_failed_to_sign_in, Toast.LENGTH_SHORT).show()
                    }
                    removeAccountInfo()
                    loadAnimation()
                }
            } else {
                loadAnimation()
            }
        }
    }

    private fun loadAnimation() {
        Thread.sleep(1000)
        val animationDuration = if (isAnimated) {
            0L
        } else {
            500L
        }

        ThreadUtil.runOnUiThread {
            ObjectAnimator.ofFloat(splash_logo, "translationY", -1000f).apply {
                duration = animationDuration
                Log.v("duration=$duration")
            }.start()

            ThreadUtil.runOnBackground {
                Thread.sleep(animationDuration)
                isAnimated = true
                Log.v("true")
                ThreadUtil.runOnUiThread(this::initializeSignUp)
            }
        }
    }

    private fun hasAccountInfo() =
        SharedPreferencesUtil.getString(requireContext(), SharedPreferencesUtil.PREF_ACCOUNT, "email") != null

    private fun getAccountInfo() = Pair(
        SharedPreferencesUtil.getString(requireContext(), SharedPreferencesUtil.PREF_ACCOUNT, "email")!!,
        SharedPreferencesUtil.getString(requireContext(), SharedPreferencesUtil.PREF_ACCOUNT, "password")!!
    )

    private fun removeAccountInfo() {
        SharedPreferencesUtil.getContext(requireContext(), SharedPreferencesUtil.PREF_ACCOUNT)
            .removeField("email")
            .removeField("password")
    }

    private fun initializeSignUp() {
        splash_content.visibility = View.VISIBLE
        splash_sign_up.visibility = View.VISIBLE
        splash_sign_in.visibility = View.VISIBLE

        splash_sign_up.setOnClickListener {
            findNavController().navigate(R.id.action_to_sign_up)
        }

        splash_sign_in.setOnClickListener {
            findNavController().navigate(R.id.action_to_sign_in)
        }
    }

    private fun initializeHome() {

    }

    companion object {
        private var isAnimated = false
    }
}