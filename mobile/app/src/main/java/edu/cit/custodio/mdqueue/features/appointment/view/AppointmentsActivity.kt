package edu.cit.custodio.mdqueue.features.appointment.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.core.session.SessionManager
import edu.cit.custodio.mdqueue.features.appointment.model.AppointmentResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppointmentsActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvTitle: TextView
    private lateinit var rvAllAppointments: RecyclerView
    private lateinit var tvNoAppointmentsPlaceholder: TextView

    private lateinit var chipAll: TextView
    private lateinit var chipPending: TextView
    private lateinit var chipConfirmed: TextView
    private lateinit var chipCompleted: TextView
    private lateinit var chipCancelled: TextView

    private lateinit var sessionManager: SessionManager
    private val masterAppointmentsList = mutableListOf<AppointmentResponse>()
    private val appointmentsList = mutableListOf<AppointmentResponse>()
    private lateinit var adapter: AppointmentsAdapter
    private var currentFilter = "ALL"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointments)

        sessionManager = SessionManager(this)

        initViews()
        setupListeners()
        loadAppointments()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitle = findViewById(R.id.tvAppointmentsTitle)
        rvAllAppointments = findViewById(R.id.rvAllAppointments)
        tvNoAppointmentsPlaceholder = findViewById(R.id.tvNoAppointmentsPlaceholder)

        chipAll = findViewById(R.id.chipAll)
        chipPending = findViewById(R.id.chipPending)
        chipConfirmed = findViewById(R.id.chipConfirmed)
        chipCompleted = findViewById(R.id.chipCompleted)
        chipCancelled = findViewById(R.id.chipCancelled)

        val role = sessionManager.getRole() ?: "PATIENT"
        when (role) {
            "DOCTOR" -> tvTitle.text = "Scheduled Appointments"
            "ADMIN" -> tvTitle.text = "Completed System Records"
            else -> tvTitle.text = "My Appointments"
        }

        rvAllAppointments.layoutManager = LinearLayoutManager(this)
        adapter = AppointmentsAdapter(
            appointments = appointmentsList,
            isDoctorOrAdmin = (role == "DOCTOR" || role == "ADMIN"),
            onItemClick = { appt ->
                val intent = Intent(this, AppointmentDetailActivity::class.java)
                intent.putExtra("APPOINTMENT_ID", appt.id)
                startActivity(intent)
            }
        )
        rvAllAppointments.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
        chipAll.setOnClickListener { selectFilter("ALL") }
        chipPending.setOnClickListener { selectFilter("PENDING") }
        chipConfirmed.setOnClickListener { selectFilter("CONFIRMED") }
        chipCompleted.setOnClickListener { selectFilter("COMPLETED") }
        chipCancelled.setOnClickListener { selectFilter("CANCELLED") }
    }

    private fun selectFilter(filter: String) {
        currentFilter = filter

        val chips = listOf(chipAll, chipPending, chipConfirmed, chipCompleted, chipCancelled)
        val filterTypes = listOf("ALL", "PENDING", "CONFIRMED", "COMPLETED", "CANCELLED")

        for (i in chips.indices) {
            val chip = chips[i]
            val type = filterTypes[i]
            if (type == currentFilter) {
                chip.setBackgroundResource(R.drawable.bg_chip_selected)
                chip.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.primary))
            } else {
                chip.setBackgroundResource(R.drawable.bg_chip_unselected)
                chip.setTextColor(androidx.core.content.ContextCompat.getColor(this, R.color.text_secondary))
            }
        }

        appointmentsList.clear()
        if (currentFilter == "ALL") {
            appointmentsList.addAll(masterAppointmentsList)
        } else {
            appointmentsList.addAll(masterAppointmentsList.filter { it.status == currentFilter })
        }
        adapter.notifyDataSetChanged()

        if (appointmentsList.isEmpty()) {
            tvNoAppointmentsPlaceholder.visibility = View.VISIBLE
            rvAllAppointments.visibility = View.GONE
        } else {
            tvNoAppointmentsPlaceholder.visibility = View.GONE
            rvAllAppointments.visibility = View.VISIBLE
        }
    }

    private fun loadAppointments() {
        val role = sessionManager.getRole() ?: "PATIENT"
        
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = if (role == "ADMIN") {
                    RetrofitClient.appointmentApi.getAllAppointments()
                } else {
                    RetrofitClient.appointmentApi.getMyAppointments()
                }

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()!!.data ?: emptyList()

                        masterAppointmentsList.clear()
                        masterAppointmentsList.addAll(list)
                        
                        selectFilter(currentFilter)
                    } else {
                        Toast.makeText(this@AppointmentsActivity, "Failed to load appointments", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AppointmentsActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
