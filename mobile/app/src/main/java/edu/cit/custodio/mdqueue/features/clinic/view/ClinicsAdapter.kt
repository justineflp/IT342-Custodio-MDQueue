package edu.cit.custodio.mdqueue.features.clinic.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.features.clinic.model.ClinicResponse

class ClinicsAdapter(
    private val clinics: MutableList<ClinicResponse>,
    private val onItemClick: (ClinicResponse) -> Unit
) : RecyclerView.Adapter<ClinicsAdapter.ClinicViewHolder>() {

    fun updateData(newClinics: List<ClinicResponse>) {
        clinics.clear()
        clinics.addAll(newClinics)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ClinicViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_clinic, parent, false)
        return ClinicViewHolder(view)
    }

    override fun onBindViewHolder(holder: ClinicViewHolder, position: Int) {
        val clinic = clinics[position]
        holder.bind(clinic, onItemClick)
    }

    override fun getItemCount(): Int = clinics.size

    class ClinicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvClinicName: TextView = itemView.findViewById(R.id.tvClinicName)
        private val tvActiveQueuesBadge: TextView = itemView.findViewById(R.id.tvActiveQueuesBadge)
        private val tvClinicAddress: TextView = itemView.findViewById(R.id.tvClinicAddress)
        private val tvClinicDescription: TextView = itemView.findViewById(R.id.tvClinicDescription)

        fun bind(clinic: ClinicResponse, onItemClick: (ClinicResponse) -> Unit) {
            tvClinicName.text = clinic.name
            tvClinicAddress.text = clinic.address
            tvClinicDescription.text = clinic.description ?: "No description available."
            
            val queuesCount = clinic.activeQueues
            tvActiveQueuesBadge.text = if (queuesCount == 1) "1 Active Queue" else "$queuesCount Active Queues"
            tvActiveQueuesBadge.visibility = if (queuesCount > 0) View.VISIBLE else View.GONE

            itemView.setOnClickListener {
                onItemClick(clinic)
            }
        }
    }
}
