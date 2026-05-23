package edu.cit.custodio.mdqueue.features.auth.presenter

import edu.cit.custodio.mdqueue.core.network.NetworkResult
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.features.auth.RegisterContract
import edu.cit.custodio.mdqueue.features.auth.model.RegisterRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RegisterPresenter(private val view: RegisterContract.View) : RegisterContract.Presenter {

    override fun register(fullName: String, email: String, phoneNumber: String, password: String, confirmPassword: String, role: String) {
        view.onRegisterResult(NetworkResult.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.authApi.register(
                    RegisterRequest(
                        fullName = fullName,
                        email = email,
                        phoneNumber = phoneNumber,
                        password = password,
                        confirmPassword = confirmPassword,
                        role = role
                    )
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse = response.body()!!
                        
                        if (apiResponse.success && apiResponse.data != null) {
                            view.onRegisterResult(NetworkResult.Success(apiResponse.data))
                        } else {
                            view.onRegisterResult(NetworkResult.Error(apiResponse.message ?: "Registration failed"))
                        }
                    } else {
                        val errorMsg = when (response.code()) {
                            400 -> "Invalid details provided"
                            409 -> "Email already exists"
                            else -> "Registration failed. Please try again."
                        }
                        view.onRegisterResult(NetworkResult.Error(errorMsg))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.onRegisterResult(NetworkResult.Error("Network error. Please try again later."))
                }
            }
        }
    }
}
