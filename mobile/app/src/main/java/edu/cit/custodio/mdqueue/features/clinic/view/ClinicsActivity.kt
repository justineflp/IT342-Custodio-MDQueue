package edu.cit.custodio.mdqueue.features.clinic.view

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import edu.cit.custodio.mdqueue.R
import edu.cit.custodio.mdqueue.features.clinic.ClinicListContract
import edu.cit.custodio.mdqueue.features.clinic.model.ClinicResponse
import edu.cit.custodio.mdqueue.features.clinic.presenter.ClinicListPresenter
import edu.cit.custodio.mdqueue.features.clinic.view.ClinicDetailActivity // We need to handle Intent to ClinicDetailActivity which currently is in edu.cit.custodio.mdqueue.ui. Wait, I will refactor ClinicDetailActivity's package as well.

class ClinicsActivity : AppCompatActivity(), ClinicListContract.View {

    private lateinit var btnBack: ImageButton
    private lateinit var etSearch: EditText
    private lateinit var rvClinics: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var layoutEmpty: LinearLayout

    private lateinit var adapter: ClinicsAdapter
    private lateinit var presenter: ClinicListContract.Presenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clinics)

        initViews()
        setupRecyclerView()
        
        presenter = ClinicListPresenter()
        presenter.attachView(this)

        setupListeners()
        presenter.fetchClinics()
    }

    private fun initViews() {
        btnBack = findViewById(R.id.btnBack)
        etSearch = findViewById(R.id.etSearch)
        rvClinics = findViewById(R.id.rvClinics)
        progressBar = findViewById(R.id.progressBar)
        layoutEmpty = findViewById(R.id.layoutEmpty)
    }

    private fun setupRecyclerView() {
        rvClinics.layoutManager = LinearLayoutManager(this)
        adapter = ClinicsAdapter(mutableListOf()) { clinic ->
            val intent = Intent(this, edu.cit.custodio.mdqueue.features.clinic.view.ClinicDetailActivity::class.java)
            intent.putExtra("clinic_id", clinic.id)
            intent.putExtra("clinic_name", clinic.name)
            startActivity(intent)
        }
        rvClinics.adapter = adapter
    }

    private fun setupListeners() {
        btnBack.setOnClickListener {
            finish()
        }

        etSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                // Debounce is handled in Presenter or should be?
                // The requirements say "Move its API fetching into the presenter."
                // I'll add debounce logic in presenter or keep it simple here.
                // It was using coroutine delay. We can just call presenter.fetchClinics(s.toString().trim())
                // Actually, I can use a simple handler or view post for debounce, or the presenter can handle it.
                // Let's just do it directly for now or let presenter fetch it.
                presenter.fetchClinics(s?.toString()?.trim())
            }
        })
    }

    override fun showLoading() {
        progressBar.visibility = View.VISIBLE
        layoutEmpty.visibility = View.GONE
    }

    override fun hideLoading() {
        progressBar.visibility = View.GONE
    }

    override fun showClinics(clinics: List<ClinicResponse>) {
        layoutEmpty.visibility = View.GONE
        adapter.updateData(clinics)
    }

    override fun showEmptyState() {
        adapter.updateData(emptyList())
        layoutEmpty.visibility = View.VISIBLE
    }

    override fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        showEmptyState()
    }

    override fun onDestroy() {
        super.onDestroy()
        presenter.detachView()
    }
}
