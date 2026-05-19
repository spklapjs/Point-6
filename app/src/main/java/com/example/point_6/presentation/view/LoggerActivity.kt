package com.example.point_6.presentation.view

import android.graphics.Color
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
import com.example.point_6.data.sensor.SensorData
import com.example.point_6.data.sensor.SpenManager
import com.example.point_6.domain.usecase.DataSyncUseCase
import com.example.point_6.presentation.viewmodel.LoggerViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.launch

class LoggerActivity : AppCompatActivity() {

    private lateinit var viewModel: LoggerViewModel
    private lateinit var sensorRepository: SensorRepositoryImpl
    private lateinit var lineChart: LineChart
    private var selectedLabel: String = "Cymbal1"
    private var selectedDisplayLabel: String = "Cymbal1 (leftup)"
    private var timeIndex = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logger)

        val phoneManager = PhoneSensorManager(this)
        val spenManager = SpenManager(this)
        sensorRepository = SensorRepositoryImpl(phoneManager, spenManager)
        val syncUseCase = DataSyncUseCase()

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return LoggerViewModel(application, sensorRepository, syncUseCase) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[LoggerViewModel::class.java]

        setupUI()
        setupChart()
        observeViewModel()
    }

    private fun setupUI() {
        val labelSpinner: Spinner = findViewById(R.id.labelSpinner)
        val btnStart: Button = findViewById(R.id.btnStart)
        val btnStop: Button = findViewById(R.id.btnStop)

        val displayLabels = arrayOf(
            "Cymbal1 (leftup)", "Tom1 (middleup)", "Cymbal2 (rightup)",
            "Hi-hat (leftdown)", "Snare (middledown)", "Tom2 (leftdown)"
        )
        val rawLabels = arrayOf("Cymbal1", "Tom1", "Cymbal2", "Hi-hat", "Snare", "Tom2")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, displayLabels)
        labelSpinner.adapter = adapter

        labelSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLabel = rawLabels[position]
                selectedDisplayLabel = displayLabels[position]
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

    private fun setupChart() {
        lineChart = findViewById(R.id.lineChart)
        val dataSets = ArrayList<ILineDataSet>()

        val colors = listOf(
            Color.RED, Color.GREEN, Color.BLUE,
            Color.MAGENTA, Color.CYAN, Color.parseColor("#FFA500"),
            Color.BLACK, Color.parseColor("#8B4513")
        )

        val labels = listOf(
            "Phone Accel X", "Phone Accel Y", "Phone Accel Z",
            "Phone Gyro X", "Phone Gyro Y", "Phone Gyro Z",
            "SPen X (x10)", "SPen Y (x10)"
        )

        for (i in 0 until 8) {
            val dataSet = LineDataSet(ArrayList<Entry>(), labels[i])
            dataSet.color = colors[i]
            dataSet.setDrawCircles(false)
            dataSet.setDrawValues(false)
            dataSets.add(dataSet)
        }

        lineChart.data = LineData(dataSets)
        lineChart.description.isEnabled = false
        lineChart.axisRight.isEnabled = false

        val legend = lineChart.legend
        legend.isWordWrapEnabled = true
    }

    private fun updateChart(data: SensorData) {
        val chartData = lineChart.data ?: return

        val values = floatArrayOf(
            data.phoneAccel[0], data.phoneAccel[1], data.phoneAccel[2],
            data.phoneGyro[0], data.phoneGyro[1], data.phoneGyro[2],
            data.spenDelta[0] * 10f, data.spenDelta[1] * 10f
        )

        for (i in 0 until 8) {
            val dataSet = chartData.getDataSetByIndex(i)
            dataSet.addEntry(Entry(timeIndex, values[i]))
        }

        timeIndex += 1f

        chartData.notifyDataChanged()
        lineChart.notifyDataSetChanged()
        lineChart.setVisibleXRangeMaximum(100f)
        lineChart.moveViewToX(chartData.entryCount.toFloat())
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
                tvStatus.text = if (isRecording) "Status: Recording [$selectedDisplayLabel]..." else "Status: Ready"
            }
        }

        lifecycleScope.launch {
            viewModel.recordedCount.collect { count ->
                tvRecordCount.text = "Recorded Windows: $count"
            }
        }

        lifecycleScope.launch {
            sensorRepository.sensorDataFlow.collect { data ->
                if (viewModel.isRecording.value) {
                    updateChart(data)
                }
            }
        }
    }
}