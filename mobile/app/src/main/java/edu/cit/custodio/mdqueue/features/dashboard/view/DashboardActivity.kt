package edu.cit.custodio.mdqueue.features.dashboard.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.core.session.SessionManager
import edu.cit.custodio.mdqueue.features.appointment.model.AppointmentResponse
import edu.cit.custodio.mdqueue.features.appointment.model.AdminDoctorResponse
import edu.cit.custodio.mdqueue.features.appointment.view.AppointmentDetailActivity
import edu.cit.custodio.mdqueue.features.appointment.view.BookAppointmentActivity
import edu.cit.custodio.mdqueue.features.appointment.view.AppointmentsActivity
import edu.cit.custodio.mdqueue.features.profile.view.ProfileActivity
import edu.cit.custodio.mdqueue.features.auth.view.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardActivity : AppCompatActivity() {

    private lateinit var tvWelcome: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvRoleBadge: TextView
    private lateinit var btnLogout: Button

    // Patient Dashboard Elements
    private lateinit var layoutPatientDashboard: LinearLayout
    private lateinit var btnBookAppointment: Button
    private lateinit var btnPatientAllAppointments: Button
    private lateinit var btnPatientProfile: Button
    private lateinit var rvPatientAppointments: RecyclerView
    private lateinit var tvNoPatientAppointments: TextView

    // Doctor Dashboard Elements
    private lateinit var layoutDoctorDashboard: LinearLayout
    private lateinit var btnDoctorAllAppointments: Button
    private lateinit var btnDoctorProfile: Button
    private lateinit var rvDoctorTodayAppointments: RecyclerView
    private lateinit var tvNoDoctorTodayAppointments: TextView
    private lateinit var rvDoctorUpcomingAppointments: RecyclerView
    private lateinit var tvNoDoctorUpcomingAppointments: TextView

    // Admin Dashboard Elements
    private lateinit var layoutAdminDashboard: LinearLayout
    private lateinit var btnAdminAllAppointments: Button
    private lateinit var btnAdminProfile: Button
    private lateinit var tvKpiAppointments: TextView
    private lateinit var tvKpiActiveDoctors: TextView
    private lateinit var tvKpiPendingDoctors: TextView
    private lateinit var rvAdminApprovals: RecyclerView
    private lateinit var tvNoAdminApprovals: TextView

    private lateinit var sessionManager: SessionManager
    private val patientApptsList = mutableListOf<AppointmentResponse>()
    private val doctorTodayList = mutableListOf<AppointmentResponse>()
    private val doctorUpcomingList = mutableListOf<AppointmentResponse>()
    private val pendingDocsList = mutableListOf<AdminDoctorResponse>()

    private lateinit var patientAdapter: DashboardAppointmentsAdapter
    private lateinit var doctorTodayAdapter: DashboardAppointmentsAdapter
    private lateinit var doctorUpcomingAdapter: DashboardAppointmentsAdapter
    private lateinit var adminAdapter: AdminApprovalsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        sessionManager = SessionManager(this)
        RetrofitClient.authToken = sessionManager.getToken()

        initViews()
        setupRecyclerViews()
        setupListeners()
        displayUserInfoAndLoad()
    }

    override fun onResume() {
        super.onResume()
        loadDataForRole()
    }

    private fun initViews() {
        tvWelcome = findViewById(R.id.tvWelcome)
        tvEmail = findViewById(R.id.tvEmail)
        tvRoleBadge = findViewById(R.id.tvRoleBadge)
        btnLogout = findViewById(R.id.btnLogout)

        // Patient Dashboard
        layoutPatientDashboard = findViewById(R.id.layoutPatientDashboard)
        btnBookAppointment = findViewById(R.id.btnBookAppointment)
        btnPatientAllAppointments = findViewById(R.id.btnPatientAllAppointments)
        btnPatientProfile = findViewById(R.id.btnPatientProfile)
        rvPatientAppointments = findViewById(R.id.rvPatientAppointments)
        tvNoPatientAppointments = findViewById(R.id.tvNoPatientAppointments)

        // Doctor Dashboard
        layoutDoctorDashboard = findViewById(R.id.layoutDoctorDashboard)
        btnDoctorAllAppointments = findViewById(R.id.btnDoctorAllAppointments)
        btnDoctorProfile = findViewById(R.id.btnDoctorProfile)
        rvDoctorTodayAppointments = findViewById(R.id.rvDoctorTodayAppointments)
        tvNoDoctorTodayAppointments = findViewById(R.id.tvNoDoctorTodayAppointments)
        rvDoctorUpcomingAppointments = findViewById(R.id.rvDoctorUpcomingAppointments)
        tvNoDoctorUpcomingAppointments = findViewById(R.id.tvNoDoctorUpcomingAppointments)

        // Admin Dashboard
        layoutAdminDashboard = findViewById(R.id.layoutAdminDashboard)
        btnAdminAllAppointments = findViewById(R.id.btnAdminAllAppointments)
        btnAdminProfile = findViewById(R.id.btnAdminProfile)
        tvKpiAppointments = findViewById(R.id.tvKpiAppointments)
        tvKpiActiveDoctors = findViewById(R.id.tvKpiActiveDoctors)
        tvKpiPendingDoctors = findViewById(R.id.tvKpiPendingDoctors)
        rvAdminApprovals = findViewById(R.id.rvAdminApprovals)
        tvNoAdminApprovals = findViewById(R.id.tvNoAdminApprovals)
    }

    private fun setupRecyclerViews() {
        // Patient Adapter
        rvPatientAppointments.layoutManager = LinearLayoutManager(this)
        patientAdapter = DashboardAppointmentsAdapter(
            appointments = patientApptsList,
            isDoctor = false,
            onItemClick = { appt -> openAppointmentDetails(appt.id) }
        )
        rvPatientAppointments.adapter = patientAdapter

        // Doctor Today Adapter
        rvDoctorTodayAppointments.layoutManager = LinearLayoutManager(this)
        doctorTodayAdapter = DashboardAppointmentsAdapter(
            appointments = doctorTodayList,
            isDoctor = true,
            onItemClick = { appt -> openAppointmentDetails(appt.id) },
            onConfirmClick = { appt -> showDoctorConfirmDialog(appt.id) },
            onCancelClick = { appt -> updateAppointmentStatus(appt.id, "CANCELLED") }
        )
        rvDoctorTodayAppointments.adapter = doctorTodayAdapter

        // Doctor Upcoming Adapter
        rvDoctorUpcomingAppointments.layoutManager = LinearLayoutManager(this)
        doctorUpcomingAdapter = DashboardAppointmentsAdapter(
            appointments = doctorUpcomingList,
            isDoctor = true,
            onItemClick = { appt -> openAppointmentDetails(appt.id) },
            onConfirmClick = { appt -> showDoctorConfirmDialog(appt.id) },
            onCancelClick = { appt -> updateAppointmentStatus(appt.id, "CANCELLED") }
        )
        rvDoctorUpcomingAppointments.adapter = doctorUpcomingAdapter

        // Admin approvals adapter
        rvAdminApprovals.layoutManager = LinearLayoutManager(this)
        adminAdapter = AdminApprovalsAdapter(
            doctors = pendingDocsList,
            onApproveClick = { doc -> approveDoctorRegistration(doc.id) }
        )
        rvAdminApprovals.adapter = adminAdapter
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

        // All appointments routing
        val toAllAppointments = View.OnClickListener {
            startActivity(Intent(this, AppointmentsActivity::class.java))
        }
        btnPatientAllAppointments.setOnClickListener(toAllAppointments)
        btnDoctorAllAppointments.setOnClickListener(toAllAppointments)
        btnAdminAllAppointments.setOnClickListener(toAllAppointments)

        // Profile routing
        val toProfile = View.OnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }
        btnPatientProfile.setOnClickListener(toProfile)
        btnDoctorProfile.setOnClickListener(toProfile)
        btnAdminProfile.setOnClickListener(toProfile)
    }

    private fun displayUserInfoAndLoad() {
        val fullName = sessionManager.getFullName() ?: "User"
        val email = sessionManager.getEmail() ?: ""
        val role = sessionManager.getRole() ?: "PATIENT"

        tvWelcome.text = "Welcome back, $fullName!"
        tvEmail.text = email
        tvRoleBadge.text = role

        // Render role-specific container
        when (role) {
            "ADMIN" -> {
                layoutAdminDashboard.visibility = View.VISIBLE
                layoutPatientDashboard.visibility = View.GONE
                layoutDoctorDashboard.visibility = View.GONE
                tvRoleBadge.setBackgroundResource(R.drawable.bg_banner_success)
                tvRoleBadge.backgroundTintList = getColorStateList(R.color.primary)
            }
            "DOCTOR" -> {
                layoutDoctorDashboard.visibility = View.VISIBLE
                layoutPatientDashboard.visibility = View.GONE
                layoutAdminDashboard.visibility = View.GONE
                tvRoleBadge.setBackgroundResource(R.drawable.bg_banner_success)
                tvRoleBadge.backgroundTintList = getColorStateList(R.color.primary)
            }
            else -> {
                layoutPatientDashboard.visibility = View.VISIBLE
                layoutDoctorDashboard.visibility = View.GONE
                layoutAdminDashboard.visibility = View.GONE
                tvRoleBadge.setBackgroundResource(R.drawable.bg_banner_success)
                tvRoleBadge.backgroundTintList = getColorStateList(R.color.success)
            }
        }

        loadDataForRole()
    }

    private fun loadDataForRole() {
        val role = sessionManager.getRole() ?: "PATIENT"
        when (role) {
            "PATIENT" -> loadPatientAppointments()
            "DOCTOR" -> loadDoctorAppointments()
            "ADMIN" -> loadAdminData()
        }
    }

    private fun loadPatientAppointments() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.appointmentApi.getMyAppointments()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()!!.data ?: emptyList()
                        
                        // FILTER: Completed and Cancelled appointments are hidden from the dashboard
                        // ALSO hide past appointments
                        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
                        val now = java.util.Date()
                        val active = list.filter { appt ->
                            if (appt.status != "PENDING" && appt.status != "CONFIRMED") return@filter false
                            try {
                                val apptDate = sdf.parse(appt.appointmentDatetime)
                                apptDate != null && apptDate.after(now)
                            } catch (e: Exception) {
                                false
                            }
                        }

                        patientApptsList.clear()
                        patientApptsList.addAll(active.take(5))
                        patientAdapter.notifyDataSetChanged()

                        if (patientApptsList.isEmpty()) {
                            tvNoPatientAppointments.visibility = View.VISIBLE
                            rvPatientAppointments.visibility = View.GONE
                        } else {
                            tvNoPatientAppointments.visibility = View.GONE
                            rvPatientAppointments.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error loading appointments: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadDoctorAppointments() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.appointmentApi.getMyAppointments()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()!!.data ?: emptyList()
                        
                        // FILTER: Completed and Cancelled appointments are hidden from the dashboard
                        val active = list.filter { it.status == "PENDING" || it.status == "CONFIRMED" }

                        // Partition into Today vs Upcoming using yyyy-MM-dd matching
                        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                        val todayStr = sdf.format(Calendar.getInstance().time)

                        val todayAppts = active.filter { it.appointmentDatetime.take(10) == todayStr }
                        val upcomingAppts = active.filter { it.appointmentDatetime.take(10) != todayStr }

                        doctorTodayList.clear()
                        doctorTodayList.addAll(todayAppts)
                        doctorTodayAdapter.notifyDataSetChanged()

                        doctorUpcomingList.clear()
                        doctorUpcomingList.addAll(upcomingAppts)
                        doctorUpcomingAdapter.notifyDataSetChanged()

                        // Manage Today placeholders
                        if (doctorTodayList.isEmpty()) {
                            tvNoDoctorTodayAppointments.visibility = View.VISIBLE
                            rvDoctorTodayAppointments.visibility = View.GONE
                        } else {
                            tvNoDoctorTodayAppointments.visibility = View.GONE
                            rvDoctorTodayAppointments.visibility = View.VISIBLE
                        }

                        // Manage Upcoming placeholders
                        if (doctorUpcomingList.isEmpty()) {
                            tvNoDoctorUpcomingAppointments.visibility = View.VISIBLE
                            rvDoctorUpcomingAppointments.visibility = View.GONE
                        } else {
                            tvNoDoctorUpcomingAppointments.visibility = View.GONE
                            rvDoctorUpcomingAppointments.visibility = View.VISIBLE
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error loading schedule: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun loadAdminData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // 1. Fetch doctors for approvals roster
                val docsResponse = RetrofitClient.appointmentApi.getAdminDoctors()
                // 2. Fetch all appointments to calculate KPIs
                val apptsResponse = RetrofitClient.appointmentApi.getAllAppointments()

                withContext(Dispatchers.Main) {
                    if (docsResponse.isSuccessful && docsResponse.body() != null) {
                        val doctors = docsResponse.body()!!.data ?: emptyList()
                        
                        // Active approved doctors count
                        val activeDocsCount = doctors.count { it.isApproved }
                        
                        // Pending unapproved doctors
                        val pending = doctors.filter { !it.isApproved }
                        pendingDocsList.clear()
                        pendingDocsList.addAll(pending)
                        adminAdapter.notifyDataSetChanged()

                        tvKpiActiveDoctors.text = activeDocsCount.toString()
                        tvKpiPendingDoctors.text = pending.size.toString()

                        if (pendingDocsList.isEmpty()) {
                            tvNoAdminApprovals.visibility = View.VISIBLE
                            rvAdminApprovals.visibility = View.GONE
                        } else {
                            tvNoAdminApprovals.visibility = View.GONE
                            rvAdminApprovals.visibility = View.VISIBLE
                        }
                    }

                    if (apptsResponse.isSuccessful && apptsResponse.body() != null) {
                        val totalAppts = apptsResponse.body()!!.data?.size ?: 0
                        tvKpiAppointments.text = totalAppts.toString()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error loading Admin data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showDoctorConfirmDialog(appointmentId: Long) {
        val builder = androidx.appcompat.app.AlertDialog.Builder(this)
        builder.setTitle("Confirm Appointment")
        builder.setMessage("Please enter the custom consultation/billing amount due (PHP):")

        val input = android.widget.EditText(this)
        input.inputType = android.view.inputmethod.EditorInfo.TYPE_CLASS_NUMBER or android.view.inputmethod.EditorInfo.TYPE_NUMBER_FLAG_DECIMAL
        input.hint = "e.g. 1500.00"
        builder.setView(input)

        builder.setPositiveButton("Confirm") { _, _ ->
            val amountStr = input.text.toString().trim()
            if (amountStr.isEmpty()) {
                Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show()
                return@setPositiveButton
            }
            try {
                val amount = amountStr.toDouble()
                if (amount <= 0) {
                    Toast.makeText(this, "Amount must be greater than zero", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                updateAppointmentStatus(appointmentId, "CONFIRMED", amountStr)
            } catch (e: Exception) {
                Toast.makeText(this, "Invalid number format", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancel") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun updateAppointmentStatus(appointmentId: Long, newStatus: String, amountDue: String? = null) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = mutableMapOf("status" to newStatus)
                if (amountDue != null) {
                    body["amountDue"] = amountDue
                }
                val response = RetrofitClient.appointmentApi.updateStatus(appointmentId, body)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@DashboardActivity, "Appointment updated to $newStatus!", Toast.LENGTH_SHORT).show()
                        loadDoctorAppointments()
                    } else {
                        Toast.makeText(this@DashboardActivity, "Failed to update status", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun approveDoctorRegistration(doctorId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.appointmentApi.approveDoctor(doctorId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@DashboardActivity, "Doctor approved successfully!", Toast.LENGTH_SHORT).show()
                        loadAdminData()
                    } else {
                        Toast.makeText(this@DashboardActivity, "Approval failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@DashboardActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun openAppointmentDetails(appointmentId: Long) {
        val intent = Intent(this, AppointmentDetailActivity::class.java)
        intent.putExtra("APPOINTMENT_ID", appointmentId)
        startActivity(intent)
    }
}
