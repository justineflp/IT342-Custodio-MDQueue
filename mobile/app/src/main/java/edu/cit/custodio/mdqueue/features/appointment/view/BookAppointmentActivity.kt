package edu.cit.custodio.mdqueue.features.appointment.view

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.ArrayAdapter
import android.widget.ImageButton
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
import java.util.Calendar
import java.util.Locale

class BookAppointmentActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var spinnerDoctors: Spinner
    private lateinit var etDatetime: EditText
    private lateinit var etReason: EditText
    private lateinit var btnSubmit: Button

    private val doctorsList = mutableListOf<DoctorResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_book_appointment)

        btnBack = findViewById(R.id.btnBack)
        spinnerDoctors = findViewById(R.id.spinnerDoctors)
        etDatetime = findViewById(R.id.etDatetime)
        etReason = findViewById(R.id.etReason)
        btnSubmit = findViewById(R.id.btnSubmit)

        btnBack.setOnClickListener {
            finish()
        }

        // Make etDatetime non-editable and focusless, so it launches picker dialogs on tap
        etDatetime.isFocusable = false
        etDatetime.isClickable = true
        etDatetime.setOnClickListener {
            showDateTimePicker()
        }

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

        try {
            val sdf = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", java.util.Locale.US)
            val selectedDate = sdf.parse(datetime)
            if (selectedDate != null && selectedDate.before(java.util.Date())) {
                Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show()
                return
            }
        } catch (e: Exception) {
            // Ignore parse errors here, backend will validate
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
                        var errorMsg = "Booking failed"
                        try {
                            val errorBodyStr = response.errorBody()?.string()
                            if (!errorBodyStr.isNullOrEmpty()) {
                                val errorObj = org.json.JSONObject(errorBodyStr)
                                if (errorObj.has("message")) {
                                    errorMsg = errorObj.getString("message")
                                }
                                if (errorObj.has("data") && errorObj.get("data") is org.json.JSONObject) {
                                    val dataObj = errorObj.getJSONObject("data")
                                    if (dataObj.keys().hasNext()) {
                                        val firstKey = dataObj.keys().next()
                                        errorMsg = dataObj.getString(firstKey)
                                    }
                                }
                            } else if (response.body() != null) {
                                errorMsg = response.body()?.message ?: "Booking failed"
                            }
                        } catch (e: Exception) {}
                        Toast.makeText(this@BookAppointmentActivity, errorMsg, Toast.LENGTH_SHORT).show()
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

    private fun showDateTimePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            this,
            { _, selectedYear, selectedMonth, selectedDay ->
                showTimePicker(selectedYear, selectedMonth, selectedDay)
            },
            year, month, day
        )
        // Restrict to future dates only (appointment must be in the future)
        datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000
        datePickerDialog.show()
    }

    private fun showTimePicker(year: Int, month: Int, day: Int) {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            this,
            { _, selectedHour, selectedMinute ->
                val formattedMonth = String.format(Locale.US, "%02d", month + 1)
                val formattedDay = String.format(Locale.US, "%02d", day)
                val formattedHour = String.format(Locale.US, "%02d", selectedHour)
                val formattedMinute = String.format(Locale.US, "%02d", selectedMinute)
                
                // Format: YYYY-MM-DDTHH:MM:SS
                val isoDateTime = "$year-$formattedMonth-${formattedDay}T$formattedHour:$formattedMinute:00"
                etDatetime.setText(isoDateTime)
            },
            hour, minute, true
        )
        timePickerDialog.show()
    }

    private fun resetButton() {
        btnSubmit.isEnabled = true
        btnSubmit.text = "Confirm Appointment"
    }
}
