package mbtinder.android.component

import androidx.navigation.fragment.findNavController
import mbtinder.android.R
import mbtinder.android.io.database.SQLiteConnection
import mbtinder.android.io.http.ImageDownloader
import mbtinder.android.io.http.SQLiteDownloader
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.util.LocationUtil
import mbtinder.android.util.Log
import mbtinder.android.util.runOnBackground
import mbtinder.android.util.runOnUiThread
import mbtinder.lib.component.user.UserContent
import mbtinder.lib.component.user.UserImageContent
import mbtinder.lib.util.IDList
import java.util.*

object StaticComponent {
    lateinit var user: UserContent

    fun signIn(fragment: Fragment, email: String, password: String, onFailed: (() -> Unit)?) =
        runOnBackground<Boolean> {
            val signInResult = CommandProcess.signIn(email, password)
            if (signInResult.isSucceed) {
                user = signInResult.result!!
                SQLiteDownloader(user.userId, fragment.requireContext().filesDir.toString())
                SQLiteConnection.createInstance(fragment.requireContext().filesDir.toString())

                if (LocationUtil.checkLocationPermission(fragment.requireContext())) {
                    runOnBackground {
                        val coordinator = LocationUtil.onLocationPermissionGranted(fragment.requireContext())
                        Log.v("StaticComponent.signIn(): coordinator=$coordinator")
                        CommandProcess.setCoordinator(user.userId, coordinator)
                    }
                } else {
                    LocationUtil.requestLocationPermission(fragment.requireActivity())
                }

                fragment.findNavController().navigate(R.id.action_to_home)
            } else {
                runOnUiThread { onFailed?.invoke() }
            }

            signInResult.isSucceed
        }

    private val userImages = IDList<UserImageContent>()

    fun getUserImage(userId: UUID): UserImageContent {
        val found = userImages.find { it.userId == userId }
        return if (found == null) {
            val newImage = UserImageContent(userId)
            ImageDownloader(newImage).execute().get()
            userImages.add(newImage)

            newImage
        } else {
            found
        }
    }
}