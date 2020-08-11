package mbtinder.android.component

import mbtinder.android.util.ImageDownloader
import mbtinder.lib.component.UserContent
import mbtinder.lib.component.UserImageContent
import mbtinder.lib.util.IDList
import java.util.*

object StaticComponent {
    lateinit var user: UserContent

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