package edu.cit.custodio.mdqueue.features.auth.view

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
import android.widget.RadioButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.core.network.NetworkResult
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.core.session.SessionManager
import edu.cit.custodio.mdqueue.features.auth.RegisterContract
import edu.cit.custodio.mdqueue.features.auth.model.AuthResponse
import edu.cit.custodio.mdqueue.features.auth.presenter.RegisterPresenter
import edu.cit.custodio.mdqueue.features.dashboard.view.DashboardActivity

class RegisterActivity : AppCompatActivity(), RegisterContract.View {

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
    private lateinit var presenter: RegisterContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        sessionManager = SessionManager(this)
        presenter = RegisterPresenter(this)

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
                val fullName = etFullName.text.toString().trim()
                val email = etEmail.text.toString().trim()
                val phone = etPhone.text.toString().trim()
                val password = etPassword.text.toString()
                val confirmPassword = etConfirmPassword.text.toString()
                
                val isDoctor = findViewById<RadioButton>(R.id.rbDoctor).isChecked
                val role = if (isDoctor) "DOCTOR" else "PATIENT"
                
                presenter.register(fullName, email, phone, password, confirmPassword, role)
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

    override fun onRegisterResult(result: NetworkResult<AuthResponse>) {
        when (result) {
            is NetworkResult.Loading -> {
                setLoading(true)
            }
            is NetworkResult.Success -> {
                setLoading(false)
                val authResponse = result.data
                if (authResponse != null) {
                    val isDoctor = findViewById<RadioButton>(R.id.rbDoctor).isChecked
                    val role = if (isDoctor) "DOCTOR" else "PATIENT"

                    sessionManager.saveAuthSession(
                        token = authResponse.token ?: "",
                        userId = authResponse.userId ?: 0,
                        email = authResponse.email ?: etEmail.text.toString().trim(),
                        fullName = authResponse.fullName ?: etFullName.text.toString().trim(),
                        role = authResponse.role ?: role
                    )
                    
                    RetrofitClient.authToken = authResponse.token

                    showSuccessBanner(
                        "Account Created!",
                        "Welcome to MDQueue, ${authResponse.fullName ?: etFullName.text.toString().trim()}!"
                    )

                    Handler(Looper.getMainLooper()).postDelayed({
                        val intent = Intent(this@RegisterActivity, DashboardActivity::class.java)
                        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        startActivity(intent)
                        finish()
                    }, 1500)
                }
            }
            is NetworkResult.Error -> {
                setLoading(false)
                Toast.makeText(this, result.message ?: "Registration failed", Toast.LENGTH_LONG).show()
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
