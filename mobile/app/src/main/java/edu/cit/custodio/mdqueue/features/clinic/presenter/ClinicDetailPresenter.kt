package edu.cit.custodio.mdqueue.features.clinic.presenter

import edu.cit.custodio.mdqueue.features.queue.model.QueueResponse
import edu.cit.custodio.mdqueue.core.network.ApiResponse
import edu.cit.custodio.mdqueue.core.network.NetworkResult
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.features.clinic.ClinicDetailContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class ClinicDetailPresenter : ClinicDetailContract.Presenter {

    private var view: ClinicDetailContract.View? = null
    private var clinicJob: Job? = null
    private var queuesJob: Job? = null
    private var joinJob: Job? = null

    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<ApiResponse<T>>): NetworkResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.success && body.data != null) {
                    NetworkResult.Success(body.data)
                } else if (!body.success) {
                    NetworkResult.Error(body.message ?: "API Error")
                } else {
                    NetworkResult.Success(body.data!!) // Might be null, but we checked success
                }
            } else {
                NetworkResult.Error("API Error: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network Error: ${e.message}")
        }
    }

    override fun fetchClinicDetails(clinicId: Long) {
        view?.showClinicLoading()
        clinicJob?.cancel()
        clinicJob = CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall<edu.cit.custodio.mdqueue.features.clinic.model.ClinicResponse> { RetrofitClient.clinicApi.getClinicById(clinicId) }
            
            withContext(Dispatchers.Main) {
                view?.hideClinicLoading()
                when (result) {
                    is NetworkResult.Success -> {
                        result.data?.let { view?.showClinicDetails(it) } ?: view?.showClinicError("Clinic not found")
                    }
                    is NetworkResult.Error -> {
                        view?.showClinicError(result.message)
                    }
                    is NetworkResult.Loading -> { }
                }
            }
        }
    }

    override fun fetchClinicQueues(clinicId: Long) {
        view?.showQueuesLoading()
        queuesJob?.cancel()
        queuesJob = CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall<List<edu.cit.custodio.mdqueue.features.queue.model.QueueResponse>> { RetrofitClient.queueApi.getQueuesByClinic(clinicId) }
            
            withContext(Dispatchers.Main) {
                view?.hideQueuesLoading()
                when (result) {
                    is NetworkResult.Success -> {
                        val list = result.data ?: emptyList()
                        if (list.isEmpty()) {
                            view?.showNoQueuesState()
                        } else {
                            view?.showQueues(list)
                        }
                    }
                    is NetworkResult.Error -> {
                        view?.showQueuesError(result.message)
                        view?.showNoQueuesState()
                    }
                    is NetworkResult.Loading -> { }
                }
            }
        }
    }

    override fun joinQueue(queue: QueueResponse) {
        view?.showJoinQueueLoading()
        joinJob?.cancel()
        joinJob = CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall<edu.cit.custodio.mdqueue.features.queue.model.QueueEntryResponse> { RetrofitClient.queueEntryApi.joinQueue(queue.id) }
            
            withContext(Dispatchers.Main) {
                view?.hideJoinQueueLoading()
                when (result) {
                    is NetworkResult.Success -> {
                        result.data?.let { view?.onJoinQueueSuccess(it) } ?: view?.onJoinQueueError("Failed to join queue")
                    }
                    is NetworkResult.Error -> {
                        view?.onJoinQueueError(result.message ?: "Could not join queue.")
                    }
                    is NetworkResult.Loading -> { }
                }
            }
        }
    }

    override fun attachView(view: ClinicDetailContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
        clinicJob?.cancel()
        queuesJob?.cancel()
        joinJob?.cancel()
    }
}
