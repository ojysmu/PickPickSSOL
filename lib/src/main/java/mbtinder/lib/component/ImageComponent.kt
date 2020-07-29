package mbtinder.lib.component

interface ImageComponent {
    fun hasImage(): Boolean

    fun getImage(): ByteArray

    fun getImageName(): String

    fun getImageUrl(): String

    fun setImage(image: ByteArray)
}