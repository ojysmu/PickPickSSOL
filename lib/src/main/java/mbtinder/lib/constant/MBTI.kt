package mbtinder.lib.constant

import mbtinder.lib.component.user.MBTIContent

enum class MBTI(private val map: Array<Int>, private val content: String) {
    INFP(arrayOf(4, 4, 4, 3, 4, 3, 4, 4, 0, 0, 0, 0, 0, 0, 0, 0), "열정적인 중재자"),
    ENFP(arrayOf(4, 4, 3, 4, 3, 4, 4, 4, 4, 0, 0, 0, 0, 0, 0, 0), "재기발랄한 활동가"),
    INFJ(arrayOf(4, 3, 4, 4, 4, 4, 4, 4, 3, 0, 0, 0, 0, 0, 0, 0), "선의의 옹호자"),
    ENFJ(arrayOf(3, 4, 4, 4, 4, 4, 4, 4, 4, 3, 0, 0, 0, 0, 0, 0), "정의로운 사회운동가"),
    INTJ(arrayOf(4, 3, 4, 4, 4, 4, 4, 3, 2, 2, 2, 2, 1, 1, 1, 1), "용의주도한 전략가"),
    ENTJ(arrayOf(3, 4, 4, 4, 4, 4, 3, 4, 2, 2, 2, 2, 2, 2, 2, 2), "대담한 통솔자"),
    INTP(arrayOf(4, 4, 4, 4, 4, 3, 4, 4, 2, 2, 2, 2, 1, 1, 1, 3), "논리적인 사색가"),
    ENTP(arrayOf(4, 4, 3, 4, 3, 4, 4, 4, 2, 2, 2, 2, 1, 1, 1, 1), "뜨거운 논쟁을 즐기는 변론가"),
    ISFP(arrayOf(0, 0, 0, 3, 2, 2, 2, 2, 1, 1, 1, 1, 2, 3, 2, 3), "호기심 많은 예술가"),
    ESFP(arrayOf(0, 0, 0, 0, 2, 2, 2, 2, 1, 1, 1, 1, 3, 2, 3, 2), "자유로운 영혼의 연예인"),
    ISTP(arrayOf(0, 0, 0, 0, 2, 2, 2, 2, 1, 1, 1, 1, 2, 3, 2, 3), "만능 재주꾼"),
    ESTP(arrayOf(0, 0, 0, 0, 2, 2, 2, 2, 1, 1, 1, 1, 3, 2, 3, 2), "모험을 즐기는 사업가"),
    ISFJ(arrayOf(0, 0, 0, 0, 1, 2, 1, 1, 2, 3, 2, 3, 4, 4, 4, 4), "용감한 수호자"),
    ESFJ(arrayOf(0, 0, 0, 0, 1, 2, 1, 1, 3, 2, 3, 2, 4, 4, 4, 4), "사교적인 외교관"),
    ISTJ(arrayOf(0, 0, 0, 0, 1, 2, 1, 1, 2, 3, 2, 3, 4, 4, 4, 4), "청렴결백한 논리주의자"),
    ESTJ(arrayOf(0, 0, 0, 0, 1, 2, 3, 1, 3, 2, 3, 2, 4, 4, 4, 4), "엄격한 관리자");

    fun getState(mbti: MBTI) = map[mbti.ordinal]

    companion object {
        fun findByName(name: String) = values().find { it.name == name.toUpperCase() }!!

        fun toMBTIContents() = values().map {
            MBTIContent(
                it.name,
                it.content
            )
        }
    }
}