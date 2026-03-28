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
import edu.cit.custodio.mdqueue.api.models.LoginRequest
import edu.cit.custodio.mdqueue.utils.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: EditText
    private lateinit var etPassword: EditText
    private lateinit var btnLogin: Button
    private lateinit var btnGoogleLogin: Button
    private lateinit var tvRegisterLink: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var bannerSuccess: LinearLayout
    private lateinit var bannerTitle: TextView
    private lateinit var bannerMessage: TextView
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)

        // If already logged in, go to dashboard
        if (sessionManager.isLoggedIn()) {
            navigateToDashboard()
            return
        }

        initViews()
        setupListeners()
    }

    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin)
        tvRegisterLink = findViewById(R.id.tvRegisterLink)
        progressBar = findViewById(R.id.progressBar)
        bannerSuccess = findViewById(R.id.bannerSuccess)
        bannerTitle = findViewById(R.id.bannerTitle)
        bannerMessage = findViewById(R.id.bannerMessage)
    }

    private fun setupListeners() {
        btnLogin.setOnClickListener {
            if (validateInputs()) {
                performLogin()
            }
        }

        btnGoogleLogin.setOnClickListener {
            Toast.makeText(this, "Google Login coming soon!", Toast.LENGTH_SHORT).show()
        }

        tvRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun validateInputs(): Boolean {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

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

        return true
    }

    private fun performLogin() {
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()

        setLoading(true)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.authApi.login(
                    LoginRequest(email = email, password = password)
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
                            fullName = authResponse.fullName ?: ""
                        )

                        // Show success banner
                        showSuccessBanner(
                            "Login Successful!",
                            "Welcome back, ${authResponse.fullName ?: "User"}!"
                        )

                        // Navigate after delay so user can see the banner
                        Handler(Looper.getMainLooper()).postDelayed({
                            navigateToDashboard()
                        }, 1500)
                    } else {
                        val errorMsg = when (response.code()) {
                            401 -> "Invalid email or password"
                            403 -> "Invalid email or password"
                            else -> "Login failed. Please try again."
                        }
                        Toast.makeText(this@LoginActivity, errorMsg, Toast.LENGTH_LONG).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(
                        this@LoginActivity,
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
        btnLogin.isEnabled = !isLoading
        btnGoogleLogin.isEnabled = !isLoading
        etEmail.isEnabled = !isLoading
        etPassword.isEnabled = !isLoading
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
