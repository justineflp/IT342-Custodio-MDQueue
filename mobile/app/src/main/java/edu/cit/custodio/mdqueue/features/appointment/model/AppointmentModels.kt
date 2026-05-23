package edu.cit.custodio.mdqueue.features.appointment.model

import com.google.gson.annotations.SerializedName

data class AppointmentRequest(
    @SerializedName("doctorId") val doctorId: Long,
    @SerializedName("appointmentDatetime") val appointmentDatetime: String,
    @SerializedName("reason") val reason: String
)

data class AppointmentResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("patientId") val patientId: Long,
    @SerializedName("patientName") val patientName: String,
    @SerializedName("doctorId") val doctorId: Long,
    @SerializedName("doctorName") val doctorName: String,
    @SerializedName("appointmentDatetime") val appointmentDatetime: String,
    @SerializedName("reason") val reason: String,
    @SerializedName("status") val status: String
)

data class PaymentRequest(
    @SerializedName("paymentMethodId") val paymentMethodId: String
)

data class PaymentResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("status") val status: String
)

data class DocumentResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("fileName") val fileName: String,
    @SerializedName("fileType") val fileType: String,
    @SerializedName("uploadedAt") val uploadedAt: String
)

data class DoctorResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("fullName") val fullName: String,
    @SerializedName("email") val email: String?,
    @SerializedName("specialty") val specialty: String?,
    @SerializedName("initials") val initials: String?,
    @SerializedName("color") val color: String?
)
