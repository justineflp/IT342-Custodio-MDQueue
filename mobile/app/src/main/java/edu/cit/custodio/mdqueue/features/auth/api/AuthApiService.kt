package edu.cit.custodio.mdqueue.features.auth.api

import edu.cit.custodio.mdqueue.core.network.ApiResponse
import edu.cit.custodio.mdqueue.features.auth.model.AuthResponse
import edu.cit.custodio.mdqueue.features.auth.model.LoginRequest
import edu.cit.custodio.mdqueue.features.auth.model.RegisterRequest
import edu.cit.custodio.mdqueue.features.auth.model.GoogleLoginRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApiService {

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<ApiResponse<AuthResponse>>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<ApiResponse<AuthResponse>>

    @POST("/api/auth/google")
    suspend fun loginWithGoogle(@Body request: GoogleLoginRequest): Response<ApiResponse<AuthResponse>>
}
