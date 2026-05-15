package com.example.koshailagbe.fragment

import android.content.Context
import com.google.firebase.Timestamp
import org.json.JSONArray
import org.json.JSONObject

/**
 * Holds registration data in memory AND SharedPreferences so it survives
 * Android killing the process while the user is in their email app.
 * Data is written to Firestore ONLY after email verification is confirmed.
 */
object PendingRegistration {
    const val ROLE_USER   = "user"
    const val ROLE_KOSHAI = "koshai"

    var role: String = ""
    var data: HashMap<String, Any> = hashMapOf()

    private const val PREFS_NAME = "pending_registration"
    private const val KEY_ROLE   = "role"
    private const val KEY_DATA   = "data"

    /** Call after populating role and data to persist across process restarts. */
    fun save(context: Context) {
        val json = JSONObject()
        for ((key, value) in data) {
            when (value) {
                is String  -> json.put(key, value)
                is Int     -> json.put(key, value)
                is Double  -> json.put(key, value)
                is Boolean -> json.put(key, value)
                is List<*> -> json.put(key, JSONArray(value))
                // Timestamps are skipped — regenerated on load
            }
        }
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_ROLE, role)
            .putString(KEY_DATA, json.toString())
            .apply()
    }

    /** Returns true if data was successfully restored from SharedPreferences. */
    fun loadIfEmpty(context: Context): Boolean {
        if (isReady()) return true  // Already in memory

        val prefs      = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedRole  = prefs.getString(KEY_ROLE, "") ?: ""
        val savedData  = prefs.getString(KEY_DATA, null) ?: return false
        if (savedRole.isEmpty()) return false

        role = savedRole
        data = hashMapOf()

        val json = JSONObject(savedData)
        for (key in json.keys()) {
            when (val v = json.get(key)) {
                is JSONArray -> {
                    val list = mutableListOf<String>()
                    for (i in 0 until v.length()) list.add(v.getString(i))
                    data[key] = list
                }
                else -> data[key] = v
            }
        }
        // Re-inject Timestamp fields that can't be serialized
        data["createdAt"] = Timestamp.now()
        if (role == ROLE_KOSHAI) data["locationUpdatedAt"] = Timestamp.now()

        return true
    }

    fun clear(context: Context? = null) {
        role = ""
        data = hashMapOf()
        context?.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            ?.edit()?.clear()?.apply()
    }

    fun isReady() = role.isNotEmpty() && data.isNotEmpty()
}
