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
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.core.network.NetworkResult
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.core.session.SessionManager
import edu.cit.custodio.mdqueue.features.auth.LoginContract
import edu.cit.custodio.mdqueue.features.auth.model.AuthResponse
import edu.cit.custodio.mdqueue.features.auth.presenter.LoginPresenter
import edu.cit.custodio.mdqueue.features.dashboard.view.DashboardActivity

class LoginActivity : AppCompatActivity(), LoginContract.View {

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
    private lateinit var presenter: LoginContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        sessionManager = SessionManager(this)
        presenter = LoginPresenter(this)

        if (sessionManager.isLoggedIn()) {
            RetrofitClient.authToken = sessionManager.getToken()
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
                val email = etEmail.text.toString().trim()
                val password = etPassword.text.toString()
                presenter.login(email, password)
            }
        }

        btnGoogleLogin.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(this)
            builder.setTitle("🔧 Google Login (Dev Mode)")
            
            val container = LinearLayout(this)
            container.orientation = LinearLayout.VERTICAL
            val lp = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
            val marginInDp = 20
            val scale = resources.displayMetrics.density
            val marginInPx = (marginInDp * scale + 0.5f).toInt()
            lp.setMargins(marginInPx, marginInPx, marginInPx, marginInPx)
            
            val input = EditText(this)
            input.layoutParams = lp
            input.hint = "developer@example.com"
            input.setText("james@gmail.com") // Pre-populate for ease of testing!
            container.addView(input)
            builder.setView(container)

            builder.setPositiveButton("Simulate Log In") { dialog, _ ->
                val email = input.text.toString().trim()
                if (email.isNotEmpty() && email.contains("@")) {
                    val mockToken = "mock_google_token_$email"
                    presenter.loginWithGoogle(mockToken)
                    dialog.dismiss()
                } else {
                    Toast.makeText(this, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                }
            }
            builder.setNegativeButton("Cancel") { dialog, _ ->
                dialog.cancel()
            }

            builder.show()
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

    override fun onLoginResult(result: NetworkResult<AuthResponse>) {
        when (result) {
            is NetworkResult.Loading -> {
                setLoading(true)
            }
            is NetworkResult.Success -> {
                setLoading(false)
                val authResponse = result.data
                if (authResponse != null) {
                    sessionManager.saveAuthSession(
                        token = authResponse.token ?: "",
                        userId = authResponse.userId ?: 0,
                        email = authResponse.email ?: etEmail.text.toString().trim(),
                        fullName = authResponse.fullName ?: "",
                        role = authResponse.role ?: "PATIENT"
                    )

                    RetrofitClient.authToken = authResponse.token

                    showSuccessBanner(
                        "Login Successful!",
                        "Welcome back, ${authResponse.fullName ?: "User"}!"
                    )

                    Handler(Looper.getMainLooper()).postDelayed({
                        navigateToDashboard()
                    }, 1500)
                }
            }
            is NetworkResult.Error -> {
                setLoading(false)
                Toast.makeText(this, result.message ?: "Login failed", Toast.LENGTH_LONG).show()
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
