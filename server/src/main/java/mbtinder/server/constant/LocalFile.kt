package mbtinder.server.constant

import mbtinder.lib.component.InterestContent
import mbtinder.lib.util.loadJSONList
import java.util.*

object LocalFile {
    private val fileRoot = "${System.getProperty("user.home")}/raw"
    val userRoot = "$fileRoot/user"

    private const val userInterestList = "interests.json"
    private const val userSignUpQuestionList = "sign_up_questions.json"
    private const val userMBTIList = "mbti.json"

    fun getUserSignUpQuestionPath(userId: UUID) = "$userRoot/$userId/$userSignUpQuestionList"
    fun getUserSignUpQuestionPath(userId: String) = "$userRoot/$userId/$userSignUpQuestionList"
    fun getUserMBTIPath(userId: String) = "$userRoot/$userId/$userMBTIList"
    fun getUserInterestPath(userId: UUID) = "$userRoot/$userId/$userInterestList"
    fun getUserInterestList(userId: UUID) = loadJSONList<InterestContent>(getUserInterestPath(userId))
    fun getUserImagePath(userId: UUID) = "$userRoot/$userId"
}