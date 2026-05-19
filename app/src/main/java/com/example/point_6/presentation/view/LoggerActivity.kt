package com.example.point_6.presentation.view

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.point_6.R
import com.example.point_6.data.repository.SensorRepositoryImpl
import com.example.point_6.data.sensor.PhoneSensorManager
import com.example.point_6.data.sensor.SpenManager
import com.example.point_6.domain.usecase.DataSyncUseCase
import com.example.point_6.presentation.viewmodel.LoggerViewModel
import kotlinx.coroutines.launch

class LoggerActivity : AppCompatActivity() {

    private lateinit var viewModel: LoggerViewModel
    private var selectedLabel: String = "Snare"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logger)

        val phoneManager = PhoneSensorManager(this)
        val spenManager = SpenManager(this)
        val repository = SensorRepositoryImpl(phoneManager, spenManager)
        val syncUseCase = DataSyncUseCase()

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoggerViewModel(application, repository, syncUseCase) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[LoggerViewModel::class.java]

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        val labelSpinner: Spinner = findViewById(R.id.labelSpinner)
        val btnStart: Button = findViewById(R.id.btnStart)
        val btnStop: Button = findViewById(R.id.btnStop)

        val labels = arrayOf("Snare", "Tom1", "Tom2", "Cymbal1", "Cymbal2", "Hi-hat")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, labels)
        labelSpinner.adapter = adapter

        labelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLabel = labels[position]
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        btnStart.setOnClickListener {
            viewModel.startRecording(selectedLabel)
        }

        btnStop.setOnClickListener {
            viewModel.stopRecording()
        }
    }

    private fun observeViewModel() {
        val btnStart: Button = findViewById(R.id.btnStart)
        val btnStop: Button = findViewById(R.id.btnStop)
        val tvStatus: TextView = findViewById(R.id.tvStatus)
        val tvRecordCount: TextView = findViewById(R.id.tvRecordCount)

        lifecycleScope.launch {
            viewModel.isRecording.collect { isRecording ->
                btnStart.isEnabled = !isRecording
                btnStop.isEnabled = isRecording
                tvStatus.text = if (isRecording) "Status: Recording [${selectedLabel}]..." else "Status: Ready"
            }
        }

        lifecycleScope.launch {
            viewModel.recordedCount.collect { count ->
                tvRecordCount.text = "Recorded Windows: $count"
            }
        }
    }
}