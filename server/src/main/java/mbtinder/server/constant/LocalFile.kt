package mbtinder.server.constant

import java.util.*

object LocalFile {
    private val fileRoot = "${System.getProperty("user.home")}/raw"
    val userRoot = "$fileRoot/user"

    private const val userInterestList = "interests.json"
    private const val userSignUpQuestionList = "sign_up_questions.json"
    private const val userMBTIList = "mbti.json"

    fun getUserSignUpQuestionPath(userId: UUID) = "$userRoot/$userId/$userSignUpQuestionList"
    fun getUserSignUpQuestionPath(userId: String) = "$userRoot/$userId/$userSignUpQuestionList"
    fun getUserMBTIPath(userId: UUID) = "$userRoot/$userId/$userMBTIList"
    fun getUserMBTIPath(userId: String) = "$userRoot/$userId/$userMBTIList"
}