package edu.cit.custodio.mdqueue.api.models

import com.google.gson.annotations.SerializedName

data class RegisterRequest(
    @SerializedName("fullName")
    val fullName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("phoneNumber")
    val phoneNumber: String,

    @SerializedName("password")
    val password: String,

    @SerializedName("confirmPassword")
    val confirmPassword: String
)
