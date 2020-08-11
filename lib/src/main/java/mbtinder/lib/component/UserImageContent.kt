package mbtinder.lib.component

import mbtinder.lib.component.json.JSONContent
import mbtinder.lib.constant.ServerPath
import org.json.JSONObject
import java.util.*

class UserImageContent: JSONContent, IDContent, ImageComponent {
    var userId: UUID
    var imageId: UUID
    private var image: ByteArray? = null
    private var imageName: String
    private var imageUrl: String

    constructor(userId: UUID) {
        this.userId = userId
        this.imageId = userId
        this.imageName = "profile.png"
        this.imageUrl = ServerPath.getUserImageUrl(userId, imageName)
    }

    constructor(userId: UUID, imageId: UUID, imageName: String, imageUrl: String) {
        this.userId = userId
        this.imageId = imageId
        this.imageName = imageName
        this.imageUrl = imageUrl
    }

    override fun toJSONObject() = JSONObject().apply {
        put("user_id", userId.toString())
        put("image_id", imageId.toString())
        put("image_name", imageName)
        put("image_url", imageUrl)
    }

    override fun getUUID() = imageId

    override fun hasImage() = image != null

    override fun getImage() = image!!

    override fun getImageName() = imageName

    override fun getImageUrl() = imageUrl

    override fun setImage(image: ByteArray) {
        this.image = image
    }
}