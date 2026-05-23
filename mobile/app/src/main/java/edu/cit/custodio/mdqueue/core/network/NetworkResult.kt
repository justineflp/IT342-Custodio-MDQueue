package edu.cit.custodio.mdqueue.core.network

sealed class NetworkResult<out T> {
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
    class Loading<out T> : NetworkResult<T>()
}
