package mbtinder.lib.component

import mbtinder.lib.component.json.JSONParsable
import mbtinder.lib.constant.MBTI
import mbtinder.lib.util.JSONList
import org.json.JSONObject

class UserDetailContent: JSONParsable, IDContent, Comparable<UserDetailContent>, CloneableContent<UserDetailContent> {
    lateinit var userContent: UserContent
    lateinit var mbti: MBTI
    lateinit var signUpQuestionContents: JSONList<SignUpQuestionContent>

    constructor(jsonObject: JSONObject): super(jsonObject)

    constructor(userContent: UserContent, mbti: MBTI, signUpQuestionContents: JSONList<SignUpQuestionContent>) {
        this.userContent = userContent
        this.mbti = mbti
        this.signUpQuestionContents = signUpQuestionContents

        updateJSONObject()
    }

    override fun getUUID() = userContent.getUUID()

    override fun compareTo(other: UserDetailContent) = getUUID().compareTo(other.getUUID())

    override fun getCloned() = UserDetailContent(userContent, mbti, signUpQuestionContents)
}