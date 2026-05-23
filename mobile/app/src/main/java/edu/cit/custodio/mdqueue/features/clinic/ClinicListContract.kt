package edu.cit.custodio.mdqueue.features.clinic
    
import edu.cit.custodio.mdqueue.features.clinic.model.ClinicResponse

interface ClinicListContract {
    interface View {
        fun showLoading()
        fun hideLoading()
        fun showClinics(clinics: List<ClinicResponse>)
        fun showEmptyState()
        fun showError(message: String)
    }

    interface Presenter {
        fun fetchClinics(query: String? = null)
        fun attachView(view: View)
        fun detachView()
    }
}
