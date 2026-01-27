package com.example.instalgam.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.instalgam.repository.LogInRepository

class SigninViewModel(
    private val loginRepository: LogInRepository,
) : ViewModel() {
    private val _navVal = MutableLiveData<SigninNav?>()
    val navVal: LiveData<SigninNav?> = _navVal

    fun onClick(
        username: String,
        pwd1: String,
        pwd2: String,
    ) {
        if (pwd1 != pwd2) {
            _navVal.value = SigninNav.PasswordsDoNotMatch
        } else if (username.isEmpty()) {
            _navVal.value = SigninNav.WrongUsername
        } else if (username.isNotEmpty() && pwd1.isNotEmpty() && pwd2.isNotEmpty()) {
            loginRepository.saveLoggedInUser(username)
            _navVal.value = SigninNav.Success
        }
    }

    fun navigationComplete() {
        _navVal.value = null
    }
}

sealed class SigninNav {
    object Success : SigninNav()

    object WrongUsername : SigninNav()

    object PasswordsDoNotMatch : SigninNav()
}
