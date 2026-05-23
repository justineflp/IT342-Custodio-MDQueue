package edu.cit.custodio.mdqueue.features.dashboard.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.core.session.SessionManager
import edu.cit.custodio.mdqueue.features.appointment.model.AppointmentResponse
import edu.cit.custodio.mdqueue.features.admin.view.AdminQueueActivity
import edu.cit.custodio.mdqueue.features.appointment.view.AppointmentDetailActivity
import edu.cit.custodio.mdqueue.features.appointment.view.BookAppointmentActivity
import edu.cit.custodio.mdqueue.features.auth.view.LoginActivity
import edu.cit.custodio.mdqueue.features.dashboard.DashboardContract
import edu.cit.custodio.mdqueue.features.dashboard.presenter.DashboardPresenter

class DashboardActivity : AppCompatActivity(), DashboardContract.View {

    private lateinit var tvWelcome: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRoleBadge: TextView
    private lateinit var btnLogout: Button
    
    // Patient Section Views
    private lateinit var layoutPatientActions: LinearLayout
    private lateinit var btnBookAppointment: Button
    private lateinit var cardUpcomingAppointment: LinearLayout
    private lateinit var tvDoctorName: TextView
    private lateinit var tvApptDatetime: TextView
    private lateinit var tvApptStatus: TextView
    private lateinit var btnViewDetails: Button
    private lateinit var tvNoAppointments: TextView

    // Doctor Section Views
    private lateinit var layoutDoctorActions: LinearLayout
    private lateinit var btnViewSchedule: Button

    private lateinit var sessionManager: SessionManager
    private lateinit var presenter: DashboardContract.Presenter
    private var upcomingAppt: AppointmentResponse? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        sessionManager = SessionManager(this)
        RetrofitClient.authToken = sessionManager.getToken()
        
        presenter = DashboardPresenter(this)

        initViews()
        displayUserInfo()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        if (sessionManager.getRole() == "PATIENT") {
            presenter.fetchUpcomingAppointment()
        }
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvEmail = findViewById(R.id.tvEmail)
        tvRoleBadge = findViewById(R.id.tvRoleBadge)
        btnLogout = findViewById(R.id.btnLogout)

        layoutPatientActions = findViewById(R.id.layoutPatientActions)
        btnBookAppointment = findViewById(R.id.btnBookAppointment)
        cardUpcomingAppointment = findViewById(R.id.cardUpcomingAppointment)
        tvDoctorName = findViewById(R.id.tvDoctorName)
        tvApptDatetime = findViewById(R.id.tvApptDatetime)
        tvApptStatus = findViewById(R.id.tvApptStatus)
        btnViewDetails = findViewById(R.id.btnViewDetails)
        tvNoAppointments = findViewById(R.id.tvNoAppointments)

        layoutDoctorActions = findViewById(R.id.layoutDoctorActions)
        btnViewSchedule = findViewById(R.id.btnViewSchedule)
    }

    private fun displayUserInfo() {
        val fullName = sessionManager.getFullName() ?: "User"
        val email = sessionManager.getEmail() ?: ""
        val role = sessionManager.getRole() ?: "PATIENT"

        tvWelcome.text = "Welcome, $fullName!"
        tvEmail.text = email
        tvRoleBadge.text = role

        if (role == "DOCTOR" || role == "ADMIN") {
            layoutDoctorActions.visibility = View.VISIBLE
            layoutPatientActions.visibility = View.GONE
            tvRoleBadge.setBackgroundResource(R.drawable.bg_banner_success)
            tvRoleBadge.backgroundTintList = getColorStateList(R.color.primary)
        } else {
            layoutDoctorActions.visibility = View.GONE
            layoutPatientActions.visibility = View.VISIBLE
            tvRoleBadge.setBackgroundResource(R.drawable.bg_banner_success)
            tvRoleBadge.backgroundTintList = getColorStateList(R.color.success)
            presenter.fetchUpcomingAppointment()
        }
    }

    private fun setupListeners() {
        btnLogout.setOnClickListener {
            sessionManager.clearSession()
            RetrofitClient.authToken = null

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        btnBookAppointment.setOnClickListener {
            startActivity(Intent(this, BookAppointmentActivity::class.java))
        }

        btnViewDetails.setOnClickListener {
            if (upcomingAppt != null) {
                val intent = Intent(this, AppointmentDetailActivity::class.java)
                intent.putExtra("APPOINTMENT_ID", upcomingAppt!!.id)
                startActivity(intent)
            }
        }

        btnViewSchedule.setOnClickListener {
            startActivity(Intent(this, AdminQueueActivity::class.java))
        }
    }

    override fun showLoading() {
        // Optional: show progress bar
    }

    override fun hideLoading() {
        // Optional: hide progress bar
    }

    override fun showUpcomingAppointment(appointment: AppointmentResponse) {
        upcomingAppt = appointment
        tvDoctorName.text = "Dr. ${appointment.doctorName}"
        tvApptDatetime.text = appointment.appointmentDatetime
        tvApptStatus.text = appointment.status

        tvNoAppointments.visibility = View.GONE
        cardUpcomingAppointment.visibility = View.VISIBLE
    }

    override fun showNoAppointments() {
        upcomingAppt = null
        cardUpcomingAppointment.visibility = View.GONE
        tvNoAppointments.visibility = View.VISIBLE
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        showNoAppointments()
    }
}
