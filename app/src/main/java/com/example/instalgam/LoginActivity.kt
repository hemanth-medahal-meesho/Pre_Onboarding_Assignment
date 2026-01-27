package com.example.instalgam

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.instalgam.repository.LogInRepository
import com.example.instalgam.viewmodel.LoginNav
import com.example.instalgam.viewmodel.LoginViewModel

class LoginActivity : AppCompatActivity() {
    private val viewmodel: LoginViewModel by viewModels {

        val sp: SharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE)

        val repo = LogInRepository(sp)
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = LoginViewModel(repo) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.login_page)
        val usernameField: EditText = findViewById(R.id.usernameEntry)
        val passwordField: EditText = findViewById(R.id.passwordEntry)

        val enterButton: Button = findViewById(R.id.logInEntry)
        enterButton.setOnClickListener {
            val uname: String = usernameField.text.toString()
            val pwd: String = passwordField.text.toString()

            viewmodel.onClick(uname, pwd)
        }
        viewmodel.navVal.observe(this) { navigation ->

            navigation?.let {
                when (it) {
                    is LoginNav.LoginSuccess -> {
                        val intent = Intent(this@LoginActivity, PostFeedActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    is LoginNav.LoginFailure -> {
                        Toast.makeText(this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show()
                        usernameField.setText(null)
                        passwordField.setText(null)
                    }
                }
                viewmodel.navigationComplete()
            }
        }
    }
}
