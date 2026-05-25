package edu.cit.custodio.mdqueue.features.appointment.api

import edu.cit.custodio.mdqueue.features.appointment.model.*
import edu.cit.custodio.mdqueue.core.network.ApiResponse
import retrofit2.Response
import retrofit2.http.*

interface AppointmentApiService {
    @POST("/api/appointments")
    suspend fun createAppointment(@Body request: AppointmentRequest): Response<ApiResponse<AppointmentResponse>>

    @GET("/api/appointments/me")
    suspend fun getMyAppointments(): Response<ApiResponse<List<AppointmentResponse>>>

    @GET("/api/appointments/{id}")
    suspend fun getAppointmentDetails(@Path("id") id: Long): Response<ApiResponse<AppointmentResponse>>

    @PATCH("/api/appointments/{id}/status")
    suspend fun updateStatus(@Path("id") id: Long, @Body statusUpdate: Map<String, String>): Response<ApiResponse<AppointmentResponse>>

    @POST("/api/payments/{id}/process")
    suspend fun processPayment(@Path("id") id: Long, @Body request: PaymentRequest): Response<ApiResponse<PaymentResponse>>

    @Multipart
    @POST("/api/appointments/{id}/documents")
    suspend fun uploadDocument(
        @Path("id") appointmentId: Long,
        @Part file: okhttp3.MultipartBody.Part
    ): Response<ApiResponse<DocumentResponse>>

    @GET("/api/appointments/{id}/documents")
    suspend fun getDocuments(@Path("id") appointmentId: Long): Response<ApiResponse<List<DocumentResponse>>>

    @GET("/api/users/doctors")
    suspend fun getDoctors(): Response<ApiResponse<List<DoctorResponse>>>

    @GET("/api/users/admin/doctors")
    suspend fun getAdminDoctors(): Response<ApiResponse<List<AdminDoctorResponse>>>

    @PATCH("/api/users/admin/doctors/{id}/approve")
    suspend fun approveDoctor(@Path("id") id: Long): Response<ApiResponse<String>>

    @PATCH("/api/users/me/specialty")
    suspend fun updateSpecialty(@Body payload: Map<String, String>): Response<ApiResponse<String>>

    @GET("/api/appointments/all")
    suspend fun getAllAppointments(): Response<ApiResponse<List<AppointmentResponse>>>

    @GET("/api/users/me")
    suspend fun getUserProfile(): Response<ApiResponse<UserProfileResponse>>

    @PUT("/api/users/me/password")
    suspend fun changePassword(@Body payload: Map<String, String>): Response<ApiResponse<String>>
}
