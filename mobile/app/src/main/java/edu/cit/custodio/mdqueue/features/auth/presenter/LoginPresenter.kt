package edu.cit.custodio.mdqueue.features.auth.presenter

import edu.cit.custodio.mdqueue.core.network.NetworkResult
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.features.auth.LoginContract
import edu.cit.custodio.mdqueue.features.auth.model.LoginRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class LoginPresenter(private val view: LoginContract.View) : LoginContract.Presenter {

    override fun login(email: String, password: String) {
        view.onLoginResult(NetworkResult.Loading())

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.authApi.login(
                    LoginRequest(email = email, password = password)
                )

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse = response.body()!!
                        
                        if (apiResponse.success && apiResponse.data != null) {
                            view.onLoginResult(NetworkResult.Success(apiResponse.data))
                        } else {
                            view.onLoginResult(NetworkResult.Error(apiResponse.message ?: "Login failed"))
                        }
                    } else {
                        val errorMsg = when (response.code()) {
                            401 -> "Invalid email or password"
                            403 -> "Invalid email or password"
                            else -> "Login failed. Please try again."
                        }
                        view.onLoginResult(NetworkResult.Error(errorMsg))
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.onLoginResult(NetworkResult.Error("Network error. Please try again later."))
                }
            }
        }
    }
}
