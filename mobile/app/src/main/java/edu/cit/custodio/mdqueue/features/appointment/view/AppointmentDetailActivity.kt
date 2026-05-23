package edu.cit.custodio.mdqueue.features.appointment.view

import android.content.Intent
import android.net.Uri
import edu.cit.custodio.mdqueue.features.appointment.model.DocumentResponse
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
        
        // Quick way to check role - if doctorName is set, and patientName is set...
        // Actually, we can check SessionManager if we want, but let's just display both or relevant.
        // For simplicity, we just display doctor's name here if available.
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
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        for (doc in documentsList) {
            val itemView = inflater.inflate(R.layout.item_medical_document, layoutDocumentsList, false)
            val tvFileName = itemView.findViewById<TextView>(R.id.tvFileName)
            val tvUploadedAt = itemView.findViewById<TextView>(R.id.tvUploadedAt)
            val btnDownload = itemView.findViewById<Button>(R.id.btnDownload)

            tvFileName.text = doc.fileName
            
            // Format date if possible, otherwise use raw
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
                // We need to copy the Uri contents to a temporary file because Retrofit needs a File
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
                
                // Cleanup temp file
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
            
            // Get original file name
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
