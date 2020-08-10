package mbtinder.lib.util

import kotlin.reflect.KFunction

/**
 * synchronized wrapper. lock을 it으로 받을 수 없는 기존 [synchronized]를 wrap
 *
 * @param lock: 단일 접근할 객체
 * @param block: [synchronized]에서 실행할 블록
 */
fun <L: Any, R> sync(lock: L, block: (lock: L) -> R): R {
    return synchronized(lock) { block.invoke(lock) }
}

/**
 * synchronized wrapper. 단일 함수를 실행하는 목적으로 사용되는 [synchronized]를 wrap
 *
 * @param lock: 단일 접근할 객체
 * @param function: 객체가 실행할 함수
 * @param args: 함수의 인자
 */
//fun <L: Any, R> sync(lock: L, function: KFunction<R>, vararg args: Any): R {
//    return synchronized(lock) { function.call(args[0]) }
//}

/**
 * flag가 false가 될 때까지 대기
 *
 * @param variable: 대기하는 대상 객체
 * @param duration: 대기 시 sleep 시간(ms)
 * @param flag: 상태를 비교할 함수
 */
fun <T> block(variable: T, duration: Long, flag: (v: T) -> Boolean): T {
    while (flag.invoke(variable)) {
        Thread.sleep(duration)
    }

    return variable
}