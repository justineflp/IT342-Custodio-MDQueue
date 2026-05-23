package edu.cit.custodio.mdqueue.features.appointment.view

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.features.appointment.model.AppointmentResponse
import edu.cit.custodio.mdqueue.features.appointment.model.DocumentResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class AppointmentDetailActivity : AppCompatActivity() {

    private lateinit var btnBack: Button
    private lateinit var tvStatusBadge: TextView
    private lateinit var tvApptDatetime: TextView
    private lateinit var tvParticipant: TextView
    private lateinit var tvReason: TextView
    private lateinit var btnUpload: Button
    private lateinit var tvNoDocuments: TextView
    private lateinit var layoutDocumentsList: LinearLayout

    private var appointmentId: Long = 0L
    private val documentsList = mutableListOf<DocumentResponse>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_appointment_detail)

        appointmentId = intent.getLongExtra("APPOINTMENT_ID", 0L)

        btnBack = findViewById(R.id.btnBack)
        tvStatusBadge = findViewById(R.id.tvStatusBadge)
        tvApptDatetime = findViewById(R.id.tvApptDatetime)
        tvParticipant = findViewById(R.id.tvParticipant)
        tvReason = findViewById(R.id.tvReason)
        btnUpload = findViewById(R.id.btnUpload)
        tvNoDocuments = findViewById(R.id.tvNoDocuments)
        layoutDocumentsList = findViewById(R.id.layoutDocumentsList)

        btnBack.setOnClickListener { finish() }

        btnUpload.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "application/pdf"
            startActivityForResult(intent, 100)
        }

        loadAppointmentDetails()
        loadDocuments()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 100 && resultCode == Activity.RESULT_OK && data != null) {
            data.data?.let { uploadFile(it) }
        }
    }

    private fun loadAppointmentDetails() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.appointmentApi.getAppointmentDetails(appointmentId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val appt = response.body()!!.data
                        if (appt != null) {
                            displayDetails(appt)
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AppointmentDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun displayDetails(appt: AppointmentResponse) {
        tvStatusBadge.text = appt.status
        tvApptDatetime.text = "Date & Time: ${appt.appointmentDatetime}"
        tvParticipant.text = "Doctor: Dr. ${appt.doctorName}\nPatient: ${appt.patientName}"
        tvReason.text = "Reason: ${appt.reason}"
    }

    private fun loadDocuments() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.appointmentApi.getDocuments(appointmentId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        documentsList.clear()
                        response.body()!!.data?.let { documentsList.addAll(it) }
                        refreshDocumentsList()
                    }
                }
            } catch (e: Exception) {
                // Ignore or log
            }
        }
    }

    private fun refreshDocumentsList() {
        if (documentsList.isEmpty()) {
            tvNoDocuments.visibility = View.VISIBLE
            layoutDocumentsList.visibility = View.GONE
            return
        }

        tvNoDocuments.visibility = View.GONE
        layoutDocumentsList.visibility = View.VISIBLE
        layoutDocumentsList.removeAllViews()

        val inflater = LayoutInflater.from(this)

        for (doc in documentsList) {
            val itemView = inflater.inflate(R.layout.item_medical_document, layoutDocumentsList, false)
            val tvFileName = itemView.findViewById<TextView>(R.id.tvFileName)
            val tvUploadedAt = itemView.findViewById<TextView>(R.id.tvUploadedAt)
            val btnDownload = itemView.findViewById<Button>(R.id.btnDownload)

            tvFileName.text = doc.fileName
            tvUploadedAt.text = "Uploaded: ${doc.uploadedAt}"

            btnDownload.setOnClickListener {
                downloadDocument(doc.id)
            }

            layoutDocumentsList.addView(itemView)
        }
    }

    private fun uploadFile(uri: Uri) {
        btnUpload.isEnabled = false
        btnUpload.text = "Uploading..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tempFile = copyUriToTempFile(uri)
                if (tempFile == null) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@AppointmentDetailActivity, "Failed to read file", Toast.LENGTH_SHORT).show()
                        resetUploadBtn()
                    }
                    return@launch
                }

                val requestFile = tempFile.asRequestBody(contentResolver.getType(uri)?.toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)

                val response = RetrofitClient.appointmentApi.uploadDocument(appointmentId, body)
                tempFile.delete()

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.success) {
                        Toast.makeText(this@AppointmentDetailActivity, "Uploaded successfully", Toast.LENGTH_SHORT).show()
                        response.body()!!.data?.let {
                            documentsList.add(0, it)
                            refreshDocumentsList()
                        }
                    } else {
                        Toast.makeText(this@AppointmentDetailActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                    resetUploadBtn()
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AppointmentDetailActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    resetUploadBtn()
                }
            }
        }
    }

    private fun resetUploadBtn() {
        btnUpload.isEnabled = true
        btnUpload.text = "+ Upload"
    }

    private fun downloadDocument(docId: Long) {
        val downloadUrl = "http://10.0.2.2:8080/api/appointments/documents/$docId"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(downloadUrl))
        startActivity(intent)
    }

    private fun copyUriToTempFile(uri: Uri): File? {
        try {
            val inputStream = contentResolver.openInputStream(uri) ?: return null
            var fileName = "temp_upload"
            contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val displayNameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (displayNameIndex != -1) {
                        fileName = cursor.getString(displayNameIndex)
                    }
                }
            }

            val tempFile = File(cacheDir, fileName)
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            inputStream.close()
            outputStream.close()
            return tempFile
        } catch (e: Exception) {
            return null
        }
    }
}
