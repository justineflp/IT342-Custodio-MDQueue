package edu.cit.custodio.mdqueue.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Patterns
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.api.ApiClient
import edu.cit.custodio.mdqueue.api.models.RegisterRequest
import edu.cit.custodio.mdqueue.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterActivity : AppCompatActivity() {

    private lateinit var etFullName: EditText
    private lateinit var etEmail: EditText
    private lateinit var etPhone: EditText
    private lateinit var etPassword: EditText
    private lateinit var etConfirmPassword: EditText
    private lateinit var btnRegister: Button
    private lateinit var tvLoginLink: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var bannerSuccess: LinearLayout
    private lateinit var bannerTitle: TextView
    private lateinit var bannerMessage: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sessionManager = SessionManager(this)

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etFullName = findViewById(R.id.etFullName)
        etEmail = findViewById(R.id.etEmail)
        etPhone = findViewById(R.id.etPhone)
        etPassword = findViewById(R.id.etPassword)
        etConfirmPassword = findViewById(R.id.etConfirmPassword)
        btnRegister = findViewById(R.id.btnRegister)
        tvLoginLink = findViewById(R.id.tvLoginLink)
        progressBar = findViewById(R.id.progressBar)
        bannerSuccess = findViewById(R.id.bannerSuccess)
        bannerTitle = findViewById(R.id.bannerTitle)
        bannerMessage = findViewById(R.id.bannerMessage)
    }

    private fun setupListeners() {
        btnRegister.setOnClickListener {
            if (validateInputs()) {
                performRegistration()
            }
        }

        tvLoginLink.setOnClickListener {
            finish() // Go back to login
        }
    }

    private fun validateInputs(): Boolean {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        if (fullName.isEmpty()) {
            etFullName.error = getString(R.string.error_empty_name)
            etFullName.requestFocus()
            return false
        }

        if (email.isEmpty()) {
            etEmail.error = getString(R.string.error_empty_email)
            etEmail.requestFocus()
            return false
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.error = getString(R.string.error_invalid_email)
            etEmail.requestFocus()
            return false
        }

        if (password.isEmpty()) {
            etPassword.error = getString(R.string.error_empty_password)
            etPassword.requestFocus()
            return false
        }

        if (password.length < 8) {
            etPassword.error = getString(R.string.error_short_password)
            etPassword.requestFocus()
            return false
        }

        if (password != confirmPassword) {
            etConfirmPassword.error = getString(R.string.error_password_mismatch)
            etConfirmPassword.requestFocus()
            return false
        }

        return true
    }

    private fun performRegistration() {
        val fullName = etFullName.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val phone = etPhone.text.toString().trim()
        val password = etPassword.text.toString()
        val confirmPassword = etConfirmPassword.text.toString()

        setLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.authApi.register(
                    RegisterRequest(
                        fullName = fullName,
                        email = email,
                        phoneNumber = phone,
                        password = password,
                        confirmPassword = confirmPassword
                    )
                )

                withContext(Dispatchers.Main) {
                    setLoading(false)

                    if (response.isSuccessful && response.body() != null) {
                        val authResponse = response.body()!!

                        // Save session
                        sessionManager.saveAuthSession(
                            token = authResponse.token ?: "",
                            userId = authResponse.userId ?: 0,
                            email = authResponse.email ?: email,
                            fullName = authResponse.fullName ?: fullName
                        )

                        // Show success banner
                        showSuccessBanner(
                            "Account Created!",
                            "Welcome to MDQueue, ${authResponse.fullName ?: fullName}!"
                        )

                        // Navigate after delay so user can see the banner
                        Handler(Looper.getMainLooper()).postDelayed({
                            val intent = Intent(this@RegisterActivity, DashboardActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                            startActivity(intent)
                            finish()
                        }, 1500)
                    } else {
                        val errorMsg = when (response.code()) {
                            400 -> "Invalid registration data. Please check your inputs."
                            409 -> "An account with this email already exists."
                            else -> "Registration failed. Please try again."
                        }
                        Toast.makeText(this@RegisterActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(
                        this@RegisterActivity,
                        getString(R.string.error_network),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun showSuccessBanner(title: String, message: String) {
        bannerTitle.text = title
        bannerMessage.text = message
        bannerSuccess.visibility = View.VISIBLE

        val slideDown = AnimationUtils.loadAnimation(this, R.anim.slide_down)
        bannerSuccess.startAnimation(slideDown)
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        btnRegister.isEnabled = !isLoading
        etFullName.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPhone.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
        etConfirmPassword.isEnabled = !isLoading
    }
}
