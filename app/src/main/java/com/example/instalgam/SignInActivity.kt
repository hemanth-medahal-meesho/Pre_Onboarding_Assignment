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
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.instalgam.repository.LogInRepository
import com.example.instalgam.viewmodel.SigninNav
import com.example.instalgam.viewmodel.SigninViewModel

class SignInActivity : AppCompatActivity() {
    private val viewmodel: SigninViewModel by viewModels {
        val sp: SharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_file_name), Context.MODE_PRIVATE)
        val repo = LogInRepository(sp)
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T = SigninViewModel(repo) as T
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.signin_page)
        val usernameField: EditText = findViewById(R.id.usernameEntry)
        val passwordField: EditText = findViewById(R.id.passwordEntry)
        val reenterPasswordField: EditText = findViewById(R.id.reenter_password)
        val enterButton: Button = findViewById(R.id.signInEntry)
        enterButton.setOnClickListener {
            val username = usernameField.text.toString()
            val pwd1 = passwordField.text.toString()
            val pwd2 = reenterPasswordField.text.toString()
            viewmodel.onClick(username, pwd1, pwd2)
        }
        viewmodel.navVal.observe(this) { navigation ->
            navigation?.let {
                when (it) {
                    is SigninNav.Success -> {
                        Toast.makeText(this, "Successfully registered!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignInActivity, MainActivity::class.java)
                        startActivity(intent)
                        finish()
                    }

                    is SigninNav.PasswordsDoNotMatch -> {
                        Toast.makeText(this, "The passwords do not match!", Toast.LENGTH_SHORT).show()
                        usernameField.setText(null)
                        passwordField.setText(null)
                        reenterPasswordField.setText(null)
                    }

                    is SigninNav.WrongUsername -> {
                        Toast.makeText(this, "Empty username", Toast.LENGTH_SHORT).show()
                        usernameField.setText(null)
                        passwordField.setText(null)
                        reenterPasswordField.setText(null)
                    }
                }
                viewmodel.navigationComplete()
            }
        }
    }
}
