package mbtinder.lib.io.constant

enum class Command {
    // Common
    /**
     * Common command.
     * Close a connection.
     */
    CLOSE,

    // User related commands
    CHECK_EMAIL_DUPLICATED,

    /**
     * Client command.
     * Add user data to server.
     * ADD_USER might not contain image.
     */
    ADD_USER,

    /**
     * Client command.
     * Get user data from server. Should return all images ID.
     */
    GET_USER,

    /**
     * Client command.
     * Update user data to server excluding image.
     */
    UPDATE_USER,

    /**
     * Client command.
     * Delete user from server.
     */
    DELETE_USER,

    GET_USER_IMAGES,

    DELETE_USER_IMAGE,

    GET_SIGN_UP_QUESTIONS,

    SET_SIGN_UP_QUESTIONS,

    /**
     * Client command.
     * Sign in with email, password.
     * Password should be encrypted with RSA-1024.
     */
    SIGN_IN,

    FIND_PASSWORD,
    UPDATE_PASSWORD,

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