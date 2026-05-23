package edu.cit.custodio.mdqueue.features.admin.view

import android.app.AlertDialog
import edu.cit.custodio.mdqueue.features.clinic.model.ClinicRequest
import edu.cit.custodio.mdqueue.features.clinic.model.ClinicResponse
import edu.cit.custodio.mdqueue.features.queue.model.QueueResponse
import edu.cit.custodio.mdqueue.features.queue.model.QueueRequest
import edu.cit.custodio.mdqueue.features.queue.model.QueueEntryResponse
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AdminQueueActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutNoClinic: LinearLayout
    private lateinit var btnCreateFirstClinic: Button
    private lateinit var layoutMainContent: View
    private lateinit var spinnerClinics: Spinner
    private lateinit var btnCreateClinic: Button
    private lateinit var rvAdminQueues: RecyclerView
    private lateinit var btnCreateQueue: Button
    private lateinit var layoutConsultationPanel: LinearLayout
    private lateinit var tvActiveQueueTitle: TextView
    private lateinit var tvNoServingPlaceholder: TextView
    private lateinit var layoutServingDetails: View
    private lateinit var tvServingNumber: TextView
    private lateinit var tvServingName: TextView
    private lateinit var btnServeNext: Button
    private lateinit var btnCompleteConsultation: Button
    private lateinit var tvWaitlistHeader: TextView
    private lateinit var tvWaitlistEmptyPlaceholder: TextView
    private lateinit var rvWaitlist: RecyclerView

    private val clinicsList = mutableListOf<ClinicResponse>()
    private val queuesList = mutableListOf<QueueResponse>()
    private val waitlistEntries = mutableListOf<QueueEntryResponse>()

    private var selectedClinic: ClinicResponse? = null
    private var selectedQueue: QueueResponse? = null
    private var activeServingEntry: QueueEntryResponse? = null

    private lateinit var queuesAdapter: QueuesAdapter
    private lateinit var waitlistAdapter: WaitlistAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_queue)

        initViews()
        setupRecyclerViews()
        setupListeners()
        fetchClinics()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        progressBar = findViewById(R.id.progressBar)
        layoutNoClinic = findViewById(R.id.layoutNoClinic)
        btnCreateFirstClinic = findViewById(R.id.btnCreateFirstClinic)
        layoutMainContent = findViewById(R.id.layoutMainContent)
        spinnerClinics = findViewById(R.id.spinnerClinics)
        btnCreateClinic = findViewById(R.id.btnCreateClinic)
        rvAdminQueues = findViewById(R.id.rvAdminQueues)
        btnCreateQueue = findViewById(R.id.btnCreateQueue)
        layoutConsultationPanel = findViewById(R.id.layoutConsultationPanel)
        tvActiveQueueTitle = findViewById(R.id.tvActiveQueueTitle)
        tvNoServingPlaceholder = findViewById(R.id.tvNoServingPlaceholder)
        layoutServingDetails = findViewById(R.id.layoutServingDetails)
        tvServingNumber = findViewById(R.id.tvServingNumber)
        tvServingName = findViewById(R.id.tvServingName)
        btnServeNext = findViewById(R.id.btnServeNext)
        btnCompleteConsultation = findViewById(R.id.btnCompleteConsultation)
        tvWaitlistHeader = findViewById(R.id.tvWaitlistHeader)
        tvWaitlistEmptyPlaceholder = findViewById(R.id.tvWaitlistEmptyPlaceholder)
        rvWaitlist = findViewById(R.id.rvWaitlist)
    }

    private fun setupRecyclerViews() {
        // Queues list
        rvAdminQueues.layoutManager = LinearLayoutManager(this)
        queuesAdapter = QueuesAdapter(
            queues = queuesList,
            onItemClick = { queue -> selectQueue(queue) },
            onToggleStatus = { queue -> toggleQueueStatus(queue) }
        )
        rvAdminQueues.adapter = queuesAdapter

        // Patients waitlist
        rvWaitlist.layoutManager = LinearLayoutManager(this)
        waitlistAdapter = WaitlistAdapter(waitlistEntries)
        rvWaitlist.adapter = waitlistAdapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener { finish() }

        btnCreateFirstClinic.setOnClickListener { showCreateClinicDialog() }
        btnCreateClinic.setOnClickListener { showCreateClinicDialog() }
        btnCreateQueue.setOnClickListener { showCreateQueueDialog() }

        btnServeNext.setOnClickListener { serveNextPatient() }
        btnCompleteConsultation.setOnClickListener { completeConsultation() }

        spinnerClinics.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position in clinicsList.indices) {
                    val clinic = clinicsList[position]
                    selectedClinic = clinic
                    loadQueuesForClinic(clinic.id)
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun fetchClinics() {
        setLoading(true)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.clinicApi.getMyClinics()
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()!!.data ?: emptyList()
                        clinicsList.clear()
                        clinicsList.addAll(list)

                        if (clinicsList.isEmpty()) {
                            layoutNoClinic.visibility = View.VISIBLE
                            layoutMainContent.visibility = View.GONE
                        } else {
                            layoutNoClinic.visibility = View.GONE
                            layoutMainContent.visibility = View.VISIBLE
                            setupClinicsSpinner()
                        }
                    } else {
                        Toast.makeText(this@AdminQueueActivity, "Failed to load clinics", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    setLoading(false)
                    Toast.makeText(this@AdminQueueActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupClinicsSpinner() {
        val names = clinicsList.map { it.name }
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, names)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerClinics.adapter = adapter

        // Pre-select the first clinic or the active clinic if it still exists
        selectedClinic?.let { current ->
            val index = clinicsList.indexOfFirst { it.id == current.id }
            if (index != -1) {
                spinnerClinics.setSelection(index)
                return
            }
        }

        if (clinicsList.isNotEmpty()) {
            spinnerClinics.setSelection(0)
            selectedClinic = clinicsList[0]
        }
    }

    private fun loadQueuesForClinic(clinicId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.queueApi.getQueuesByClinic(clinicId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val list = response.body()!!.data ?: emptyList()
                        queuesList.clear()
                        queuesList.addAll(list)
                        queuesAdapter.notifyDataSetChanged()

                        // If the currently selected queue is still available, refresh it
                        selectedQueue?.let { current ->
                            val updated = queuesList.find { it.id == current.id }
                            if (updated != null) {
                                selectedQueue = updated
                                loadConsultationDetails(updated.id)
                            } else {
                                resetConsultationPanel()
                            }
                        }
                    } else {
                        Toast.makeText(this@AdminQueueActivity, "Failed to load clinic queues", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminQueueActivity, "Error loading queues: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun selectQueue(queue: QueueResponse) {
        selectedQueue = queue
        layoutConsultationPanel.visibility = View.VISIBLE
        tvActiveQueueTitle.text = "Active Queue: ${queue.name}"
        loadConsultationDetails(queue.id)
    }

    private fun resetConsultationPanel() {
        selectedQueue = null
        layoutConsultationPanel.visibility = View.GONE
        activeServingEntry = null
    }

    private fun loadConsultationDetails(queueId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.queueEntryApi.getEntriesByQueue(queueId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val allEntries = response.body()!!.data ?: emptyList()

                        // 1. Find currently SERVING patient
                        val serving = allEntries.find { it.status == "SERVING" }
                        if (serving != null) {
                            activeServingEntry = serving
                            tvNoServingPlaceholder.visibility = View.GONE
                            layoutServingDetails.visibility = View.VISIBLE
                            tvServingNumber.text = "#${serving.queueNumber}"
                            tvServingName.text = serving.patientName ?: "Anonymous"
                            btnCompleteConsultation.visibility = View.VISIBLE
                        } else {
                            activeServingEntry = null
                            tvNoServingPlaceholder.visibility = View.VISIBLE
                            layoutServingDetails.visibility = View.GONE
                            btnCompleteConsultation.visibility = View.GONE
                        }

                        // 2. Filter waitlist
                        val waiting = allEntries.filter { it.status == "WAITING" }.sortedBy { it.queueNumber }
                        waitlistEntries.clear()
                        waitlistEntries.addAll(waiting)
                        waitlistAdapter.notifyDataSetChanged()

                        tvWaitlistHeader.text = "Patient Waitlist (${waiting.size} waiting)"
                        if (waiting.isEmpty()) {
                            tvWaitlistEmptyPlaceholder.visibility = View.VISIBLE
                            rvWaitlist.visibility = View.GONE
                        } else {
                            tvWaitlistEmptyPlaceholder.visibility = View.GONE
                            rvWaitlist.visibility = View.VISIBLE
                        }
                    } else {
                        Toast.makeText(this@AdminQueueActivity, "Failed to load consultation waitlist", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminQueueActivity, "Error loading waitlist: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun toggleQueueStatus(queue: QueueResponse) {
        val nextStatus = if (queue.status == "OPEN") "CLOSED" else "OPEN"
        val body = mapOf("status" to nextStatus)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.queueApi.updateQueueStatus(queue.id, body)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        Toast.makeText(this@AdminQueueActivity, "Queue is now $nextStatus", Toast.LENGTH_SHORT).show()
                        selectedClinic?.let { loadQueuesForClinic(it.id) }
                    } else {
                        Toast.makeText(this@AdminQueueActivity, "Failed to update queue status", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@AdminQueueActivity, "Network error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun serveNextPatient() {
        val queue = selectedQueue ?: return
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.queueEntryApi.serveNext(queue.id)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful && response.body() != null) {
                        val entry = response.body()!!.data
                        if (entry != null) {
                            Toast.makeText(this@AdminQueueActivity, "Called ticket #${entry.queueNumber}!", Toast.LENGTH_SHORT).show()
                        }
                        loadConsultationDetails(queue.id)
                        selectedClinic?.let { loadQueuesForClinic(it.id) }
                    } else {
                        Toast.makeText(this@AdminQueueActivity, "No patients in queue to serve", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@AdminQueueActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun completeConsultation() {
        val entry = activeServingEntry ?: return
        val queue = selectedQueue ?: return
        progressBar.visibility = View.VISIBLE

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.queueEntryApi.completeEntry(entry.id)
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    if (response.isSuccessful) {
                        Toast.makeText(this@AdminQueueActivity, "Consultation completed!", Toast.LENGTH_SHORT).show()
                        loadConsultationDetails(queue.id)
                        selectedClinic?.let { loadQueuesForClinic(it.id) }
                    } else {
                        Toast.makeText(this@AdminQueueActivity, "Failed to complete consultation", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    progressBar.visibility = View.GONE
                    Toast.makeText(this@AdminQueueActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun showCreateQueueDialog() {
        val clinic = selectedClinic ?: return
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_create_queue, null)
        builder.setView(view)

        val dialog = builder.create()
        val etName = view.findViewById<EditText>(R.id.etDialogQueueName)
        val btnCancel = view.findViewById<Button>(R.id.btnDialogQueueCancel)
        val btnCreate = view.findViewById<Button>(R.id.btnDialogQueueCreate)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnCreate.setOnClickListener {
            val name = etName.text.toString().trim()
            if (name.isEmpty()) {
                etName.error = "Queue name is required"
                return@setOnClickListener
            }

            dialog.dismiss()
            progressBar.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = RetrofitClient.queueApi.createQueue(clinic.id, QueueRequest(name = name))
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        if (response.isSuccessful) {
                            Toast.makeText(this@AdminQueueActivity, "Queue created successfully!", Toast.LENGTH_SHORT).show()
                            loadQueuesForClinic(clinic.id)
                        } else {
                            Toast.makeText(this@AdminQueueActivity, "Failed to create queue", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@AdminQueueActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun showCreateClinicDialog() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_create_clinic, null)
        builder.setView(view)

        val dialog = builder.create()
        val etName = view.findViewById<EditText>(R.id.etClinicName)
        val etAddress = view.findViewById<EditText>(R.id.etClinicAddress)
        val etPhone = view.findViewById<EditText>(R.id.etClinicPhone)
        val etDesc = view.findViewById<EditText>(R.id.etClinicDesc)
        val etOpen = view.findViewById<EditText>(R.id.etClinicOpen)
        val etClose = view.findViewById<EditText>(R.id.etClinicClose)

        val btnCancel = view.findViewById<Button>(R.id.btnCancel)
        val btnCreate = view.findViewById<Button>(R.id.btnCreate)

        btnCancel.setOnClickListener { dialog.dismiss() }

        btnCreate.setOnClickListener {
            val name = etName.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val desc = etDesc.text.toString().trim()
            val opening = etOpen.text.toString().trim()
            val closing = etClose.text.toString().trim()

            if (name.isEmpty()) {
                etName.error = "Name is required"
                return@setOnClickListener
            }
            if (address.isEmpty()) {
                etAddress.error = "Address is required"
                return@setOnClickListener
            }

            dialog.dismiss()
            progressBar.visibility = View.VISIBLE

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val request = ClinicRequest(
                        name = name,
                        address = address,
                        phoneNumber = phone,
                        description = desc,
                        openingTime = opening,
                        closingTime = closing
                    )
                    val response = RetrofitClient.clinicApi.createClinic(request)
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        if (response.isSuccessful) {
                            Toast.makeText(this@AdminQueueActivity, "Clinic registered successfully!", Toast.LENGTH_SHORT).show()
                            fetchClinics()
                        } else {
                            Toast.makeText(this@AdminQueueActivity, "Failed to register clinic", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this@AdminQueueActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        dialog.show()
    }

    private fun setLoading(isLoading: Boolean) {
        progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }
}

// Recycler Adapter for Clinic Queues in Admin Workspace
class QueuesAdapter(
    private val queues: List<QueueResponse>,
    private val onItemClick: (QueueResponse) -> Unit,
    private val onToggleStatus: (QueueResponse) -> Unit
) : RecyclerView.Adapter<QueuesAdapter.QueueViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): QueueViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_queue, parent, false)
        return QueueViewHolder(view)
    }

    override fun onBindViewHolder(holder: QueueViewHolder, position: Int) {
        val queue = queues[position]
        holder.bind(queue, onItemClick, onToggleStatus)
    }

    override fun getItemCount(): Int = queues.size

    class QueueViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvAdminQueueName: TextView = itemView.findViewById(R.id.tvAdminQueueName)
        private val tvAdminNowServing: TextView = itemView.findViewById(R.id.tvAdminNowServing)
        private val btnToggleStatus: Button = itemView.findViewById(R.id.btnToggleStatus)

        fun bind(
            queue: QueueResponse,
            onItemClick: (QueueResponse) -> Unit,
            onToggleStatus: (QueueResponse) -> Unit
        ) {
            tvAdminQueueName.text = queue.name
            tvAdminNowServing.text = "Serving: #${queue.currentNumber} | Waiting: ${queue.waitingCount}"

            // Toggle button text based on current status
            if (queue.status == "OPEN") {
                btnToggleStatus.text = "CLOSE"
                btnToggleStatus.setTextColor(itemView.context.getColor(R.color.error))
            } else {
                btnToggleStatus.text = "OPEN"
                btnToggleStatus.setTextColor(itemView.context.getColor(R.color.success))
            }

            itemView.setOnClickListener { onItemClick(queue) }
            btnToggleStatus.setOnClickListener { onToggleStatus(queue) }
        }
    }
}

// Recycler Adapter for patient waitlist
class WaitlistAdapter(
    private val waitlist: List<QueueEntryResponse>
) : RecyclerView.Adapter<WaitlistAdapter.WaitlistViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WaitlistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_waitlist_patient, parent, false)
        return WaitlistViewHolder(view)
    }

    override fun onBindViewHolder(holder: WaitlistViewHolder, position: Int) {
        val entry = waitlist[position]
        holder.bind(entry, position + 1)
    }

    override fun getItemCount(): Int = waitlist.size

    class WaitlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvWaitlistNumber: TextView = itemView.findViewById(R.id.tvWaitlistNumber)
        private val tvWaitlistName: TextView = itemView.findViewById(R.id.tvWaitlistName)
        private val tvWaitlistTime: TextView = itemView.findViewById(R.id.tvWaitlistTime)
        private val tvWaitlistPosition: TextView = itemView.findViewById(R.id.tvWaitlistPosition)

        fun bind(entry: QueueEntryResponse, queuePosition: Int) {
            tvWaitlistNumber.text = "#${entry.queueNumber}"
            tvWaitlistName.text = entry.patientName ?: "Anonymous"
            tvWaitlistTime.text = "Checked-in: ${entry.checkInTime?.substringBefore(".")?.replace("T", " ") ?: "Waiting"}"

            val ordinal = getOrdinal(queuePosition)
            tvWaitlistPosition.text = ordinal
        }

        private fun getOrdinal(i: Int): String {
            val mod100 = i % 100
            val mod10 = i % 10
            if (mod100 in 11..13) {
                return "${i}th"
            }
            return when (mod10) {
                1 -> "${i}st"
                2 -> "${i}nd"
                3 -> "${i}rd"
                else -> "${i}th"
            }
        }
    }
}
