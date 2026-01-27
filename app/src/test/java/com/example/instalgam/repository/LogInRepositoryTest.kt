package com.example.instalgam.repository

import android.content.SharedPreferences
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Test

class LogInRepositoryTest {
    private lateinit var mockSharedPreferences: SharedPreferences
    private lateinit var mockEditor: SharedPreferences.Editor
    private lateinit var repository: LogInRepository

    @Before
    fun setup() {
        // Create mocks for SharedPreferences and its Editor
        mockSharedPreferences = mockk<SharedPreferences>(relaxed = true)
        mockEditor = mockk<SharedPreferences.Editor>(relaxed = true)

        // Configure mock behavior
        every { mockSharedPreferences.edit() } returns mockEditor
        every { mockEditor.putString(any(), any()) } returns mockEditor

        repository = LogInRepository(mockSharedPreferences)
    }

    @Test
    fun `getPassword and getUsername should return admin`() {
        // When: Getting username
        val username = repository.getUsername()
        val password = repository.getPassword()

        // Then: Should return "admin"
        assertThat(username).isEqualTo("admin")
        assertThat(password).isEqualTo("password")
    }

    @Test
    fun `saveLoggedInUser should save username to SharedPreferences`() {
        // Given: A username to save
        val username = "admin"

        // When: Saving logged in user
        repository.saveLoggedInUser(username)

        // Then: Should call edit() on SharedPreferences
        verify { mockSharedPreferences.edit() }

        // Then: Should save username with key "loginStatus"
        verify { mockEditor.putString("loginStatus", username) }

        // Then: Should apply changes
        verify { mockEditor.apply() }
    }

    @Test
    fun `saveLoggedInUser should save different usernames correctly`() {
        // Given: Different usernames
        val username1 = "user1"
        val username2 = "testuser"

        // When: Saving different users
        repository.saveLoggedInUser(username1)
        repository.saveLoggedInUser(username2)

        // Then: Should save both correctly
        verify { mockEditor.putString("loginStatus", username1) }
        verify { mockEditor.putString("loginStatus", username2) }
    }
}
