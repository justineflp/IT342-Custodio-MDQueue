package edu.cit.custodio.mdqueue.features.clinic.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.features.queue.model.QueueResponse

class QueuesAdapter(
    private val queues: MutableList<QueueResponse>,
    private val onJoinClick: (QueueResponse) -> Unit
) : RecyclerView.Adapter<QueuesAdapter.QueueViewHolder>() {

    fun updateData(newQueues: List<QueueResponse>) {
        queues.clear()
        queues.addAll(newQueues)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_queue, parent, false)
        return QueueViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        val queue = queues[position]
        holder.bind(queue, onJoinClick)
    }

    override fun getItemCount(): Int = queues.size

    class QueueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvQueueName: TextView = itemView.findViewById(R.id.tvQueueName)
        private val tvQueueStatusBadge: TextView = itemView.findViewById(R.id.tvQueueStatusBadge)
        private val tvNowServingNumber: TextView = itemView.findViewById(R.id.tvNowServingNumber)
        private val tvWaitingCount: TextView = itemView.findViewById(R.id.tvWaitingCount)
        private val btnJoinQueue: Button = itemView.findViewById(R.id.btnJoinQueue)

        fun bind(queue: QueueResponse, onJoinClick: (QueueResponse) -> Unit) {
            tvQueueName.text = queue.name
            tvNowServingNumber.text = "#${queue.currentNumber}"
            
            val waitingCountVal = queue.waitingCount
            tvWaitingCount.text = if (waitingCountVal == 1L) "1 patient" else "$waitingCountVal patients"

            val isOpen = queue.status.uppercase() == "OPEN"
            tvQueueStatusBadge.text = if (isOpen) "OPEN" else "CLOSED"
            tvQueueStatusBadge.setBackgroundResource(R.drawable.bg_banner_success)
            tvQueueStatusBadge.backgroundTintList = itemView.context.getColorStateList(
                if (isOpen) R.color.success else R.color.error
            )

            if (isOpen) {
                btnJoinQueue.isEnabled = true
                btnJoinQueue.text = "Join Queue"
                btnJoinQueue.backgroundTintList = itemView.context.getColorStateList(R.color.primary)
            } else {
                btnJoinQueue.isEnabled = false
                btnJoinQueue.text = "CLOSED"
                btnJoinQueue.backgroundTintList = itemView.context.getColorStateList(R.color.text_hint)
            }

            btnJoinQueue.setOnClickListener {
                onJoinClick(queue)
            }
        }
    }
}
