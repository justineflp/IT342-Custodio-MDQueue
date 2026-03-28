package edu.cit.custodio.mdqueue.api.models

import com.google.gson.annotations.SerializedName

data class AuthResponse(
    @SerializedName("token")
    val token: String?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("userId")
    val userId: Long?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("fullName")
    val fullName: String?,

    @SerializedName("message")
    val message: String?
)
