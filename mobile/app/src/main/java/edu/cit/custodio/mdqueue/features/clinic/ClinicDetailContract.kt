package edu.cit.custodio.mdqueue.features.clinic

import edu.cit.custodio.mdqueue.features.clinic.model.ClinicResponse
// I need the queue response model. It seems to be in edu.cit.custodio.mdqueue.api.models.QueueResponse currently, but we will fix its import later or use a correct path if refactored.
import edu.cit.custodio.mdqueue.features.queue.model.QueueResponse
import edu.cit.custodio.mdqueue.features.queue.model.QueueEntryResponse

interface ClinicDetailContract {
    interface View {
        fun showClinicLoading()
        fun hideClinicLoading()
        fun showClinicDetails(clinic: ClinicResponse)
        fun showClinicError(message: String)

        fun showQueuesLoading()
        fun hideQueuesLoading()
        fun showQueues(queues: List<QueueResponse>)
        fun showNoQueuesState()
        fun showQueuesError(message: String)
        
        fun showJoinQueueLoading()
        fun hideJoinQueueLoading()
        fun onJoinQueueSuccess(entry: QueueEntryResponse)
        fun onJoinQueueError(message: String)
    }

    interface Presenter {
        fun fetchClinicDetails(clinicId: Long)
        fun fetchClinicQueues(clinicId: Long)
        fun joinQueue(queue: QueueResponse)
        fun attachView(view: View)
        fun detachView()
    }
}
