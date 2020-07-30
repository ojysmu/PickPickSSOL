package mbtinder.lib.constant

enum class PasswordQuestion(val questionId: Int, val question: String) {
    QUESTION1(0, "기억에 남는 추억의 장소는?"),
    QUESTION2(1, "오래 기억하고 싶은 날짜는?"),
    QUESTION3(2, "다시 태어나면 되고 싶은 것은?"),
    QUESTION4(3, "제일 좋아하는 캐릭터 이름은?"),
    QUESTION5(4, "학창 시절 나의 꿈은?");

    companion object {
        private val questions = values()

        fun findQuestion(questionId: Int) = questions.find { it.questionId == questionId }

        fun findQuestion(question: String) = questions.find { it.question == question }
    }
}