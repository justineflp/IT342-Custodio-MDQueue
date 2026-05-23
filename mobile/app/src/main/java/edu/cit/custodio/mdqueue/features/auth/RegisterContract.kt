package edu.cit.custodio.mdqueue.features.auth

import edu.cit.custodio.mdqueue.core.network.NetworkResult
import edu.cit.custodio.mdqueue.features.auth.model.AuthResponse

interface RegisterContract {
    interface View {
        fun onRegisterResult(result: NetworkResult<AuthResponse>)
    }

    interface Presenter {
        fun register(fullName: String, email: String, phoneNumber: String, password: String, confirmPassword: String, role: String)
    }
}
