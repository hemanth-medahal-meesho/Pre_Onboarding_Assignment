package com.example.instalgam.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.instalgam.repository.LogInRepository
import com.google.common.truth.Truth.assertThat
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class LoginViewModelTest {
    // This rule makes LiveData execute synchronously on the main thread
    // Required for testing LiveData without Android framework
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private lateinit var mockRepository: LogInRepository
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        // Create a mock repository
        mockRepository = mockk<LogInRepository>(relaxed = true)
        viewModel = LoginViewModel(mockRepository)
    }

    @Test
    fun `onClick with correct credentials should emit LoginSuccess`() {
        // Given: Repository returns correct credentials
        every { mockRepository.getUsername() } returns "admin"
        every { mockRepository.getPassword() } returns "password"

        // When: User clicks with correct credentials
        viewModel.onClick("admin", "password")

        // Then: Navigation should be LoginSuccess
        val navigationValue = viewModel.navVal.value
        assertThat(navigationValue).isInstanceOf(LoginNav.LoginSuccess::class.java)

        // Verify repository method was called
        verify { mockRepository.saveLoggedInUser("admin") }
    }

    @Test
    fun `onClick with wrong username should emit LoginFailure`() {
        // Given: Repository returns correct credentials
        every { mockRepository.getUsername() } returns "admin"
        every { mockRepository.getPassword() } returns "password"

        // When: User clicks with wrong username
        viewModel.onClick("wronguser", "password")

        // Then: Navigation should be LoginFailure
        val navigationValue = viewModel.navVal.value
        assertThat(navigationValue).isInstanceOf(LoginNav.LoginFailure::class.java)

        // Verify saveLoggedInUser was NOT called
        verify(exactly = 0) { mockRepository.saveLoggedInUser(any()) }
    }

    @Test
    fun `onClick with wrong password should emit LoginFailure`() {
        // Given: Repository returns correct credentials
        every { mockRepository.getUsername() } returns "admin"
        every { mockRepository.getPassword() } returns "password"

        // When: User clicks with wrong password
        viewModel.onClick("admin", "wrongpassword")

        // Then: Navigation should be LoginFailure
        val navigationValue = viewModel.navVal.value
        assertThat(navigationValue).isInstanceOf(LoginNav.LoginFailure::class.java)

        // Verify saveLoggedInUser was NOT called
        verify(exactly = 0) { mockRepository.saveLoggedInUser(any()) }
    }

    @Test
    fun `onClick with both wrong credentials should emit LoginFailure`() {
        // Given: Repository returns correct credentials
        every { mockRepository.getUsername() } returns "admin"
        every { mockRepository.getPassword() } returns "password"

        // When: User clicks with both wrong credentials
        viewModel.onClick("wronguser", "wrongpassword")

        // Then: Navigation should be LoginFailure
        val navigationValue = viewModel.navVal.value
        assertThat(navigationValue).isInstanceOf(LoginNav.LoginFailure::class.java)
    }

    @Test
    fun `navigationComplete should reset navigation value to null`() {
        // Given: Navigation value is set
        every { mockRepository.getUsername() } returns "admin"
        every { mockRepository.getPassword() } returns "password"
        viewModel.onClick("admin", "password")

        // Verify navigation is set
        assertThat(viewModel.navVal.value).isNotNull()

        // When: Navigation is marked as complete
        viewModel.navigationComplete()

        // Then: Navigation value should be null
        assertThat(viewModel.navVal.value).isNull()
    }
}
