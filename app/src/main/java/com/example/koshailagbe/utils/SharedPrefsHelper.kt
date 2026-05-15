package com.example.koshailagbe.utils

import android.content.Context
import android.content.SharedPreferences

object SharedPrefsHelper {
    private const val PREFS_NAME = "KoshaiPrefs"
    private const val KEY_USER_ROLE = "user_role"

    const val ROLE_USER = "user"
    const val ROLE_KOSHAI = "koshai"
    const val ROLE_ADMIN = "admin"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveUserRole(context: Context, role: String) {
        getPrefs(context).edit().putString(KEY_USER_ROLE, role).apply()
    }

    fun getUserRole(context: Context): String? {
        return getPrefs(context).getString(KEY_USER_ROLE, null)
    }

    fun clearUserRole(context: Context) {
        getPrefs(context).edit().remove(KEY_USER_ROLE).apply()
    }
}
