package mbtinder.lib.component

import mbtinder.lib.annotation.SkipParsing
import mbtinder.lib.component.json.JSONParsable
import mbtinder.lib.constant.MBTI
import mbtinder.lib.constant.ServerPath
import org.json.JSONObject
import java.util.*

class CardStackContent: JSONParsable, IDContent, ImageComponent {
    lateinit var userId: UUID
    lateinit var mbti: MBTI
    lateinit var contents: List<SignUpQuestionContent.ConnectionForm>

    @SkipParsing
    private var image: ByteArray? = null
    @SkipParsing
    private var imageName: String
    @SkipParsing
    private var imageUrl: String

    constructor(jsonObject: JSONObject): super(jsonObject) {
        this.imageName = "profile.png"
        this.imageUrl = ServerPath.getUserImageUrl(userId, imageName)
    }

    constructor(userContent: UserContent, mbti: MBTI, contents: List<SignUpQuestionContent.ConnectionForm>) {
        this.userId = userContent.userId
        this.mbti = mbti
        this.contents = contents
        this.imageName = "profile.png"
        this.imageUrl = ServerPath.getUserImageUrl(userId, imageName)

        updateJSONObject()
    }

    override fun getUUID() = userId

    override fun hasImage() = image != null

    override fun getImage() = image!!

    override fun getImageName() = imageName

    override fun getImageUrl() = imageUrl

    override fun setImage(image: ByteArray) {
        this.image = image
    }
}