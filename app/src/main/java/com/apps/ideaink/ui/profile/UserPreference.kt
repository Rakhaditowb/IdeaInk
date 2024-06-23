package com.example.PPAB10

import android.content.Context

class UserPreference(context: Context) {
    companion object {
        private const val PREFS_NAME = "user_set"
        private const val NAME = "name"
        private const val EMAIL = "email"
        private const val QUOTE = "quote"
        private const val IMG = "img"
        private const val IS_DARK_MODE = "isDarkMode"
    }
    private val preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    fun setUser(value: UserModel) {
        val editor = preferences.edit()
        editor.putString(IMG, value.img)
        editor.putString(NAME, value.name)
        editor.putString(EMAIL, value.email)
        editor.putString(QUOTE, value.quote)
        editor.putBoolean(IS_DARK_MODE, value.theme ?: false)
        editor.apply()
    }
    fun getUser(): UserModel {
        val model = UserModel()
        model.img = preferences.getString(IMG, null)
        model.name = preferences.getString(NAME, "")
        model.email = preferences.getString(EMAIL, "")
        model.quote = preferences.getString(QUOTE, "")
        model.theme = preferences.getBoolean(IS_DARK_MODE, false)
        return model
    }
}