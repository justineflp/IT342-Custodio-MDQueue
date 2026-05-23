package edu.cit.custodio.mdqueue.core.network

import com.google.gson.annotations.SerializedName

data class ApiResponse<T>(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("status")
    val status: Int,
    
    @SerializedName("message")
    val message: String?,
    
    @SerializedName("data")
    val data: T?,
    
    @SerializedName("timestamp")
    val timestamp: String?
)
