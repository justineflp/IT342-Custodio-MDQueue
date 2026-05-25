package edu.cit.custodio.mdqueue.features.appointment.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.features.appointment.model.AppointmentResponse

class AppointmentsAdapter(
    private val appointments: List<AppointmentResponse>,
    private val isDoctorOrAdmin: Boolean,
    private val onItemClick: (AppointmentResponse) -> Unit
) : RecyclerView.Adapter<AppointmentsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_dashboard_appointment, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val appt = appointments[position]
        holder.bind(appt)
    }

    override fun getItemCount(): Int = appointments.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvApptItemTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvApptItemSubtitle)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvApptItemStatus)

        fun bind(appt: AppointmentResponse) {
            tvTitle.text = if (isDoctorOrAdmin) "Patient: ${appt.patientName}" else "Dr. ${appt.doctorName}"
            
            // Format datetime: replace 'T' with a space for readability
            val rawTime = appt.appointmentDatetime
            tvSubtitle.text = "${rawTime.replace("T", " ")} | Reason: ${appt.reason}"

            tvStatus.text = appt.status
            
            // Status styling
            val context = itemView.context
            when (appt.status) {
                "PENDING" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_status_pending)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.primary))
                }
                "CONFIRMED" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_status_confirmed)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.success))
                }
                "CANCELLED" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_status_cancelled)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.error))
                }
                "COMPLETED" -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_status_completed)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }
                else -> {
                    tvStatus.setBackgroundResource(R.drawable.bg_status_completed)
                    tvStatus.setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                }
            }

            itemView.setOnClickListener { onItemClick(appt) }
        }
    }
}
