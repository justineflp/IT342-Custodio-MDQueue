package edu.cit.custodio.mdqueue.features.appointment.api

import edu.cit.custodio.mdqueue.features.appointment.model.*
import edu.cit.custodio.mdqueue.core.network.ApiResponse
import retrofit2.Response
import retrofit2.http.*

interface AppointmentApiService {
    @POST("appointments")
    suspend fun createAppointment(@Body request: AppointmentRequest): Response<ApiResponse<AppointmentResponse>>

    @GET("appointments/me")
    suspend fun getMyAppointments(): Response<ApiResponse<List<AppointmentResponse>>>

    @GET("appointments/{id}")
    suspend fun getAppointmentDetails(@Path("id") id: Long): Response<ApiResponse<AppointmentResponse>>

    @PATCH("appointments/{id}/status")
    suspend fun updateStatus(@Path("id") id: Long, @Body statusUpdate: Map<String, String>): Response<ApiResponse<AppointmentResponse>>

    @POST("payments/{id}/process")
    suspend fun processPayment(@Path("id") id: Long, @Body request: PaymentRequest): Response<ApiResponse<PaymentResponse>>

    @Multipart
    @POST("appointments/{id}/documents")
    suspend fun uploadDocument(
        @Path("id") appointmentId: Long,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<ApiResponse<DocumentResponse>>

    @GET("appointments/{id}/documents")
    suspend fun getDocuments(@Path("id") appointmentId: Long): Response<ApiResponse<List<DocumentResponse>>>

    @GET("users/doctors")
    suspend fun getDoctors(): Response<ApiResponse<List<DoctorResponse>>>
}
