package com.example.instalgam.repository

import android.content.SharedPreferences
import androidx.core.content.edit

class LogInRepository(
    private val sp: SharedPreferences,
) {
    fun getUsername(): String = "admin"

    fun getPassword(): String = "password"

    fun saveLoggedInUser(username: String) {
        sp
            .edit {
                putString("loginStatus", username)
            }
    }
}
