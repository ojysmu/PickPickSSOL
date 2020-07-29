package mbtinder.android.io.component

data class ServerResult<T>(var isSucceed: Boolean, var code: Int, var result: T?) {
    constructor(isSucceed: Boolean): this(
        isSucceed = isSucceed,
        code = 0,
        result = null
    )

    constructor(isSucceed: Boolean, code: Int): this(
        isSucceed = isSucceed,
        code = code,
        result = null
    )
}