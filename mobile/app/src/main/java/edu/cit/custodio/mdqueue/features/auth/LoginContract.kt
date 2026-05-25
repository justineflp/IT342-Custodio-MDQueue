package edu.cit.custodio.mdqueue.features.auth

import edu.cit.custodio.mdqueue.core.network.NetworkResult
import edu.cit.custodio.mdqueue.features.auth.model.AuthResponse

interface LoginContract {
    interface View {
        fun onLoginResult(result: NetworkResult<AuthResponse>)
    }

    interface Presenter {
        fun login(email: String, password: String)
        fun loginWithGoogle(token: String)
    }
}
