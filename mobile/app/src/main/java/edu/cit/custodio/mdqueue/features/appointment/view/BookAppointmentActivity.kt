package edu.cit.custodio.mdqueue.features.appointment.view

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.features.appointment.model.AppointmentRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var etDoctorId: EditText
    private lateinit var etDatetime: EditText
    private lateinit var etReason: EditText
    private lateinit var btnSubmit: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)

        etDoctorId = findViewById(R.id.etDoctorId)
        etDatetime = findViewById(R.id.etDatetime)
        etReason = findViewById(R.id.etReason)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnSubmit.setOnClickListener {
            bookAppointment()
        }
    }

    private fun bookAppointment() {
        val doctorId = etDoctorId.text.toString().toLongOrNull() ?: 0L
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
