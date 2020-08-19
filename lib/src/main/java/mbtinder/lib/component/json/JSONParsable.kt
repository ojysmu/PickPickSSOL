package mbtinder.lib.component.json

import mbtinder.lib.annotation.SkipParsing
import mbtinder.lib.constant.MBTI
import mbtinder.lib.util.JSONList
import mbtinder.lib.util.clone
import mbtinder.lib.util.toJSONArray
import org.json.JSONObject
import java.lang.reflect.Field
import java.sql.Date
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.kotlinProperty

abstract class JSONParsable: JSONContent {
    var jsonObject: JSONObject

    constructor(jsonObject: JSONObject) {
        this.jsonObject = jsonObject.clone()
        updateFromJSONObject(this.jsonObject)
    }

    constructor() {
        jsonObject = JSONObject()
    }

    /**
     * [JSONObject]로부터 초기화
     *
     * @author 김지환
     * @param jsonObject: 멤버변수를 초기화할 JSONObject. 필드명이 언더스코어 형식이고, 멤버변수는 camel case여야 한다.
     */
    private fun updateFromJSONObject(jsonObject: JSONObject) {
        // 변수를 초기화할 JSONObject Key
        val keys = jsonObject.keySet()
        // JSONParsable을 상속받는 class의 선언된 필드 목록
        val fields = this::class.java.declaredFields
        for (field in fields) {
            // 필드 접근 활성화
            field.isAccessible = true
            // SkipParsing annotation 있을 떄 파싱하지 않음
            if (containsSkipParsing(field)) {
                continue
            }
            // 변수 이름을 언더스코어 형식으로 변환
            val formatted = toUnderscore(field.name)
            for (key in keys) {
                if (key == formatted || toUnderscore(key) == formatted) {
                    if (field.type.isArray) {
                        continue
                    }

                    if (field.type.superclass == JSONParsable::class.java) {
                        // JSONParsable content
                        val value = jsonObject.getJSONObject(key)
                        val instance = field.type.getDeclaredConstructor(JSONObject::class.java).newInstance(value)
                        field.set(this, instance)
                        continue
                    }

                    parseFromType(field, jsonObject, key)
                    break
                }
            }
        }
    }

    /**
     * 멤버변수값으로 jsonObject 내용 업데이트
     * 필드명이 언더스코어 형식이고, 멤버변수는 camel case여야 한다.
     *
     * @author 김지환
     */
    final override fun updateJSONObject() {
        val fields = this::class.java.declaredFields
        for (field in fields) {
            field.isAccessible = true

            if (containsSkipParsing(field)) {
                // SkipParsing annotation 무시
                continue
            }

            if (field.type.isArray) {
                // 배열 무시
                continue
            }

            parseFromField(field, toUnderscore(field.name), jsonObject)
        }
    }

    private fun parseFromType(field: Field, jsonObject: JSONObject, key: String) {
        when (field.type) {
            // UUID, Date, JSONList의 경우 별도 wrapping 필요
            Byte::class.java, Short::class.java, Int::class.java -> field.set(this, jsonObject.getInt(key))
            Long::class.java -> field.set(this, jsonObject.getLong(key))
            Float::class.java -> field.set(this, jsonObject.getFloat(key))
            Double::class.java -> field.set(this, jsonObject.getDouble(key))
            UUID::class.java -> field.set(this, UUID.fromString(jsonObject.getString(key)))
            Date::class.java -> field.set(this, Date.valueOf(jsonObject.getString(key)))
            MBTI::class.java -> field.set(this, MBTI.findByName(jsonObject.getString(key)))
            JSONList::class.java -> {
                // key와 변환된 변수 이름이 같을 때
                // Generic 타입을 사용하므로 변수의 제너릭 타입을 가져옴
                // 제너릭 타입으로 생성한 Class
                // ex) informedClass: class com.special_uridongne.jupeed.component.User
                val informedClass = Class.forName(getGenericName(field.genericType.typeName))
                // JSONParsable을 상속받음을 명시
                // T: JSONParsable에 대한 T로 사용할 수 있음
                val genericClass = informedClass.asSubclass(JSONContent::class.java)
                field.set(this, JSONList(jsonObject.getJSONArray(key), genericClass))
            }
            List::class.java -> {
                // List 무시
            }
            Companion::class.java -> {
                // Companion 무시
            }
            JSONObject::class.java -> field.set(this, jsonObject.getJSONObject(key))
            // 이외에는 String, Int 등 get-set 가능한 타입들
            else -> field.set(this, jsonObject.get(key))
        }
    }

    private fun parseFromField(field: Field, key: String, jsonObject: JSONObject) {
        when (val value = field.get(this)) {
            is UUID -> jsonObject.put(key, value.toString())
            is Date -> jsonObject.put(key, value.toString())
            is MBTI -> jsonObject.put(key, value.name)
            is JSONList<*> -> {
                // JSONList<*>는 element.updateJSONObject() 실행 후 추가
                value.updateJSONObject()
                // toJSONArray()라는 별도의 함수 사용
                jsonObject.put(key, value.toJSONArray())
            }
            is List<*> -> jsonObject.put(key, toJSONArray())
            is Companion -> {
                // Companion 무시
            }
            is JSONParsable -> jsonObject.put(key, value.toJSONObject())
            is JSONContent -> jsonObject.put(key, value.toJSONObject())
            else -> jsonObject.put(key, value)
        }
    }

    /**
     * 현재 필드의 값으로 업데이트된 JSONObject 반환
     *
     * @author 김지환
     * @return 새 JSONObject
     */
    override fun toJSONObject(): JSONObject {
        updateJSONObject()
        return jsonObject
    }

    /**
     * [JSONObject.toString]으로 [Any.toString] 대체
     *
     * @author 김지환
     * @return [JSONObject.toString]
     */
    override fun toString(): String {
        return jsonObject.toString()
    }

    companion object {
        /**
         * 클래스 이름에서 Generic을 분리
         *
         * @author 김지환
         * @param type: Generic을 사용하는 클래스 이름 (com.special_uridongne_jupeed.model<com.special_uridongne.jupeed.component.User>)
         * @return Generic 클래스 이름 (com.special_uridongne.jupeed.component.User)
         */
        private fun getGenericName(type: String): String {
            val start = type.indexOf('<')
            return if (start == -1) {
                type
            } else {
                type.substring(start + 1, type.indexOf('>'))
            }
        }

        /**
         * Camel case 문자열을 언더스코어 문자열로 변환
         * testVariableName -> test_variable_name
         *
         * @author 김지환
         * @param camelCase: Camel case 문자열
         * @return 언더스코어로 변환된 문자열
         */
        private fun toUnderscore(camelCase: String): String {
            val stringBuilder = StringBuilder()
            val list = ArrayList<Char>()
            camelCase.forEach { list.add(it) }

            var index = 0
            while (index < list.size) {
                val c = list[index]
                if (c.isUpperCase()) {
                    list.add(index, '_')
                    list[index + 1] = c.toLowerCase()
                }

                if (c == ' ') {
                    list.removeAt(index)
                    index--
                }

                index++
            }

            for (c in list) {
                stringBuilder.append(c)
            }

            if (stringBuilder[0] == '_') {
                stringBuilder.delete(0, 1)
            }

            return stringBuilder.toString()
        }

        private fun containsSkipParsing(field: Field) =
            field.annotations.map { it.annotationClass }.contains(SkipParsing::class)
    }
}