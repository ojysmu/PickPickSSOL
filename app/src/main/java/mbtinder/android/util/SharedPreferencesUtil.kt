package mbtinder.android.util

import android.content.Context

object SharedPreferencesUtil {
    const val PREF_DEFAULT = "default"
    const val PREF_ACCOUNT = "account"

    fun getContext(context: Context, prefName: String) = PreferencesContext(context, prefName)

    class PreferencesContext(val context: Context, private val prefName: String) {
        private val preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        private val editor = preferences.edit()

        fun put(key: String, value: String): PreferencesContext {
            editor.putString(key, value)
            editor.apply()

            return this
        }

        @JvmName("putStrings")
        fun put(pairs: Map<String, String>): PreferencesContext {
            pairs.forEach { (key, value) -> editor.putString(key, value) }
            editor.apply()

            return this
        }

        fun put(key: String, value: Boolean): PreferencesContext {
            editor.putBoolean(key, value)
            editor.apply()

            return this
        }

        fun put(key: String, value: Int): PreferencesContext {
            editor.putInt(key, value)
            editor.apply()

            return this
        }

        @JvmName("putInts")
        fun put(pairs: Map<String, Int>): PreferencesContext {
            pairs.forEach { (key, value) -> editor.putInt(key, value) }
            editor.apply()

            return this
        }

        fun put(key: String, value: Long): PreferencesContext {
            editor.putLong(key, value)
            editor.apply()

            return this
        }

        fun put(key: String, value: Float): PreferencesContext {
            editor.putFloat(key, value)
            editor.apply()

            return this
        }

        fun put(key: String, value: Set<String>): PreferencesContext {
            editor.putStringSet(key, value)
            editor.apply()

            return this
        }

        fun put(key: String, value: List<String>): PreferencesContext {
            editor.putStringSet(key, value.mapTo(HashSet()) { s -> s })
            editor.apply()

            return this
        }

        fun getString(key: String): String? {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getString(key, null)
        }

        fun getBoolean(key: String): Boolean {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getBoolean(key, false)
        }

        fun getInt(key: String): Int {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getInt(key, 0)
        }

        fun getLong(key: String): Long {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getLong(key, 0)
        }

        fun getFloat(key: String): Float {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getFloat(key, 0f)
        }

        fun getStringSet(key: String): Set<String>? {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getStringSet(key, null)
        }

        fun getStringList(key: String): List<String>? {
            return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getStringSet(key, null)?.mapTo(ArrayList()) { s -> s }
        }

        fun removePreference(): MutableMap<String, *> {
            val allFields = preferences.all
            for (field in allFields) {
                editor.remove(field.key)
            }
            editor.apply()

            return allFields
        }

        fun removeField(key: String): PreferencesContext {
            editor.remove(key)
            editor.apply()

            return this
        }
    }

    fun put(context: Context, prefName: String, key: String, value: String) {
        val preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putString(key, value)
        editor.apply()
    }

    @JvmName("putStrings")
    fun put(context: Context, prefName: String, pairs: Map<String, String>) {
        val preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        pairs.forEach { (key, value) -> editor.putString(key, value) }
        editor.apply()
    }

    fun put(context: Context, prefName: String, key: String, value: Boolean) {
        val preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putBoolean(key, value)
        editor.apply()
    }

    fun put(context: Context, prefName: String, key: String, value: Int) {
        val preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putInt(key, value)
        editor.apply()
    }

    @JvmName("putInts")
    fun put(context: Context, prefName: String, pairs: Map<String, Int>) {
        val preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        pairs.forEach { (key, value) -> editor.putInt(key, value) }
        editor.apply()
    }

    fun put(context: Context, prefName: String, key: String, value: Long) {
        val preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putLong(key, value)
        editor.apply()
    }

    fun put(context: Context, prefName: String, key: String, value: Float) {
        val preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putFloat(key, value)
        editor.apply()
    }

    fun put(context: Context, prefName: String, key: String, value: Set<String>) {
        val preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putStringSet(key, value)
        editor.apply()
    }

    fun put(context: Context, prefName: String, key: String, value: List<String>) {
        val preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.putStringSet(key, value.mapTo(HashSet()) { s -> s })
        editor.apply()
    }

    fun getString(context: Context, prefName: String, key: String): String? {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getString(key, null)
    }

    fun getBoolean(context: Context, prefName: String, key: String): Boolean {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getBoolean(key, false)
    }

    fun getInt(context: Context, prefName: String, key: String): Int {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getInt(key, 0)
    }

    fun getLong(context: Context, prefName: String, key: String): Long {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getLong(key, 0)
    }

    fun getFloat(context: Context, prefName: String, key: String): Float {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getFloat(key, 0f)
    }

    fun getStringSet(context: Context, prefName: String, key: String): Set<String>? {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getStringSet(key, null)
    }

    fun getStringList(context: Context, prefName: String, key: String): List<String>? {
        return context.getSharedPreferences(prefName, Context.MODE_PRIVATE).getStringSet(key, null)?.mapTo(ArrayList()) { s -> s }
    }

    fun removePreference(context: Context, prefName: String): MutableMap<String, *> {
        val sharedPreferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        val allFields = sharedPreferences.all
        for (field in allFields) {
            editor.remove(field.key)
        }
        editor.apply()

        return allFields
    }

    fun removeField(context: Context, prefName: String, key: String) {
        val preferences = context.getSharedPreferences(prefName, Context.MODE_PRIVATE)
        val editor = preferences.edit()
        editor.remove(key)
        editor.apply()
    }
}