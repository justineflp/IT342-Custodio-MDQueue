package edu.cit.custodio.mdqueue.features.profile.view

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.core.network.RetrofitClient
import edu.cit.custodio.mdqueue.core.session.SessionManager
import edu.cit.custodio.mdqueue.features.appointment.model.UserProfileResponse
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ProfileActivity : AppCompatActivity() {

    private lateinit var btnBack: ImageButton
    private lateinit var tvProfileAvatar: TextView
    private lateinit var tvProfileName: TextView
    private lateinit var tvProfileRoleBadge: TextView
    private lateinit var tvProfileEmail: TextView
    private lateinit var tvProfilePhone: TextView
    private lateinit var tvProfileRole: TextView

    // Doctor specialty elements
    private lateinit var cardDoctorSpecialty: LinearLayout
    private lateinit var spinnerSpecialty: Spinner
    private lateinit var btnSaveSpecialty: Button

    // Change password elements
    private lateinit var etCurrentPassword: EditText
    private lateinit var etNewPassword: EditText
    private lateinit var etConfirmNewPassword: EditText
    private lateinit var tvPasswordMessage: TextView
    private lateinit var btnChangePassword: Button

    private lateinit var sessionManager: SessionManager
    private val specialties = arrayOf(
        "General Practice",
        "Cardiology",
        "Dermatology",
        "Pediatrics",
        "Neurology",
        "Psychiatry",
        "Orthopedics",
        "Oncology",
        "Gynecology",
        "Ophthalmology"
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        sessionManager = SessionManager(this)

        initViews()
        setupListeners()
        loadProfileData()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        tvProfileAvatar = findViewById(R.id.tvProfileAvatar)
        tvProfileName = findViewById(R.id.tvProfileName)
        tvProfileRoleBadge = findViewById(R.id.tvProfileRoleBadge)
        tvProfileEmail = findViewById(R.id.tvProfileEmail)
        tvProfilePhone = findViewById(R.id.tvProfilePhone)
        tvProfileRole = findViewById(R.id.tvProfileRole)

        cardDoctorSpecialty = findViewById(R.id.cardDoctorSpecialty)
        spinnerSpecialty = findViewById(R.id.spinnerSpecialty)
        btnSaveSpecialty = findViewById(R.id.btnSaveSpecialty)

        // Change password
        etCurrentPassword = findViewById(R.id.etCurrentPassword)
        etNewPassword = findViewById(R.id.etNewPassword)
        etConfirmNewPassword = findViewById(R.id.etConfirmNewPassword)
        tvPasswordMessage = findViewById(R.id.tvPasswordMessage)
        btnChangePassword = findViewById(R.id.btnChangePassword)

        // Set up specialty spinner
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, specialties)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSpecialty.adapter = adapter

        // Set static local details first
        val fullName = sessionManager.getFullName() ?: "User"
        tvProfileAvatar.text = fullName.take(1).uppercase(Locale.US)
        tvProfileName.text = fullName
        tvProfileEmail.text = sessionManager.getEmail() ?: ""
        tvProfileRole.text = sessionManager.getRole() ?: "PATIENT"
        tvProfileRoleBadge.text = sessionManager.getRole() ?: "PATIENT"

        val role = sessionManager.getRole() ?: "PATIENT"
        if (role == "DOCTOR") {
            cardDoctorSpecialty.visibility = View.VISIBLE
            tvProfileRoleBadge.backgroundTintList = getColorStateList(R.color.primary)
        } else {
            cardDoctorSpecialty.visibility = View.GONE
            tvProfileRoleBadge.backgroundTintList = getColorStateList(R.color.success)
        }
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        btnSaveSpecialty.setOnClickListener {
            val selectedSpecialty = spinnerSpecialty.selectedItem.toString()
            saveDoctorSpecialty(selectedSpecialty)
        }

        btnChangePassword.setOnClickListener {
            handleChangePassword()
        }
    }

    private fun handleChangePassword() {
        val currentPw = etCurrentPassword.text.toString().trim()
        val newPw = etNewPassword.text.toString().trim()
        val confirmPw = etConfirmNewPassword.text.toString().trim()

        // Client-side validation
        if (currentPw.isEmpty() || newPw.isEmpty() || confirmPw.isEmpty()) {
            showPasswordMessage("All password fields are required", isError = true)
            return
        }
        if (newPw != confirmPw) {
            showPasswordMessage("New password and confirm password do not match", isError = true)
            return
        }
        if (currentPw == newPw) {
            showPasswordMessage("New password must be different from current password", isError = true)
            return
        }

        btnChangePassword.isEnabled = false
        btnChangePassword.text = "Changing..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = mapOf(
                    "currentPassword" to currentPw,
                    "newPassword" to newPw,
                    "confirmNewPassword" to confirmPw
                )
                val response = RetrofitClient.appointmentApi.changePassword(body)
                withContext(Dispatchers.Main) {
                    btnChangePassword.isEnabled = true
                    btnChangePassword.text = "Change Password"
                    if (response.isSuccessful && response.body()?.success == true) {
                        showPasswordMessage("Password changed successfully!", isError = false)
                        etCurrentPassword.text.clear()
                        etNewPassword.text.clear()
                        etConfirmNewPassword.text.clear()
                    } else {
                        val errorMsg = response.body()?.message ?: "Failed to change password"
                        showPasswordMessage(errorMsg, isError = true)
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnChangePassword.isEnabled = true
                    btnChangePassword.text = "Change Password"
                    showPasswordMessage("Error: ${e.message}", isError = true)
                }
            }
        }
    }

    private fun showPasswordMessage(message: String, isError: Boolean) {
        tvPasswordMessage.text = message
        tvPasswordMessage.visibility = View.VISIBLE
        if (isError) {
            tvPasswordMessage.setTextColor(ContextCompat.getColor(this, R.color.error))
        } else {
            tvPasswordMessage.setTextColor(ContextCompat.getColor(this, R.color.success))
        }
    }

    private fun loadProfileData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = RetrofitClient.appointmentApi.getUserProfile()
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful && response.body() != null) {
                        val profile = response.body()!!.data
                        if (profile != null) {
                            displayProfile(profile)
                        }
                    }
                }
            } catch (e: Exception) {
                // Silently fall back to cached session manager info if network fails
            }
        }
    }

    private fun displayProfile(profile: UserProfileResponse) {
        tvProfileName.text = profile.fullName
        tvProfileAvatar.text = profile.fullName.take(1).uppercase(Locale.US)
        tvProfileEmail.text = profile.email
        tvProfilePhone.text = if (!profile.phoneNumber.isNullOrEmpty()) profile.phoneNumber else "Not set"
        tvProfileRole.text = profile.role
        tvProfileRoleBadge.text = profile.role

        // Cache changes in local SessionManager
        sessionManager.saveAuthSession(
            token = sessionManager.getToken() ?: "",
            userId = profile.id,
            email = profile.email,
            fullName = profile.fullName,
            role = profile.role
        )

        if (profile.role == "DOCTOR") {
            cardDoctorSpecialty.visibility = View.VISIBLE
            // Select their saved specialty in the dropdown spinner
            val specialtyIndex = specialties.indexOf(profile.specialty ?: "")
            if (specialtyIndex != -1) {
                spinnerSpecialty.setSelection(specialtyIndex)
            }
        } else {
            cardDoctorSpecialty.visibility = View.GONE
        }
    }

    private fun saveDoctorSpecialty(specialty: String) {
        btnSaveSpecialty.isEnabled = false
        btnSaveSpecialty.text = "Saving..."

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val body = mapOf("specialty" to specialty)
                val response = RetrofitClient.appointmentApi.updateSpecialty(body)
                withContext(Dispatchers.Main) {
                    btnSaveSpecialty.isEnabled = true
                    btnSaveSpecialty.text = "Save Specialty"
                    if (response.isSuccessful && response.body()?.success == true) {
                        Toast.makeText(this@ProfileActivity, "Specialty updated successfully!", Toast.LENGTH_SHORT).show()
                        loadProfileData()
                    } else {
                        Toast.makeText(this@ProfileActivity, "Failed to save specialty", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    btnSaveSpecialty.isEnabled = true
                    btnSaveSpecialty.text = "Save Specialty"
                    Toast.makeText(this@ProfileActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}
