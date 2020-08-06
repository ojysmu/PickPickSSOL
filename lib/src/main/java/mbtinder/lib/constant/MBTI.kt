package mbtinder.lib.constant

enum class MBTI(private val map: Array<Int>) {
    INFP(arrayOf(4, 4, 4, 3, 4, 3, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0)),
    ENFP(arrayOf(4, 4, 3, 4, 3, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0)),
    INFJ(arrayOf(4, 3, 4, 4, 4, 4, 4, 4, 3, 0, 0, 0, 0, 0, 0, 0)),
    ENFJ(arrayOf(3, 4, 4, 4, 4, 4, 4, 4, 4, 3, 0, 0, 0, 0, 0, 0)),
    INTJ(arrayOf(4, 3, 4, 4, 4, 4, 4, 3, 2, 2, 2, 2, 1, 1, 1, 1)),
    ENTJ(arrayOf(3, 4, 4, 4, 4, 4, 3, 4, 2, 2, 2, 2, 2, 2, 2, 2)),
    INTP(arrayOf(4, 4, 4, 4, 4, 3, 4, 4, 2, 2, 2, 2, 1, 1, 1, 3)),
    ENTP(arrayOf(4, 4, 3, 4, 3, 4, 4, 4, 2, 2, 2, 2, 1, 1, 1, 1)),
    ISFP(arrayOf(0, 0, 0, 3, 2, 2, 2, 2, 1, 1, 1, 1, 2, 3, 2, 3)),
    ESFP(arrayOf(0, 0, 0, 0, 2, 2, 2, 2, 1, 1, 1, 1, 3, 2, 3, 2)),
    ISTP(arrayOf(0, 0, 0, 0, 2, 2, 2, 2, 1, 1, 1, 1, 2, 3, 2, 3)),
    ESTP(arrayOf(0, 0, 0, 0, 2, 2, 2, 2, 1, 1, 1, 1, 3, 2, 3, 2)),
    ISFJ(arrayOf(0, 0, 0, 0, 1, 2, 1, 1, 2, 3, 2, 3, 4, 4, 4, 4)),
    ESFJ(arrayOf(0, 0, 0, 0, 1, 2, 1, 1, 3, 2, 3, 2, 4, 4, 4, 4)),
    ISTJ(arrayOf(0, 0, 0, 0, 1, 2, 1, 1, 2, 3, 2, 3, 4, 4, 4, 4)),
    ESTJ(arrayOf(0, 0, 0, 0, 1, 2, 3, 1, 3, 2, 3, 2, 4, 4, 4, 4));

    fun getState(mbti: MBTI) = map[mbti.ordinal]

    companion object {
        fun findByName(name: String) = values().find { it.name == name.toUpperCase() }!!
    }
}