package edu.cit.custodio.mdqueue.features.clinic.api

import edu.cit.custodio.mdqueue.core.network.ApiResponse
import edu.cit.custodio.mdqueue.features.clinic.model.ClinicRequest
import edu.cit.custodio.mdqueue.features.clinic.model.ClinicResponse
import retrofit2.Response
import retrofit2.http.*

interface ClinicApiService {

    @POST("/api/clinics")
    suspend fun createClinic(@Body request: ClinicRequest): Response<ApiResponse<ClinicResponse>>

    @GET("/api/clinics")
    suspend fun getAllClinics(@Query("search") search: String? = null): Response<ApiResponse<List<ClinicResponse>>>

    @GET("/api/clinics/{id}")
    suspend fun getClinicById(@Path("id") id: Long): Response<ApiResponse<ClinicResponse>>

    @GET("/api/clinics/mine")
    suspend fun getMyClinics(): Response<ApiResponse<List<ClinicResponse>>>
}
