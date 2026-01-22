package com.example.instalgam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val _navigationEvent = MutableLiveData<Navigation?>()
    val navigationEvent: LiveData<Navigation?> = _navigationEvent

    fun onLoginButtonClick() {
        _navigationEvent.value = Navigation.Login
    }

    fun onSignInButtonClick() {
        _navigationEvent.value = Navigation.Signin
    }

    fun navigationComplete() {
        _navigationEvent.value = null
    }
}

sealed class Navigation {
    object Login : Navigation()

    object Signin : Navigation()
}
