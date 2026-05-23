package edu.cit.custodio.mdqueue.features.dashboard.presenter

import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.features.dashboard.DashboardContract
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DashboardPresenter(
    private val view: DashboardContract.View
) : DashboardContract.Presenter {

    override fun fetchUpcomingAppointment() {
        view.showLoading()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.appointmentApi.getMyAppointments()
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    if (response.isSuccessful && response.body() != null) {
                        val activeEntries = response.body()!!.data ?: emptyList<edu.cit.custodio.mdqueue.features.appointment.model.AppointmentResponse>()
                        
                        if (activeEntries.isNotEmpty()) {
                            view.showUpcomingAppointment(activeEntries[0])
                        } else {
                            view.showNoAppointments()
                        }
                    } else {
                        view.showNoAppointments()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    view.hideLoading()
                    view.showError("Failed to fetch appointment: ${e.message}")
                }
            }
        }
    }
}
