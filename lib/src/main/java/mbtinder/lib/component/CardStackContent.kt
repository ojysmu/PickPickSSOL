package mbtinder.lib.component

import mbtinder.lib.annotation.SkipParsing
import mbtinder.lib.component.json.JSONParsable
import mbtinder.lib.component.user.Coordinator
import mbtinder.lib.component.user.SignUpQuestionContent
import mbtinder.lib.component.user.UserContent
import mbtinder.lib.constant.MBTI
import mbtinder.lib.constant.ServerPath
import mbtinder.lib.util.JSONList
import org.json.JSONObject
import java.util.*

class CardStackContent: JSONParsable, IDContent, ImageComponent, CloneableContent<CardStackContent>, Comparable<CardStackContent> {
    lateinit var userId: UUID
    lateinit var userName: String
    var age: Int = -1
    var gender: Int = -1
    lateinit var coordinator: Coordinator
    lateinit var description: String
    lateinit var mbti: MBTI
    lateinit var contents: JSONList<SignUpQuestionContent>
    var score: Int = -1

    @SkipParsing
    private var image: ByteArray? = null
    @SkipParsing
    private var imageName: String
    @SkipParsing
    private var imageUrl: String

    constructor(jsonObject: JSONObject): super(jsonObject) {
        println("CardStackContent(): $jsonObject")
        this.imageName = "profile.png"
        this.imageUrl = ServerPath.getUserImageUrl(userId, imageName)
    }

    constructor(userContent: UserContent, mbti: MBTI, contents: JSONList<SignUpQuestionContent>, score: Int = 0) {
        println("CardStackContent(): 1")
        this.userId = userContent.userId
        this.userName = userContent.name
        this.age = userContent.age
        this.description = userContent.description
        this.mbti = mbti
        this.contents = contents
        this.imageName = "profile.png"
        this.imageUrl = ServerPath.getUserImageUrl(userId, imageName)
        this.score = score

        updateJSONObject()
    }

    constructor(userId: UUID, userName: String, age: Int, gender: Int, coordinator: Coordinator, description: String,
                mbti: MBTI, contents: JSONList<SignUpQuestionContent>, score: Int = 0) {
        println("CardStackContent(): 2")
        this.userId = userId
        this.userName = userName
        this.age = age
        this.gender = gender
        this.coordinator = coordinator
        this.description = description
        this.mbti = mbti
        this.contents = contents
        this.imageName = "profile.png"
        this.imageUrl = ServerPath.getUserImageUrl(userId, imageName)
        this.score = score

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

    override fun getCloned() = CardStackContent(
        userId = userId,
        userName = userName,
        age = age,
        gender = gender,
        coordinator = coordinator,
        description = description,
        mbti = mbti,
        contents = contents,
        score = score
    )

    override fun compareTo(other: CardStackContent) = userId.compareTo(other.userId)

    companion object {
        fun getSelectAllSql() =
            "SELECT user_id, name, age, gender, last_location_lng, last_location_lat, description FROM pickpick.user"
    }
}