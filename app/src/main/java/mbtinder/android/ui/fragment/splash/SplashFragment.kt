package mbtinder.android.ui.fragment.splash

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.fragment_splash.*
import mbtinder.android.R
import mbtinder.android.io.SocketUtil
import mbtinder.android.ui.fragment.home.HomeFragment
import mbtinder.android.ui.fragment.sign_up.SignUpFragment
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.SharedPreferencesUtil
import mbtinder.android.util.ThreadUtil

class SplashFragment : Fragment() {
    override fun initializeView() {
        requireActivity().window.statusBarColor = requireContext().getColor(R.color.colorAccent)
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE

        ThreadUtil.runOnBackground {
            if (hasAccountInfo()) {
                ThreadUtil.runOnBackground {
                    val (email, password) = getAccountInfo()
                    val signInResult = SocketUtil.signIn(email, password)
                    if (signInResult.isSucceed) {
                        findNavController().navigate(R.id.action_to_home)
                    } else {
                        removeAccountInfo()
                        ThreadUtil.runOnUiThread {
                            Toast.makeText(requireContext(), R.string.splash_failed_to_sign_in, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                Thread.sleep(1000)

                ThreadUtil.runOnUiThread {
                    ObjectAnimator.ofFloat(splash_logo, "translationY", -1000f).apply {
                        duration = 500
                        start()
                    }

                    ThreadUtil.runOnBackground {
                        Thread.sleep(500)
                        ThreadUtil.runOnUiThread(this::initializeSignUp)
                    }
                }
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflateView(R.layout.fragment_splash, inflater, container)
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
}