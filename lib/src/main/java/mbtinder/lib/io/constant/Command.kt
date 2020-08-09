package mbtinder.lib.io.constant

enum class Command {
    // Common
    /**
     * Common command.
     * Close a connection.
     */
    CLOSE,

    // 사용자 또는 사용자 정보 관련 명령
    CHECK_EMAIL_DUPLICATED, // 이메일 중복 확인
    ADD_USER,               // 사용자 추가(=회원가입)
    GET_USER,               // 사용자 탐색
    UPDATE_USER,            // 사용자 DB 정보 갱신
    DELETE_USER,            // 사용자 삭제(=회원탈퇴)
    GET_USER_IMAGES,        // 사용자 이미지 ID 목록 탐색
    DELETE_USER_IMAGE,      // 사용자 이미지 삭제
    GET_SIGN_UP_QUESTIONS,  // 회원가입 질문 목록 탐색
    SET_SIGN_UP_QUESTIONS,  // 회원가입 질문 답변 설정
    SET_MBTI,               // MBTI 결과 설정
    SIGN_IN,                // 로그인
    FIND_PASSWORD,          // 비밀번호 찾기
    UPDATE_PASSWORD,        // 비밀번호 변경
    GET_MATCHABLE_USERS,    // 현재 사용자와 매칭 가능한 사용자 목록 탐색(=카드스택 업데이트)
    PICK,                   // PICK 누적

    // Message related commands
    /**
     *
     */
    CREATE_CHAT,

    /**
     *
     */
    DELETE_CHAT,

    GET_MESSAGES,
    /**
     * Client command.
     * Send a message to server.
     */
    SEND_MESSAGE_TO_SERVER;

    companion object {
        private val commands = values()

        fun findCommand(name: String) = commands.first { it.name == name }
    }
}