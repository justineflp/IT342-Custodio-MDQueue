package edu.cit.custodio.mdqueue.features.clinic.presenter

import edu.cit.custodio.mdqueue.core.network.ApiResponse
import edu.cit.custodio.mdqueue.core.network.NetworkResult
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.features.clinic.ClinicListContract
import edu.cit.custodio.mdqueue.features.clinic.model.ClinicResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class ClinicListPresenter : ClinicListContract.Presenter {

    private var view: ClinicListContract.View? = null
    private var job: Job? = null

    private suspend fun <T> safeApiCall(apiCall: suspend () -> Response<ApiResponse<T>>): NetworkResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful && response.body() != null) {
                val body = response.body()!!
                if (body.data != null) {
                    NetworkResult.Success(body.data)
                } else {
                    NetworkResult.Error("No data available")
                }
            } else {
                NetworkResult.Error("API Error: ${response.message()}")
            }
        } catch (e: Exception) {
            NetworkResult.Error("Network Error: ${e.message}")
        }
    }

    override fun fetchClinics(query: String?) {
        view?.showLoading()
        job?.cancel()
        job = CoroutineScope(Dispatchers.IO).launch {
            val result = safeApiCall<List<edu.cit.custodio.mdqueue.features.clinic.model.ClinicResponse>> { RetrofitClient.clinicApi.getAllClinics(query) }
            
            withContext(Dispatchers.Main) {
                view?.hideLoading()
                when (result) {
                    is NetworkResult.Success -> {
                        val list = result.data ?: emptyList()
                        if (list.isEmpty()) {
                            view?.showEmptyState()
                        } else {
                            view?.showClinics(list)
                        }
                    }
                    is NetworkResult.Error -> {
                        view?.showError(result.message)
                    }
                    is NetworkResult.Loading -> { }
                }
            }
        }
    }

    override fun attachView(view: ClinicListContract.View) {
        this.view = view
    }

    override fun detachView() {
        this.view = null
        job?.cancel()
    }
}
