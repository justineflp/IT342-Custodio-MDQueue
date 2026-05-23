package edu.cit.custodio.mdqueue.features.queue.api

import edu.cit.custodio.mdqueue.core.network.ApiResponse
import edu.cit.custodio.mdqueue.features.queue.model.QueueRequest
import edu.cit.custodio.mdqueue.features.queue.model.QueueResponse
import retrofit2.Response
import retrofit2.http.*

interface QueueApiService {

    @POST("/api/queues/clinic/{clinicId}")
    suspend fun createQueue(
        @Path("clinicId") clinicId: Long,
        @Body request: QueueRequest
    ): Response<ApiResponse<QueueResponse>>

    @GET("/api/queues/clinic/{clinicId}")
    suspend fun getQueuesByClinic(@Path("clinicId") clinicId: Long): Response<ApiResponse<List<QueueResponse>>>

    @GET("/api/queues/{id}")
    suspend fun getQueueById(@Path("id") id: Long): Response<ApiResponse<QueueResponse>>

    @PATCH("/api/queues/{id}/status")
    suspend fun updateQueueStatus(
        @Path("id") id: Long,
        @Body body: Map<String, String>
    ): Response<ApiResponse<QueueResponse>>
}
