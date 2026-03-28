package edu.cit.custodio.mdqueue.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.utils.SessionManager

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvEmail: TextView
    private lateinit var btnLogout: Button
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        sessionManager = SessionManager(this)

        initViews()
        displayUserInfo()
        setupListeners()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvEmail = findViewById(R.id.tvEmail)
        btnLogout = findViewById(R.id.btnLogout)
    }

    private fun displayUserInfo() {
        val fullName = sessionManager.getFullName() ?: "User"
        val email = sessionManager.getEmail() ?: ""

        tvWelcome.text = "Welcome, $fullName!"
        tvEmail.text = email
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            sessionManager.clearSession()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
