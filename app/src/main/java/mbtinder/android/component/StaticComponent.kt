package mbtinder.android.component

import androidx.annotation.MainThread
import androidx.navigation.fragment.findNavController
import mbtinder.android.R
import mbtinder.android.io.database.SQLiteConnection
import mbtinder.android.io.http.ImageDownloader
import mbtinder.android.io.http.SQLiteDownloader
import mbtinder.android.io.socket.CommandProcess
import mbtinder.android.ui.model.Fragment
import mbtinder.android.ui.view.AsyncImageView
import mbtinder.android.util.*
import mbtinder.lib.component.user.UserContent
import mbtinder.lib.component.user.UserImageContent
import mbtinder.lib.util.IDList
import java.util.*

object StaticComponent {
    lateinit var user: UserContent

    fun signIn(fragment: Fragment, email: String, password: String, @MainThread onFailed: (() -> Unit)?) =
        runOnBackground<Boolean> {
            val signInResult = CommandProcess.signIn(email, password)
            if (signInResult.isSucceed) {
                user = signInResult.result!!
                SharedPreferencesUtil.getContext(fragment.requireContext(), SharedPreferencesUtil.PREF_ACCOUNT).let {
                    it.put("email", email)
                    it.put("password", password)
                }

                val sqlitePath = fragment.requireContext().getDatabasePath("tables.db")
                SQLiteDownloader(user.userId, sqlitePath).execute().get()

                if (LocationUtil.checkLocationPermission(fragment.requireContext())) {
                    runOnBackground {
                        val coordinator = LocationUtil.onLocationPermissionGranted(fragment.requireContext())
                        Log.v("StaticComponent.signIn(): coordinator=$coordinator")
                        user.lastLocationLng = coordinator.longitude
                        user.lastLocationLat = coordinator.latitude
                        CommandProcess.setCoordinator(user.userId, coordinator)

                        runOnUiThread { fragment.findNavController().navigate(R.id.action_to_home) }
                    }
                } else {
                    LocationUtil.requestLocationPermission(fragment)
                }
            } else {
                runOnUiThread { onFailed?.invoke() }
            }

            signInResult.isSucceed
        }

    private val userImages = IDList<UserImageContent>()

    fun setUserImage(userId: UUID, imageView: AsyncImageView) {
        val found = userImages.find { it.userId == userId }
        if (found == null) {
            val newImage = UserImageContent(userId)
            ImageDownloader(newImage, imageView).execute()
            userImages.add(newImage)
        } else {
            imageView.setImage(found)
        }
    }

    fun setUserImage(userId: UUID, rawImage: ByteArray) {
        userImages.find { it.userId == userId }?.setImage(rawImage)
    }
}