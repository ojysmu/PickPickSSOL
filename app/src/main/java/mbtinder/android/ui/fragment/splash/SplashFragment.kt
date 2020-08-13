package mbtinder.android.ui.fragment.splash

import android.animation.ObjectAnimator
import android.view.View
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.fragment_splash.*
import mbtinder.android.R
import mbtinder.android.component.StaticComponent
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.*

class SplashFragment : Fragment(R.layout.fragment_splash) {
    override fun initializeView() {
        requireActivity().findViewById<BottomNavigationView>(R.id.nav_view).visibility = View.GONE

        if (hasAccountInfo()) {
            val (email, password) = getAccountInfo()
            StaticComponent.signIn(this, email, password) {
                Toast.makeText(requireContext(), R.string.splash_failed_to_sign_in, Toast.LENGTH_SHORT).show()
                runOnBackground { removeAccountInfo(); loadAnimation() }
            }
        } else {
            runOnBackground { loadAnimation() }
        }
    }

    private fun loadAnimation() {
        Thread.sleep(1000)
        val animationDuration = if (isAnimated) {
            0L
        } else {
            500L
        }

        runOnUiThread {
            ObjectAnimator.ofFloat(splash_logo, "translationY", -1000f).apply {
                duration = animationDuration
                Log.v("duration=$duration")
            }.start()

            runOnBackground {
                Thread.sleep(animationDuration)
                isAnimated = true
                Log.v("true")
                runOnUiThread(this::initializeSignUp)
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

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (LocationUtil.isLocationPermissionGranted(requestCode, grantResults)) {
            val coordinator = LocationUtil.onLocationPermissionGranted(requireContext())
            Log.v("StaticComponent.signIn(): coordinator=$coordinator")
        }
    }

    companion object {
        private var isAnimated = false
    }
}