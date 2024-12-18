package com.example.climbc1

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ClipData
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.icu.util.Calendar
import android.media.RouteListingPreference
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.climbc1.databinding.ActivityMainBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.sql.Types.NULL
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.logging.Handler
import kotlin.math.max

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var db: WorkoutDataDatabase

    // Bluetooth Variables
    private lateinit var bluetoothManager: BluetoothManager
    private var bluetoothAdapter: BluetoothAdapter? = null
    private var bluetoothSocket: BluetoothSocket? = null
    private var outputStream: OutputStream? = null
    private var inputStream: InputStream? = null

    private var isDatabaseSync: Boolean = false
    private var lastUnixTimeSinceStart: Long = 0
    private var dummyWorkoutID = 0

    lateinit var timerDisplay: TextView
    lateinit var startStopButton: Button
    lateinit var timerBg: View
    lateinit var timerDisplayText: TextView
    lateinit var sessionNumberText: TextView
    lateinit var dateDisplayText: TextView
    lateinit var sharedPref: SharedPreferences

    var clockRunning = false
    var timeElapsed = 0
    val handler = android.os.Handler()

    val updateTimeRunnable = object : Runnable {
        override fun run() {
            if (clockRunning) {
                timeElapsed++
                updateStopwatchDisplay()
                handler.postDelayed(this, 1000)  // Update every 1 second
            }
        }
    }

    val updateCalibrationTimerRunnable = object : Runnable {
        override fun run() {
            if (clockRunning) {
                timeElapsed++
                updateCalibrationDisplay()
                handler.postDelayed(this, 1000)  // Update every 1 second
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //init bluetooth relevant modules.
        bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.BLUETOOTH_CONNECT
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return // ALEX, change this to prompt for permission instead of crashing
        }

        val teal1 = ContextCompat.getColor(this, R.color.teal1)
        val black2 = ContextCompat.getColor(this, R.color.black2)
        val black1 = ContextCompat.getColor(this, R.color.black)
        val white = ContextCompat.getColor(this, R.color.white)
        val orange1 = ContextCompat.getColor(this, R.color.orange1)

        //get calendar date to update on timer header
        val calendar = Calendar.getInstance()

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        lateinit var monthWord: String

        when (month) {
            0 -> monthWord = "January"
            1 -> monthWord = "February"
            2 -> monthWord = "March"
            3 -> monthWord = "April"
            4 -> monthWord = "May"
            5 -> monthWord = "June"
            6 -> monthWord = "July"
            7 -> monthWord = "August"
            8 -> monthWord = "September"
            9 -> monthWord = "October"
            10 -> monthWord = "November"
            11 -> monthWord = "December"
            else -> { // Note the block
                monthWord = "wuh woh"
            }
        }

        val dateString = "$monthWord $day, $year"

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timerDisplay = findViewById(R.id.timerDisplayText)
        startStopButton = findViewById(R.id.sessionStartButton)
        timerBg = findViewById(R.id.rectangle)
        timerDisplayText = findViewById(R.id.timerDisplayText)
        sessionNumberText = findViewById(R.id.sessionNumberText)
        dateDisplayText = findViewById(R.id.dateDisplayText)

        //init changes
        startStopButton.setBackgroundColor(teal1)
        startStopButton.setTextColor(black1)
        dateDisplayText.text = dateString

        sharedPref = getSharedPreferences("ThresholdPreferences", Context.MODE_PRIVATE)
        dummyWorkoutID = sharedPref.getInt("nextSession", 0)

        sessionNumberText.text = "Session $dummyWorkoutID"

        // Start button functionality
        startStopButton.setOnClickListener {
//            var lock = true
//            if (lock) {
//                val dummy = WorkoutData(1,1,1,1,null)
//                lifecycleScope.launch() {
//                    db.dao.upsertTuple(dummy)
//                }
//            } else {
                if (!isDatabaseSync) {
                    if (clockRunning) { //stop timer
                        stopTimer()
                        timerBg.setBackgroundColor(black2)
                        startStopButton.setBackgroundColor(orange1)
                        startStopButton.setTextColor(black1)
                        timerDisplayText.setTextColor(white)
                        sessionNumberText.setTextColor(white)
                        dateDisplayText.setTextColor(white)
                        startStopButton.text = "Loading..."

                        //Stop ESP control
                        sendToESP("STOP")

                        dataReceiver(this)

                    } else { //start timer and startup sequence
                        resetTimer()

                        startStopButton.isEnabled = false
                        startStopButton.isClickable = false
                        binding.transitionBg.visibility = View.VISIBLE

                        val bluetoothScreen: ConstraintLayout = binding.bluetoothLoadingScreen
//                    bluetoothScreen.alpha = 0f  // Make sure the view starts invisible
//                    bluetoothScreen.visibility = View.VISIBLE

                        //signals for screens coroutines to start
                        var bluetoothConnected = false


                        //all coroutines related to start session sequence (BLE+Calibration)
                        CoroutineScope(Dispatchers.Main).launch {
                            val startInitialFade = launch {
                                bluetoothScreen.alpha = 0f  // Make sure the view starts invisible
                                bluetoothScreen.visibility = View.VISIBLE

                                bluetoothScreen.animate()
                                    .alpha(1f)  // Fade to fully visible
                                    .setDuration(1000)  // Set the duration of the fade
                                    .start()
                            }

                            startInitialFade.join()

                            //wait for BLE connection confirmation sent from other coroutine
                            val waitForBLEConn = launch {
                                while (!bluetoothConnected) {
                                    delay(50L)
                                }

                                bluetoothScreen.animate()
                                    .alpha(0f)  // Fade to fully invisible
                                    .setDuration(1000)  // Set the duration of the fade
                                    .withEndAction {
                                        bluetoothScreen.visibility =
                                            View.GONE  // After fading, set visibility to gone
                                    }
                                    .start()
                            }

                            waitForBLEConn.join()

                            val startCalibrationSequence = launch {
                                delay(1000L)
                                val calibrationScreen: ConstraintLayout = binding.calibrationScreen
                                calibrationScreen.alpha = 0f  // Make sure the view starts invisible
                                calibrationScreen.visibility = View.VISIBLE

                                calibrationScreen.animate()
                                    .alpha(1f)  // Fade to fully visible
                                    .setDuration(1000)  // Set the duration of the fade
                                    .start()

                                startCalibrationTimer()

                                // Send signal to ESP to begin data send process.
//                            sendToESP("START")

                                // Retrieve the stored hint values and set them if they exist
                                val a2Threshold1 = sharedPref.getString("a2Threshold1", "--")
                                val a2Threshold2 = sharedPref.getString("a2Threshold2", "--")
                                val a4Threshold1 = sharedPref.getString("a4Threshold1", "--")
                                val a4Threshold2 = sharedPref.getString("a4Threshold2", "--")

                                var a2Threshold1ToESP = a2Threshold1
                                var a2Threshold2ToESP = a2Threshold2
                                var a4Threshold1ToESP = a4Threshold1
                                var a4Threshold2ToESP = a4Threshold2

                                if (a2Threshold1?.length == 1){
                                    a2Threshold1ToESP = "0$a2Threshold1"
                                }

                                if (a2Threshold2?.length == 1){
                                    a2Threshold2ToESP = "0$a2Threshold2"
                                }

                                if (a4Threshold1?.length == 1){
                                    a4Threshold1ToESP = "0$a4Threshold1"
                                }

                                if (a4Threshold2?.length == 1){
                                    a4Threshold2ToESP = "0$a4Threshold2"
                                }

                                sendToESP("START$a2Threshold1ToESP$a2Threshold2ToESP$a4Threshold1ToESP$a4Threshold2ToESP ")
                            }

                            startCalibrationSequence.join()

                            //run for 11 seconds (1000 ms buffer) before end
                            val endCalibrationSequence = launch {
                                delay(11000L) // Delay in milliseconds, 11 seconds
                                stopCalibrationTimer()

                                binding.transitionBg.visibility = View.GONE

                                val calibrationScreen: ConstraintLayout = binding.calibrationScreen
                                calibrationScreen.animate()
                                    .alpha(0f)  // Fade to fully invisible
                                    .setDuration(1000)  // Set the duration of the fade
                                    .withEndAction {
                                        calibrationScreen.visibility =
                                            View.GONE  // After fading, set visibility to gone
                                    }
                                    .start()

                                resetTimer()
                                startStopButton.text = "Stop"

                            }

                            endCalibrationSequence.join()

                            val startSession = launch {
                                startTimer()
                                startStopButton.isEnabled = true
                                startStopButton.isClickable = true
                            }

                            startSession.join()

                        }

                        // Try to connect to device "CLIMB_Device"
                        CoroutineScope(Dispatchers.Main).launch {
                            delay(1000L)
                            if (bluetoothSocket == null || outputStream == null || inputStream == null) {
                                val pairedDevices: Set<BluetoothDevice>? =
                                    bluetoothAdapter?.bondedDevices
                                val esp32Device = pairedDevices?.find { it.name == "CLIMB_Device" }
                                val uuid =
                                    UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard UUID for serial
                                bluetoothSocket =
                                    esp32Device?.createRfcommSocketToServiceRecord(uuid)
                                bluetoothSocket?.connect()
                                outputStream = bluetoothSocket?.outputStream
                                inputStream = bluetoothSocket?.inputStream
                            }
                            bluetoothConnected = true
                        }

                        timerBg.setBackgroundColor(teal1)
                        startStopButton.setBackgroundColor(black1)
                        startStopButton.setTextColor(teal1)
                        timerDisplayText.setTextColor(black1)
                        sessionNumberText.setTextColor(black1)
                        dateDisplayText.setTextColor(black1)
                        //startStopButton.text = "Stop"
                    }
                }
//            }
        }

        val navView: BottomNavigationView = binding.navView

        //val customTypeface: Typeface? = ResourcesCompat.getFont(this, R.font.jetbrains_mono_regular)

        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
            )
        )

        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        db = WorkoutDataDatabase.getDatabase(this)

//        UNCOMMENT BELOW TO RESET DB
//        lifecycleScope.launch() {
//            db.dao.resetWorkoutTable()
//            val editor = sharedPref.edit()
//            editor.putInt("nextSession", 0)
//            editor.apply()

//            var finger = 0
//            for (i in 1..400) {
//                val work: WorkoutData = WorkoutData(i.toLong(),i,0,finger,null)
//                finger++
//                if (finger > 3) {
//                    finger = 0
//                }
//                db.dao.upsertTuple(work)
//            }
//            finger = 0
//            for (i in 401..800) {
//                val work: WorkoutData = WorkoutData(i.toLong(),i,1,finger,null)
//                finger++
//                if (finger > 3) {
//                    finger = 0
//                }
//                db.dao.upsertTuple(work)
//            }
//            dummyWorkoutID = 2
//            val editor = sharedPref.edit()
//            editor.putInt("nextSession", 2)
//            editor.apply()
//        }

    }

    override fun onDestroy() {
        super.onDestroy()
    }

    private fun startTimer() {
        clockRunning = true
        handler.post(updateTimeRunnable)  // Start updating the time
    }

    private fun stopTimer() {
        clockRunning = false
        handler.removeCallbacks(updateTimeRunnable)  // Stop updating the time
    }

    private fun startCalibrationTimer() {
        clockRunning = true
        handler.post(updateCalibrationTimerRunnable)  // Start updating the time
    }

    private fun stopCalibrationTimer() {
        clockRunning = false
        handler.removeCallbacks(updateCalibrationTimerRunnable)  // Stop updating the time
    }


    private fun resetTimer() {
        timeElapsed = 0
        updateStopwatchDisplay()
        if (!clockRunning) {
            startStopButton.text = "Start"
        }
    }

    // Send a string to the ESP via the bluetooth output buffer.
    private fun sendToESP(item: String) {
        try {
            outputStream?.write(item.toByteArray())
            //Log.i("Bluetooth", "$A2threshold11")
        } catch (e: Exception) {
            return // maybe deal with error eventually. but for now, don't do anything
        }
    }

    private fun dataReceiver(context: Context) {
        lifecycleScope.launch() {
            withContext(Dispatchers.IO) {
                isDatabaseSync = true
                try {
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    var delta: Long = 0
                    while (true) {
                        val line = reader.readLine() ?: break

                        if (line == "EOF") {
                            break
                        }
                        val values = line.split(":").map { it.toInt() }
                        val pointer = values[0]
                        val middle = values[1]
                        val ring = values[2]
                        val pinky = values[3]
                        val entry0 = WorkoutData(
                            delta,
                            pointer,
                            dummyWorkoutID,
                            0,
                            null
                        )
                        val entry1 = WorkoutData(
                            delta,
                            middle,
                            dummyWorkoutID,
                            1,
                            null
                        )
                        val entry2 = WorkoutData(
                            delta,
                            ring,
                            dummyWorkoutID,
                            2,
                            null
                        )
                        val entry3 = WorkoutData(
                            delta,
                            pinky,
                            dummyWorkoutID,
                            3,
                            null
                        )

                        db.dao.upsertTuple(entry0)
                        db.dao.upsertTuple(entry1)
                        db.dao.upsertTuple(entry2)
                        db.dao.upsertTuple(entry3)
                        delta += 1
                    }
                    dummyWorkoutID++

                    runOnUiThread {
                        val teal1 = ContextCompat.getColor(context, R.color.teal1)
                        startStopButton.text = "Start"
                        startStopButton.setBackgroundColor(teal1)
                        sessionNumberText.text = "Session $dummyWorkoutID"
                    }

                    val maxF3A2 = db.dao.getMaxFRFromFingerAndID(0, dummyWorkoutID-1)
                    val maxF4A2 = db.dao.getMaxFRFromFingerAndID(1, dummyWorkoutID-1)
                    val maxF3A4 = db.dao.getMaxFRFromFingerAndID(2, dummyWorkoutID-1)
                    val maxF4A4 = db.dao.getMaxFRFromFingerAndID(3, dummyWorkoutID-1)

                    println("maxF3A2: " + maxF3A2)
                    println("maxA4A2: " + maxF4A2)
                    println("maxA3A4: " + maxF3A4)
                    println("maxA4A4: " + maxF4A4)

                    var maxA2 = 0
                    var maxA4 = 0
                    //get around int? type mismatch (bruh)
                    if ((maxF3A2 != null) && (maxF4A2 != null)) {
                        maxA2 = max(maxF3A2, maxF4A2)
                        maxA2 = max(maxA2, 0)
                    }

                    if ((maxF3A4 != null) && (maxF4A4 != null)) {
                        maxA4 = max(maxF3A4, maxF4A4)
                        maxA4 = max(maxA4, 0)
                    }

                    val editor = sharedPref.edit()
                    editor.putInt("maxA2", maxA2)
                    editor.putInt("nextSession", dummyWorkoutID)
                    editor.putInt("maxA4", maxA4)
                    editor.apply()

                } catch (_: Exception) {
                    // do nothing? idk what to do for now LMFAO JUBI
                }
                isDatabaseSync = false
            }
        }
    }

    // Update the display with the current time
    fun updateStopwatchDisplay() {
        val hours = timeElapsed / 3600
        val minutes = timeElapsed / 60
        val seconds = timeElapsed % 60
        val timeText = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        timerDisplay.text = timeText
    }

    fun updateCalibrationDisplay() {
        val secondsLeft = 11-timeElapsed //cheese to make sure 10 shows up
        val timeLeft = String.format("%02d", secondsLeft)
        binding.calibrationTimerText.text = timeLeft
    }

    fun setChartData(lineChart: LineChart, finger: Int, workoutID: Int?) {

        //val sharedPrefs = getSharedPreferences("ThresholdPreferences", Context.MODE_PRIVATE)
        var lastSession: Int
        if (workoutID == null) {
            lastSession = sharedPref.getInt("nextSession", 0)
            if (lastSession != 0) {
                lastSession -= 1
            } else {
                return
            }
        } else {
            lastSession = workoutID
        }

        lifecycleScope.launch {
            val workoutData = db.dao.getWorkoutByIDAndFingOrderByTime(lastSession, finger)

//            var entriesAL = ArrayList<Entry>()
//
//            var i = 0;
//            while(i < 30) {
//                var tempEntry = Entry(i.toFloat(), i.toFloat())
//                entriesAL += tempEntry
//                i++
//            }
//
//            val entries: List<Entry> = entriesAL

            val entries = workoutData.map { data ->
                Entry(data.time.toFloat(), data.forceReading.toFloat())
            }

            val lineDataSet = LineDataSet(entries, "Force Readings").apply {
                color = getColor(R.color.orange1)
                valueTextColor = getColor(R.color.black)
                lineWidth = 2f
                circleRadius = 4f
                setCircleColor(getColor(R.color.teal_700))
                setDrawCircleHole(false)
                setDrawValues(false)
                setDrawCircles(false)
            }

            // Add dataset to the chart
            lineChart.data = LineData(lineDataSet)

            // Customize chart appearance
            lineChart.apply {

                description.isEnabled = false
                legend.isEnabled = false
                axisRight.isEnabled = false

                // Format X-Axis to display time
                xAxis.apply {
                    granularity = 1f
//                    valueFormatter = TimeValueFormatter()
                }
            }

            // Refresh the chart
            lineChart.invalidate()
        }


    }
}

//class TimeValueFormatter : ValueFormatter() {
//
//    private val dateFormat = SimpleDateFormat("HH:mm:ss", Locale.getDefault())
//
//    override fun getFormattedValue(value: Float): String {
//        // Convert the float value (assumed to be milliseconds) to a formatted time string
//        val timeInMillis = value.toLong()
//        return dateFormat.format(timeInMillis)
//    }
//}
