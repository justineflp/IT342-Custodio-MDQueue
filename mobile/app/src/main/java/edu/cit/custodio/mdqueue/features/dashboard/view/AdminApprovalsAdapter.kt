package edu.cit.custodio.mdqueue.features.dashboard.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.features.appointment.model.AdminDoctorResponse

class AdminApprovalsAdapter(
    private val doctors: List<AdminDoctorResponse>,
    private val onApproveClick: (AdminDoctorResponse) -> Unit
) : RecyclerView.Adapter<AdminApprovalsAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_doctor_approval, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val doc = doctors[position]
        holder.bind(doc)
    }

    override fun getItemCount(): Int = doctors.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tvApprovalDocName)
        private val tvEmail: TextView = itemView.findViewById(R.id.tvApprovalDocEmail)
        private val btnApprove: Button = itemView.findViewById(R.id.btnApproveDoc)

        fun bind(doc: AdminDoctorResponse) {
            tvName.text = "Dr. ${doc.fullName}"
            tvEmail.text = doc.email
            btnApprove.setOnClickListener { onApproveClick(doc) }
        }
    }
}
