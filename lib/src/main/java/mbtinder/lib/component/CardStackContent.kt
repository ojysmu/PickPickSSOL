package mbtinder.lib.component

import java.util.*

class CardStackContent: IDContent, ImageComponent {
    lateinit var userId: UUID
    lateinit var name: String
    var age = 0
    lateinit var content: String

    private var image: ByteArray? = null
    private lateinit var imageName: String
    private lateinit var imageUrl: String

    constructor(userId: UUID, name: String, age: Int, content: String, imageName: String, imageUrl: String) {
        this.userId = userId
        this.name = name
        this.age = age
        this.content = content
        this.imageName = imageName
        this.imageUrl = imageUrl
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