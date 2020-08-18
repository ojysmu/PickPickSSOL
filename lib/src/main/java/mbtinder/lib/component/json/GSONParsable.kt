package mbtinder.lib.component.json

import com.google.gson.Gson

abstract class GSONParsable<T> : GSONContent {
    protected var parsed: T

    constructor(clazz: Class<T>) {
        this.parsed = Gson().fromJson("{}", clazz)
    }

    constructor(jsonString: String, clazz: Class<T>) {
        this.parsed = Gson().fromJson(jsonString, clazz)
    }

    fun toJSONString() = Gson().toJson(this)!!
}