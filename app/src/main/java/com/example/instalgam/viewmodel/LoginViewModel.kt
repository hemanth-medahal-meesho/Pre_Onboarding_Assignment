package com.example.instalgam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instalgam.repository.LogInRepository

class LoginViewModel(
    private var loginRepository: LogInRepository,
) : ViewModel() {
    private val _navVal = MutableLiveData<LoginNav?>()
    val navVal: LiveData<LoginNav?> = _navVal

    fun onClick(
        username: String,
        password: String,
    ) {
        if (username == loginRepository.getUsername() && password == loginRepository.getPassword()) {
            _navVal.value = LoginNav.LoginSuccess
            loginRepository.saveLoggedInUser(username)
        } else {
            _navVal.value = LoginNav.LoginFailure
        }
    }

    fun navigationComplete() {
        _navVal.value = null
    }
}

sealed class LoginNav {
    object LoginSuccess : LoginNav()

    object LoginFailure : LoginNav()
}
