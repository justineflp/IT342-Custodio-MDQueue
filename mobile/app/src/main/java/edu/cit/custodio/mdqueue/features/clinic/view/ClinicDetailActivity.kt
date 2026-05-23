package edu.cit.custodio.mdqueue.features.clinic.view

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.features.queue.model.QueueEntryResponse
import edu.cit.custodio.mdqueue.features.queue.model.QueueResponse
import edu.cit.custodio.mdqueue.features.clinic.ClinicDetailContract
import edu.cit.custodio.mdqueue.features.clinic.model.ClinicResponse
import edu.cit.custodio.mdqueue.features.clinic.presenter.ClinicDetailPresenter
// Note: we assume QueueStatusActivity is still in ui package for now since it's out of scope
import edu.cit.custodio.mdqueue.ui.QueueStatusActivity

class ClinicDetailActivity : AppCompatActivity(), ClinicDetailContract.View {

    private lateinit var btnBack: ImageButton
    private lateinit var tvTitleName: TextView
    private lateinit var tvDetailClinicName: TextView
    private lateinit var tvDetailHours: TextView
    private lateinit var tvDetailAddress: TextView
    private lateinit var tvDetailPhone: TextView
    private lateinit var tvDetailDescription: TextView
    
    private lateinit var rvQueues: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var tvNoQueues: TextView

    private lateinit var adapter: QueuesAdapter
    private lateinit var presenter: ClinicDetailContract.Presenter
    private var clinicId: Long = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinic_detail)

        clinicId = intent.getLongExtra("clinic_id", -1)
        val initialName = intent.getStringExtra("clinic_name") ?: "Clinic Details"

        initViews()
        tvTitleName.text = initialName
        
        setupRecyclerView()
        setupListeners()

        presenter = ClinicDetailPresenter()
        presenter.attachView(this)
        
        if (clinicId != -1L) {
            presenter.fetchClinicDetails(clinicId)
            presenter.fetchClinicQueues(clinicId)
        } else {
            Toast.makeText(this, "Invalid Clinic ID", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvTitleName = findViewById(R.id.tvTitleName)
        tvDetailClinicName = findViewById(R.id.tvDetailClinicName)
        tvDetailHours = findViewById(R.id.tvDetailHours)
        tvDetailAddress = findViewById(R.id.tvDetailAddress)
        tvDetailPhone = findViewById(R.id.tvDetailPhone)
        tvDetailDescription = findViewById(R.id.tvDetailDescription)
        
        rvQueues = findViewById(R.id.rvQueues)
        progressBar = findViewById(R.id.progressBar)
        tvNoQueues = findViewById(R.id.tvNoQueues)
    }

    private fun setupRecyclerView() {
        rvQueues.layoutManager = LinearLayoutManager(this)
        adapter = QueuesAdapter(mutableListOf()) { queue ->
            presenter.joinQueue(queue)
        }
        rvQueues.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }
    }

    override fun showClinicLoading() {
        // Optional: show a loading indicator for clinic details if needed
    }

    override fun hideClinicLoading() {
        // Optional
    }

    override fun showClinicDetails(clinic: ClinicResponse) {
        tvDetailClinicName.text = clinic.name
        tvTitleName.text = clinic.name
        tvDetailHours.text = "Open: ${clinic.openingTime ?: "08:00 AM"} - ${clinic.closingTime ?: "05:00 PM"}"
        tvDetailAddress.text = clinic.address
        tvDetailPhone.text = clinic.phoneNumber ?: "No phone number listed"
        tvDetailDescription.text = clinic.description ?: "Welcome to ${clinic.name}! We provide outstanding medical services tailored to your needs."
    }

    override fun showClinicError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun showQueuesLoading() {
        progressBar.visibility = View.VISIBLE
        tvNoQueues.visibility = View.GONE
    }

    override fun hideQueuesLoading() {
        progressBar.visibility = View.GONE
    }

    override fun showQueues(queues: List<QueueResponse>) {
        tvNoQueues.visibility = View.GONE
        adapter.updateData(queues)
    }

    override fun showNoQueuesState() {
        adapter.updateData(emptyList())
        tvNoQueues.visibility = View.VISIBLE
    }

    override fun showQueuesError(message: String) {
        showNoQueuesState()
        // Optionally show toast for error
    }

    override fun showJoinQueueLoading() {
        progressBar.visibility = View.VISIBLE
    }

    override fun hideJoinQueueLoading() {
        progressBar.visibility = View.GONE
    }

    override fun onJoinQueueSuccess(entry: QueueEntryResponse) {
        Toast.makeText(this, "Joined queue successfully!", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, QueueStatusActivity::class.java)
        intent.putExtra("entry_id", entry.id)
        intent.putExtra("queue_id", entry.queueId)
        intent.putExtra("queue_name", entry.queueName) // queue name might need safe call but handled in previous code
        intent.putExtra("clinic_name", entry.clinicName ?: tvDetailClinicName.text.toString())
        intent.putExtra("ticket_number", entry.queueNumber)
        startActivity(intent)
        finish()
    }

    override fun onJoinQueueError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}
