package com.app.gdl.presentation.ui.activity

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.app.gdl.data.model.Address
import com.app.gdl.data.model.SignupRequest
import com.app.gdl.databinding.ActivitySignupBinding
import com.app.gdl.presentation.viewmodel.SignupViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SignUpActivity :AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private val signupViewModel: SignupViewModel by viewModels()

    @SuppressLint("UnsafeIntentLaunch")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnCreateAccount.setOnClickListener{
          /*  intent = Intent(this,SignInActivity::class.java)
            startActivity(intent)*/
            val request = SignupRequest(
                first_name = binding.etFirstName.text.toString(),
                last_name = binding.etLastName.text.toString(),
                email_id = binding.etEmail.text.toString(),
                phone = binding.etEmail.text.toString(),
                password = binding.etPassword.text.toString(),
                address = listOf(
                    Address("home", "Bandra west, Mumbai", listOf(19.053838, 72.851319), 1),
                    Address("office", "Colaba, Mumbai", listOf(18.900117, 72.805541), 0)
                )
            )
            signupViewModel.signup(request)
        }
        signupViewModel.signupResult.observe(this) { result ->
            result.onSuccess {
                Toast.makeText(this, "Signup Successful: ${it.message}", Toast.LENGTH_LONG).show()
            }.onFailure {
                Toast.makeText(this, "Signup Failed: ${it.localizedMessage}", Toast.LENGTH_LONG).show()
            }
        }
    }
}