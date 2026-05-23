package edu.cit.custodio.mdqueue.features.queue.model

import com.google.gson.annotations.SerializedName

data class QueueRequest(
    @SerializedName("name")
    val name: String
)

data class QueueResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("clinicId")
    val clinicId: Long,
    
    @SerializedName("clinicName")
    val clinicName: String?,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("status")
    val status: String, // OPEN, CLOSED
    
    @SerializedName("currentNumber")
    val currentNumber: Int,
    
    @SerializedName("waitingCount")
    val waitingCount: Long,
    
    @SerializedName("createdAt")
    val createdAt: String?
)
