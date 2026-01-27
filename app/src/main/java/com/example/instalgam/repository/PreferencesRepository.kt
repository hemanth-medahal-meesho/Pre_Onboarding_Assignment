package com.example.instalgam.repository

import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesRepository(
    private val sharedPreferences: SharedPreferences,
) {
    fun signOutUser() {
        sharedPreferences.edit {
            putString("loginStatus", null)
        }
    }
}
