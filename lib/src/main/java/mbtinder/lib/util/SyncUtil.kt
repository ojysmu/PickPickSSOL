package mbtinder.lib.util

/**
 * synchronized wrapper. lock을 it으로 받을 수 없는 기존 [synchronized]를 wrap
 *
 * @param lock: 단일 접근할 객체
 * @param block: [synchronized]에서 실행할 블록
 */
fun <L: Any, R> sync(lock: L, block: (lock: L) -> R): R {
    return synchronized(lock) { block.invoke(lock) }
}

class BlockWrapper<T>(var variable: T? = null)

/**
 * flag가 false가 될 때까지 대기
 *
 * @param variable: 대기하는 대상 non-null 객체
 * @param duration: 대기 시 sleep 시간(ms)
 * @param flag: 상태를 비교할 함수
 */
fun <T> block(variable: T, duration: Long = 100, flag: (T) -> Boolean): T {
    while (flag.invoke(variable)) {
        Thread.sleep(duration)
    }

    return variable
}

/**
 * flag가 false가 될 때까지 대기
 *
 * @param wrapper: 대기하는 대상 nullable 객체
 * @param duration: 대기 시 sleep 시간(ms)
 * @param flag: 상태를 비교할 함수
 */
fun <T> blockNull(wrapper: BlockWrapper<T>, duration: Long = 100, flag: (T?) -> Boolean): T? {
    while (flag.invoke(wrapper.variable)) {
        Thread.sleep(duration)
    }

    return wrapper.variable
}