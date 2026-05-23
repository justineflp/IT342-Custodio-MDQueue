package edu.cit.custodio.mdqueue.features.clinic.model

import com.google.gson.annotations.SerializedName

data class ClinicRequest(
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("phoneNumber")
    val phoneNumber: String,
    
    @SerializedName("description")
    val description: String,
    
    @SerializedName("openingTime")
    val openingTime: String,
    
    @SerializedName("closingTime")
    val closingTime: String
)

data class ClinicResponse(
    @SerializedName("id")
    val id: Long,
    
    @SerializedName("name")
    val name: String,
    
    @SerializedName("address")
    val address: String,
    
    @SerializedName("phoneNumber")
    val phoneNumber: String?,
    
    @SerializedName("description")
    val description: String?,
    
    @SerializedName("openingTime")
    val openingTime: String?,
    
    @SerializedName("closingTime")
    val closingTime: String?,
    
    @SerializedName("ownerId")
    val ownerId: Long?,
    
    @SerializedName("ownerName")
    val ownerName: String?,
    
    @SerializedName("activeQueues")
    val activeQueues: Int
)
