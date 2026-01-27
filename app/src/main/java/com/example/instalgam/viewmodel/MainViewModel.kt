package com.example.instalgam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instalgam.repository.PreferencesRepository

class MainViewModel(
    private val preferencesRepository: PreferencesRepository,
) : ViewModel() {
    private val _navigationEvent = MutableLiveData<Navigation?>()
    val navigationEvent: LiveData<Navigation?> = _navigationEvent

    private val _shouldNavigateToPostFeed = MutableLiveData<Boolean>()
    val shouldNavigateToPostFeed: LiveData<Boolean> = _shouldNavigateToPostFeed

    fun checkLoginStatus() {
        val username = preferencesRepository.fetchUsername()
        _shouldNavigateToPostFeed.value = username != null && username.isNotEmpty()
    }

    fun onLoginButtonClick() {
        _navigationEvent.value = Navigation.Login
    }

    fun onSignInButtonClick() {
        _navigationEvent.value = Navigation.Signin
    }

    fun navigationComplete() {
        _navigationEvent.value = null
    }

    fun navigationToPostFeedComplete() {
        _shouldNavigateToPostFeed.value = false
    }
}

sealed class Navigation {
    object Login : Navigation()

    object Signin : Navigation()
}
