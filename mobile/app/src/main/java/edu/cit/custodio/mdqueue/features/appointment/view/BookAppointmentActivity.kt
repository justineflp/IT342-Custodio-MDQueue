package edu.cit.custodio.mdqueue.features.appointment.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.features.appointment.model.AppointmentRequest
import edu.cit.custodio.mdqueue.features.appointment.model.DoctorResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var spinnerDoctors: Spinner
    private lateinit var etDatetime: EditText
    private lateinit var etReason: EditText
    private lateinit var btnSubmit: Button

    private val doctorsList = mutableListOf<DoctorResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)

        spinnerDoctors = findViewById(R.id.spinnerDoctors)
        etDatetime = findViewById(R.id.etDatetime)
        etReason = findViewById(R.id.etReason)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            bookAppointment()
        }

        loadDoctors()
    }

    private fun loadDoctors() {
        btnSubmit.isEnabled = false
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.appointmentApi.getDoctors()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val docs = response.body()!!.data ?: emptyList()
                        doctorsList.clear()
                        doctorsList.addAll(docs)

                        val displayNames = docs.map { doc ->
                            "Dr. ${doc.fullName}${if (!doc.specialty.isNullOrEmpty()) " - ${doc.specialty}" else ""}"
                        }

                        val adapter = ArrayAdapter(
                            this@BookAppointmentActivity,
                            android.R.layout.simple_spinner_item,
                            displayNames
                        )
                        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                        spinnerDoctors.adapter = adapter
                        btnSubmit.isEnabled = true
                    } else {
                        Toast.makeText(this@BookAppointmentActivity, "Failed to load doctors list.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BookAppointmentActivity, "Error loading doctors: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun bookAppointment() {
        val selectedIndex = spinnerDoctors.selectedItemPosition
        if (selectedIndex == -1 || selectedIndex >= doctorsList.size) {
            Toast.makeText(this, "Please select a doctor.", Toast.LENGTH_SHORT).show()
            return
        }

        val doctorId = doctorsList[selectedIndex].id
        val datetime = etDatetime.text.toString()
        val reason = etReason.text.toString()

        if (datetime.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            return
        }

        btnSubmit.isEnabled = false
        btnSubmit.text = "Booking..."

        val request = AppointmentRequest(doctorId, datetime, reason)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.appointmentApi.createAppointment(request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@BookAppointmentActivity, "Appointment Booked!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@BookAppointmentActivity, "Booking failed", Toast.LENGTH_SHORT).show()
                        resetButton()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@BookAppointmentActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    resetButton()
                }
            }
        }
    }

    private fun resetButton() {
        btnSubmit.isEnabled = true
        btnSubmit.text = "Confirm Appointment"
    }
}
