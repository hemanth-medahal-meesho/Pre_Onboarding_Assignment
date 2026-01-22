package com.example.instalgam

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.instalgam.viewmodel.MainViewModel
import com.example.instalgam.viewmodel.Navigation
import kotlinx.coroutines.launch
import kotlin.jvm.java

class MainActivity : AppCompatActivity() {
    val username: String = "admin"
    val password: String = "password"

    private val viewmodel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sp: SharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE)
        val username: String? = sp.getString(getString(R.string.logged_in_user), null)

        if (username != null) {
            val intent = Intent(this@MainActivity, PostFeedActivity::class.java)
            // intent.putExtra("USER_USERNAME", username)
            startActivity(intent)
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
