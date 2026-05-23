package edu.cit.custodio.mdqueue.features.queue.view

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.Animation
import android.view.animation.RotateAnimation
import android.widget.Button
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.features.queue.model.QueueEntryResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class QueueStatusActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var btnManualRefresh: ImageButton
    private lateinit var tvStatusClinicName: TextView
    private lateinit var tvStatusQueueName: TextView
    private lateinit var tvTicketNumber: TextView
    private lateinit var tvPeopleAhead: TextView
    private lateinit var tvEstWait: TextView
    private lateinit var tvNowServing: TextView
    private lateinit var tvQueueStatus: TextView
    private lateinit var pbQueueStatus: ProgressBar
    private lateinit var tvLastUpdated: TextView
    private lateinit var btnLeaveQueue: Button

    private var entryId: Long = -1
    private var queueId: Long = -1
    private var ticketNumber: Int = -1
    
    private var refreshJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_queue_status)

        // Read intent data
        entryId = intent.getLongExtra("entry_id", -1)
        queueId = intent.getLongExtra("queue_id", -1)
        ticketNumber = intent.getIntExtra("ticket_number", -1)
        val queueName = intent.getStringExtra("queue_name") ?: "General Consultation"
        val clinicName = intent.getStringExtra("clinic_name") ?: "Clinic"

        initViews()
        
        // Setup initial static info
        tvStatusClinicName.text = clinicName
        tvStatusQueueName.text = queueName
        tvTicketNumber.text = String.format("#%02d", ticketNumber)
        tvQueueStatus.text = "WAITING"

        setupListeners()
    }

    override fun onStart() {
        super.onStart()
        startAutoRefresh()
    }

    override fun onStop() {
        super.onStop()
        stopAutoRefresh()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        btnManualRefresh = findViewById(R.id.btnManualRefresh)
        tvStatusClinicName = findViewById(R.id.tvStatusClinicName)
        tvStatusQueueName = findViewById(R.id.tvStatusQueueName)
        tvTicketNumber = findViewById(R.id.tvTicketNumber)
        tvPeopleAhead = findViewById(R.id.tvPeopleAhead)
        tvEstWait = findViewById(R.id.tvEstWait)
        tvNowServing = findViewById(R.id.tvNowServing)
        tvQueueStatus = findViewById(R.id.tvQueueStatus)
        pbQueueStatus = findViewById(R.id.pbQueueStatus)
        tvLastUpdated = findViewById(R.id.tvLastUpdated)
        btnLeaveQueue = findViewById(R.id.btnLeaveQueue)
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnManualRefresh.setOnClickListener {
            animateRefreshButton()
            refreshStatus()
        }

        btnLeaveQueue.setOnClickListener {
            showLeaveConfirmationDialog()
        }
    }

    private fun startAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = scope.launch {
            while (true) {
                refreshStatus()
                delay(5000) // Poll every 5 seconds
            }
        }
    }

    private fun stopAutoRefresh() {
        refreshJob?.cancel()
        refreshJob = null
    }

    private fun animateRefreshButton() {
        val rotate = RotateAnimation(
            0f, 360f,
            Animation.RELATIVE_TO_SELF, 0.5f,
            Animation.RELATIVE_TO_SELF, 0.5f
        ).apply {
            duration = 500
            repeatCount = 0
        }
        btnManualRefresh.startAnimation(rotate)
    }

    private fun refreshStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch the list of active entries for the user
                val response = RetrofitClient.queueEntryApi.getMyActiveEntries()
                
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val apiResponse = response.body()!!
                        val activeEntries = apiResponse.data ?: emptyList<edu.cit.custodio.mdqueue.features.queue.model.QueueEntryResponse>()
                        
                        // Find our specific entry in the list
                        val matchingEntry = activeEntries.find { it.id == entryId }
                        
                        if (matchingEntry != null) {
                            updateUI(matchingEntry)
                        } else {
                            // If the entry is not found in the ACTIVE list, it was either completed or cancelled
                            checkPastEntryStatus()
                        }
                    }
                }
            } catch (e: Exception) {
                // Fail silently during auto-refresh, keep showing last data
            }
        }
    }

    private fun checkPastEntryStatus() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Fetch all entries (which includes history)
                val response = RetrofitClient.queueEntryApi.getMyEntries()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val allEntries = response.body()!!.data ?: emptyList<edu.cit.custodio.mdqueue.features.queue.model.QueueEntryResponse>()
                        val pastEntry = allEntries.find { it.id == entryId }
                        
                        if (pastEntry != null) {
                            val status = pastEntry.status.uppercase()
                            if (status == "COMPLETED") {
                                stopAutoRefresh()
                                showStatusInfoDialog("Consultation Complete", "Your doctor has completed your session. Thank you!")
                            } else if (status == "CANCELLED") {
                                stopAutoRefresh()
                                showStatusInfoDialog("Queue Cancelled", "This entry was successfully cancelled.")
                            }
                        } else {
                            // Entry completely missing
                            stopAutoRefresh()
                            Toast.makeText(this@QueueStatusActivity, "Queue session not found.", Toast.LENGTH_SHORT).show()
                            finish()
                        }
                    }
                }
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    private fun updateUI(entry: QueueEntryResponse) {
        val status = entry.status.uppercase()
        tvQueueStatus.text = status
        
        // Update color of status text
        when (status) {
            "WAITING" -> tvQueueStatus.setTextColor(getColor(R.color.primary))
            "SERVING" -> tvQueueStatus.setTextColor(getColor(R.color.success))
            else -> tvQueueStatus.setTextColor(getColor(R.color.text_secondary))
        }

        // Fetch the queue current serving number
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val qResponse = RetrofitClient.queueApi.getQueueById(queueId)
                withContext(Dispatchers.Main) {
                    if (qResponse.isSuccessful && qResponse.body() != null) {
                        val queue = qResponse.body()!!.data
                        if (queue != null) {
                            tvNowServing.text = String.format("#%02d", queue.currentNumber)
                            
                            // Adjust progress bar
                            val progressMax = queue.currentNumber + entry.peopleAhead.toInt()
                            pbQueueStatus.max = if (progressMax > 0) progressMax else 100
                            pbQueueStatus.progress = queue.currentNumber
                        }
                    }
                }
            } catch (e: Exception) {
                // Fail silently
            }
        }

        if (status == "SERVING") {
            tvPeopleAhead.text = "It's Your Turn!"
            tvPeopleAhead.setTextColor(getColor(R.color.success))
            tvEstWait.text = "Please enter the doctor's room immediately."
            tvEstWait.setTextColor(getColor(R.color.success))
            btnLeaveQueue.visibility = View.GONE // Can't cancel once being served
        } else {
            val peopleAhead = entry.peopleAhead
            tvPeopleAhead.text = when (peopleAhead) {
                0L -> "You are next in line!"
                1L -> "1 person ahead of you"
                else -> "$peopleAhead people ahead of you"
            }
            tvPeopleAhead.setTextColor(getColor(R.color.text_primary))
            tvEstWait.text = "Estimated wait time: ~${peopleAhead * 5 + 5} mins"
            tvEstWait.setTextColor(getColor(R.color.text_secondary))
            btnLeaveQueue.visibility = View.VISIBLE
        }

        val currentTime = SimpleDateFormat("hh:mm:ss a", Locale.getDefault()).format(Date())
        tvLastUpdated.text = "Auto-refreshing in real time... Last updated: $currentTime"
    }

    private fun showStatusInfoDialog(title: String, message: String) {
        if (isFinishing || isDestroyed) return
        
        AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setCancelable(false)
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                finish()
            }
            .show()
    }

    private fun showLeaveConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("Leave Queue?")
            .setMessage("Are you sure you want to leave this queue? Your ticket number will be forfeited.")
            .setPositiveButton("Leave") { _, _ ->
                leaveQueue()
            }
            .setNegativeButton("Keep Waiting", null)
            .show()
    }

    private fun leaveQueue() {
        stopAutoRefresh()
        btnLeaveQueue.isEnabled = false
        btnLeaveQueue.text = "Leaving..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.queueEntryApi.cancelEntry(entryId)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@QueueStatusActivity, "You left the queue successfully.", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        btnLeaveQueue.isEnabled = true
                        btnLeaveQueue.text = "Leave & Cancel Queue"
                        Toast.makeText(this@QueueStatusActivity, "Failed to leave queue. Please try again.", Toast.LENGTH_SHORT).show()
                        startAutoRefresh()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnLeaveQueue.isEnabled = true
                    btnLeaveQueue.text = "Leave & Cancel Queue"
                    Toast.makeText(this@QueueStatusActivity, "Network error. Please try again.", Toast.LENGTH_SHORT).show()
                    startAutoRefresh()
                }
            }
        }
    }
}
