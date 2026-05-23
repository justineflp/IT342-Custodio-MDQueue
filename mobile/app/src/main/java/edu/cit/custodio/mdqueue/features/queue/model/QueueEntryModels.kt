package edu.cit.custodio.mdqueue.features.queue.model

import com.google.gson.annotations.SerializedName

data class QueueEntryResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("queueId")
    val queueId: Long,
    
    @SerializedName("queueName")
    val queueName: String?,
    
    @SerializedName("clinicId")
    val clinicId: Long?,
    
    @SerializedName("clinicName")
    val clinicName: String?,
    
    @SerializedName("patientId")
    val patientId: Long?,
    
    @SerializedName("patientName")
    val patientName: String?,
    
    @SerializedName("queueNumber")
    val queueNumber: Int,
    
    @SerializedName("status")
    val status: String, // WAITING, SERVING, COMPLETED, CANCELLED
    
    @SerializedName("checkInTime")
    val checkInTime: String?,
    
    @SerializedName("servedTime")
    val servedTime: String?,
    
    @SerializedName("completedTime")
    val completedTime: String?,
    
    @SerializedName("positionInQueue")
    val positionInQueue: Long,
    
    @SerializedName("peopleAhead")
    val peopleAhead: Long
)
