package edu.cit.custodio.mdqueue.features.dashboard

import edu.cit.custodio.mdqueue.core.network.NetworkResult
import edu.cit.custodio.mdqueue.features.appointment.model.AppointmentResponse

interface DashboardContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showUpcomingAppointment(appointment: AppointmentResponse)
        fun showNoAppointments()
        fun showError(message: String)
    }

    interface Presenter {
        fun fetchUpcomingAppointment()
    }
}
