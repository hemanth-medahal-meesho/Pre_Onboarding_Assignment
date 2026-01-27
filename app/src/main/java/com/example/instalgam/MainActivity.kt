package com.example.instalgam

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.instalgam.repository.PreferencesRepository
import com.example.instalgam.viewmodel.MainViewModel
import com.example.instalgam.viewmodel.Navigation

class MainActivity : AppCompatActivity() {
    private val viewmodel: MainViewModel by viewModels {
        val sp: SharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE)
        val repo = PreferencesRepository(sp)
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = MainViewModel(repo) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewmodel.checkLoginStatus()
        viewmodel.shouldNavigateToPostFeed.observe(this) { shouldNavigate ->
            if (shouldNavigate == true) {
                val intent = Intent(this@MainActivity, PostFeedActivity::class.java)
                startActivity(intent)
                finish()
                viewmodel.navigationToPostFeedComplete()
            }
        }

        setContentView(R.layout.landing_page)
        val login: Button = findViewById(R.id.loginButton)
        val signin: Button = findViewById(R.id.signInButton)
        login.setOnClickListener {
            viewmodel.onLoginButtonClick()
        }
        signin.setOnClickListener {
            viewmodel.onSignInButtonClick()
        }
        viewmodel.navigationEvent.observe(this) { navigation ->
            navigation?.let {
                when (it) {
                    Navigation.Login -> {
                        startActivity(Intent(this, LoginActivity::class.java))
                    }

                    Navigation.Signin -> {
                        startActivity(Intent(this, SignInActivity::class.java))
                    }
                }
                viewmodel.navigationComplete()
            }
        }
    }
}
