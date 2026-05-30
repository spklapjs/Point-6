package com.example.point_6.presentation.view

import android.graphics.Color
import android.os.Bundle
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
import com.example.point_6.presentation.viewmodel.LoggerViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import kotlinx.coroutines.launch

class LoggerActivity : AppCompatActivity() {

    private lateinit var viewModel: LoggerViewModel
    private lateinit var phoneChart: LineChart
    private lateinit var spenChart: LineChart
    private lateinit var btnStart: Button
    private lateinit var btnStop: Button
    private lateinit var tvStatus: TextView
    private lateinit var tvRecordCount: TextView
    private lateinit var labelSpinner: Spinner

    private var phoneDataSetAccelX = LineDataSet(mutableListOf(), "Accel X").apply { color = Color.RED; setDrawCircles(false) }
    private var phoneDataSetAccelY = LineDataSet(mutableListOf(), "Accel Y").apply { color = Color.GREEN; setDrawCircles(false) }
    private var phoneDataSetAccelZ = LineDataSet(mutableListOf(), "Accel Z").apply { color = Color.BLUE; setDrawCircles(false) }

    private var spenDataSetX = LineDataSet(mutableListOf(), "Delta X").apply { color = Color.MAGENTA; setDrawCircles(false) }
    private var spenDataSetY = LineDataSet(mutableListOf(), "Delta Y").apply { color = Color.CYAN; setDrawCircles(false) }

    private var phoneXIndex = 0f
    private var spenXIndex = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logger)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val phoneManager = PhoneSensorManager(applicationContext)
                val spenManager = SpenManager(applicationContext)
                val repository = SensorRepositoryImpl(phoneManager, spenManager)
                return LoggerViewModel(application, repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[LoggerViewModel::class.java]

        initUI()
        setupCharts()
        observeViewModel()
    }

    private fun initUI() {
        phoneChart = findViewById(R.id.phoneChart)
        spenChart = findViewById(R.id.spenChart)
        btnStart = findViewById(R.id.btnStart)
        btnStop = findViewById(R.id.btnStop)
        tvStatus = findViewById(R.id.tvStatus)
        tvRecordCount = findViewById(R.id.tvRecordCount)
        labelSpinner = findViewById(R.id.labelSpinner)

        val labels = arrayOf(
            "Phone_Cymbal1_LeftUp", "Phone_Tom1_MidUp", "Phone_Cymbal2_RightUp",
            "Phone_Hihat_LeftDown", "Phone_Snare_MidDown", "Phone_Tom2_RightDown",
            "SPen_Cymbal1_LeftUp", "SPen_Tom1_MidUp", "SPen_Cymbal2_RightUp",
            "SPen_Hihat_LeftDown", "SPen_Snare_MidDown", "SPen_Tom2_RightDown"
        )
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, labels)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        labelSpinner.adapter = adapter

        btnStart.setOnClickListener {
            val selectedLabel = labelSpinner.selectedItem.toString()
            viewModel.startRecording(selectedLabel)
        }

        btnStop.setOnClickListener {
            viewModel.stopRecording()
        }
    }

    private fun setupCharts() {
        val phoneDataSets = ArrayList<ILineDataSet>().apply {
            add(phoneDataSetAccelX)
            add(phoneDataSetAccelY)
            add(phoneDataSetAccelZ)
        }
        phoneChart.data = LineData(phoneDataSets)
        phoneChart.description.isEnabled = false
        phoneChart.setVisibleXRangeMaximum(500f)
        phoneChart.invalidate()

        val spenDataSets = ArrayList<ILineDataSet>().apply {
            add(spenDataSetX)
            add(spenDataSetY)
        }
        spenChart.data = LineData(spenDataSets)
        spenChart.description.isEnabled = false
        spenChart.setVisibleXRangeMaximum(500f)
        spenChart.invalidate()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.isRecording.collect { isRecording ->
                btnStart.isEnabled = !isRecording
                btnStop.isEnabled = isRecording
                labelSpinner.isEnabled = !isRecording
                tvStatus.text = if (isRecording) "Status: Recording..." else "Status: Ready"
            }
        }

        lifecycleScope.launch {
            viewModel.recordedCount.collect { count ->
                tvRecordCount.text = "Recorded Points: $count"
            }
        }

        lifecycleScope.launch {
            viewModel.phoneStream.collect { data ->
                data?.let {
                    phoneDataSetAccelX.addEntry(Entry(phoneXIndex, it.accel[0]))
                    phoneDataSetAccelY.addEntry(Entry(phoneXIndex, it.accel[1]))
                    phoneDataSetAccelZ.addEntry(Entry(phoneXIndex, it.accel[2]))
                    phoneXIndex += 1f

                    phoneChart.data.notifyDataChanged()
                    phoneChart.notifyDataSetChanged()
                    phoneChart.moveViewToX(phoneXIndex)
                }
            }
        }

        lifecycleScope.launch {
            viewModel.spenStream.collect { data ->
                data?.let {
                    spenDataSetX.addEntry(Entry(spenXIndex, it.deltaX))
                    spenDataSetY.addEntry(Entry(spenXIndex, it.deltaY))
                    spenXIndex += 1f

                    spenChart.data.notifyDataChanged()
                    spenChart.notifyDataSetChanged()
                    spenChart.moveViewToX(spenXIndex)
                }
            }
        }
    }
}