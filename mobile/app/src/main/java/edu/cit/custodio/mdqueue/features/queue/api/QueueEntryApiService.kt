package edu.cit.custodio.mdqueue.features.queue.api

import edu.cit.custodio.mdqueue.core.network.ApiResponse
import edu.cit.custodio.mdqueue.features.queue.model.QueueEntryResponse
import retrofit2.Response
import retrofit2.http.*

interface QueueEntryApiService {

    @POST("/api/queue-entries/join/{queueId}")
    suspend fun joinQueue(@Path("queueId") queueId: Long): Response<ApiResponse<QueueEntryResponse>>

    @GET("/api/queue-entries/queue/{queueId}")
    suspend fun getEntriesByQueue(@Path("queueId") queueId: Long): Response<ApiResponse<List<QueueEntryResponse>>>

    @GET("/api/queue-entries/queue/{queueId}/waiting")
    suspend fun getWaitingEntries(@Path("queueId") queueId: Long): Response<ApiResponse<List<QueueEntryResponse>>>

    @GET("/api/queue-entries/my")
    suspend fun getMyEntries(): Response<ApiResponse<List<QueueEntryResponse>>>

    @GET("/api/queue-entries/my/active")
    suspend fun getMyActiveEntries(): Response<ApiResponse<List<QueueEntryResponse>>>

    @PATCH("/api/queue-entries/serve-next/{queueId}")
    suspend fun serveNext(@Path("queueId") queueId: Long): Response<ApiResponse<QueueEntryResponse>>

    @PATCH("/api/queue-entries/{id}/complete")
    suspend fun completeEntry(@Path("id") id: Long): Response<ApiResponse<QueueEntryResponse>>

    @PATCH("/api/queue-entries/{id}/cancel")
    suspend fun cancelEntry(@Path("id") id: Long): Response<ApiResponse<QueueEntryResponse>>
}
