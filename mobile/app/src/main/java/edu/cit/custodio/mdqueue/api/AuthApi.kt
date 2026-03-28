package edu.cit.custodio.mdqueue.api

import edu.cit.custodio.mdqueue.api.models.AuthResponse
import edu.cit.custodio.mdqueue.api.models.LoginRequest
import edu.cit.custodio.mdqueue.api.models.RegisterRequest
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {

    @POST("/api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @POST("/api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>
}
