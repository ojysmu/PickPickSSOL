package mbtinder.server.io.database

import mbtinder.lib.component.database.Query
import mbtinder.lib.component.database.QueryResult
import mbtinder.lib.util.CloseableThread
import mbtinder.lib.util.IDList
import mbtinder.lib.util.block
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException
import java.sql.Statement
import java.util.*

/**
 * MySQL 쿼리를 대기하는 Thread
 *
 * @param url: DB 호스트
 * @param database: 데이터베이스 명 (we_share_ad 등)
 * @param id: DB 계정 이름
 * @param password: DB 계정 비밀번호
 * @constructor MySQL 쿼리를 대기하는 서버 생성
 */
class MySQLServer private constructor(url: String, database: String, id: String, password: String): CloseableThread() {
    companion object {
        /**
         * Singleton 객체 유지를 위한 instance
         */
        private var instance: MySQLServer? = null

        /**
         * 이미 생성된 인스턴스를 호출하기 위한 method
         *
         * @throws RuntimeException: 인스턴스가 생성되지 않았을 때 예외 발생 [createInstance]
         * @return 실행중인 인스턴스
         */
        fun getInstance(): MySQLServer {
            if (instance == null) {
                throw RuntimeException("Instance never initialized")
            }

            return instance!!
        }

        /**
         * 1회에 한해 인스턴스를 생성하기 위한 method
         *
         * @param url: DB 호스트
         * @param database: 데이터베이스 명 (we_share_ad 등)
         * @param id: DB 계정 이름
         * @param password: DB 계정 비밀번호
         * @throws RuntimeException: 이미 인스턴스가 생성되었을 때 예외 발생
         * @return 새로 생성된 instance
         */
        fun createInstance(url: String, database: String, id: String, password: String): MySQLServer {
            if (instance == null) {
                instance = MySQLServer(url, database, id, password)

                return instance!!
            } else {
                throw RuntimeException("Instance already initialized")
            }
        }

        private fun getConnection(url: String, database: String, id: String, password: String): Connection {
            Class.forName("com.mysql.cj.jdbc.Driver")

            return DriverManager.getConnection("jdbc:mysql://$url/$database?useLegacyDatetimeCode=false&serverTimezone=Asia/Seoul&useSSL=false&autoReconnect=true&allowPublicKeyRetrieval=true", id, password)
        }
    }

    /**
     * SQL 실행과 결과를 관리하는 Statement
     */
    private val statement: Statement

    /**
     * 실행을 대기하는 query pool
     */
    private val queries = IDList<Query>()

    /**
     * 결과를 필요로 하는 SQL 명령에 대한 결과가 저장되는 pool
     */
    private val results = IDList<QueryResult>()

    /**
     * 주기적으로 validation query를 전송하기 위한 sleep counter
     */
    private var sleepCounter: Long = 0L

    init {
        val connection = getConnection(url, database, id, password)
        statement = connection.createStatement()

        loop = {
            if (queries.isEmpty()) {
                sleep()
                sleepCounter += intervalInMillis

                if (sleepCounter > 20000000L) {
                    validateQuery()
                    sleepCounter = 0L
                }
            } else {
                sleepCounter = 0L
                val query = queries.removeAt(0)

                if (query.needResult) {
                    try {
                        val resultSet = statement.executeQuery(query.sql)
                        val queryResult = QueryResult(query, resultSet)
                        resultSet.close()
                        results.add(queryResult)
                    } catch (e: SQLException) {
                        System.err.println("Error occurred while running query: ${query.sql}")
                        e.printStackTrace()
                    }
                } else {
                    try {
                        statement.executeUpdate(query.sql)
                    } catch (e: SQLException) {
                        System.err.println("Error occurred while running query: ${query.sql}")
                        e.printStackTrace()
                    }
                }
            }
        }
    }

    /**
     * SQL 명령줄을 대기열의 끝에 삽입
     *
     * @param sql: SQL 명령 (insert, select 등)
     * @return 실행될 SQL의 고유 ID
     */
    fun addQuery(sql: String): UUID {
        val queryId = UUID.randomUUID()
        queries.add(Query(queryId, sql))

        return queryId
    }

    /**
     * 실행한 SQL의 결과를 반환
     * [queryId]에 해당하는 결과가 생길때까지 대기
     *
     * @param queryId: 실행된 SQL의 고유 ID
     * @return 실행한 SQL의 결과
     */
    fun getResult(queryId: UUID): QueryResult {
        block(results, intervalInMillis) { !it.contains(queryId) }

        return results.remove(queryId)
    }

    /**
     * 8시간 이상 쿼리가 없었을 때 Connection이 종료되는 현상 개선을 위한 임시 쿼리
     */
    private fun validateQuery() {
        getResult(addQuery("select 1"))
    }
}