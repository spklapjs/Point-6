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
import com.github.mikephil.charting.utils.Utils
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

    private var phoneXIndex = 0f
    private var spenXIndex = 0f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_logger)

        Utils.init(this)

        val factory = object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                val phoneManager = PhoneSensorManager(applicationContext)
                val spenManager = SpenManager(this@LoggerActivity)
                val repository = SensorRepositoryImpl(phoneManager, spenManager)
                return LoggerViewModel(application, repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory).get(LoggerViewModel::class.java)

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
        val phoneDataSets = ArrayList<ILineDataSet>()
        val phoneColors = listOf(
            Color.RED, Color.GREEN, Color.BLUE,
            Color.MAGENTA, Color.CYAN, Color.parseColor("#FFA500")
        )
        val phoneLabels = listOf(
            "Accel X", "Accel Y", "Accel Z",
            "Gyro X", "Gyro Y", "Gyro Z"
        )

        for (i in 0 until 6) {
            val dataSet = LineDataSet(ArrayList<Entry>(), phoneLabels.get(i)).apply {
                color = phoneColors.get(i)
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 1f
            }
            phoneDataSets.add(dataSet)
        }

        phoneChart.data = LineData(phoneDataSets)
        phoneChart.description.isEnabled = false
        phoneChart.axisRight.isEnabled = false
        phoneChart.legend.isWordWrapEnabled = true

        val spenDataSets = ArrayList<ILineDataSet>()
        val spenColors = listOf(Color.BLACK, Color.parseColor("#8B4513"))
        val spenLabels = listOf("Delta X (x10)", "Delta Y (x10)")

        for (i in 0 until 2) {
            val dataSet = LineDataSet(ArrayList<Entry>(), spenLabels.get(i)).apply {
                color = spenColors.get(i)
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 1f
            }
            spenDataSets.add(dataSet)
        }

        spenChart.data = LineData(spenDataSets)
        spenChart.description.isEnabled = false
        spenChart.axisRight.isEnabled = false
        spenChart.legend.isWordWrapEnabled = true
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
                    val chartData = phoneChart.data ?: return@let

                    // 채팅 시스템의 대괄호 인용구 강제 변환 버그를 피하기 위해 get 함수 사용
                    val values = floatArrayOf(
                        it.accel.get(0), it.accel.get(1), it.accel.get(2),
                        it.gyro.get(0), it.gyro.get(1), it.gyro.get(2)
                    )

                    for (i in 0 until 6) {
                        val dataSet = chartData.getDataSetByIndex(i)
                        dataSet.addEntry(Entry(phoneXIndex, values.get(i)))
                    }

                    phoneXIndex += 1f

                    chartData.notifyDataChanged()
                    phoneChart.notifyDataSetChanged()

                    phoneChart.setVisibleXRangeMaximum(500f)
                    phoneChart.moveViewToX(phoneXIndex - 500f)
                    phoneChart.invalidate()
                }
            }
        }

        lifecycleScope.launch {
            viewModel.spenStream.collect { data ->
                data?.let {
                    val chartData = spenChart.data ?: return@let

                    val values = floatArrayOf(
                        it.deltaX * 10f, it.deltaY * 10f
                    )

                    for (i in 0 until 2) {
                        val dataSet = chartData.getDataSetByIndex(i)
                        dataSet.addEntry(Entry(spenXIndex, values.get(i)))
                    }

                    spenXIndex += 1f

                    chartData.notifyDataChanged()
                    spenChart.notifyDataSetChanged()

                    // 에스펜 차트의 데이터 압축률을 줄여 선이 겹치지 않도록 최대 범위 100으로 변경
                    spenChart.setVisibleXRangeMaximum(100f)
                    spenChart.moveViewToX(spenXIndex - 100f)
                    spenChart.invalidate()
                }
            }
        }
    }
}