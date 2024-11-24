package com.example.climbc1

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.ClipData
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.icu.util.Calendar
import android.media.RouteListingPreference
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.climbc1.databinding.ActivityMainBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.util.UUID
import java.util.logging.Handler

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

        // Start button functionality
        startStopButton.setOnClickListener {

            if (clockRunning) { //stop timer
                stopTimer()
                timerBg.setBackgroundColor(black2)
                startStopButton.setBackgroundColor(teal1)
                startStopButton.setTextColor(black1)
                timerDisplayText.setTextColor(white)
                sessionNumberText.setTextColor(white)
                dateDisplayText.setTextColor(white)
                startStopButton.text = "Loading..."

                //Stop ESP control
                sendToESP("STOP")
                dataReceiver()


            } else { //start timer and startup sequence
                resetTimer()

                startStopButton.isEnabled = false
                startStopButton.isClickable = false
                binding.transitionBg.visibility = View.VISIBLE

                val bluetoothScreen: ConstraintLayout = binding.bluetoothLoadingScreen
                bluetoothScreen.alpha = 0f  // Make sure the view starts invisible
                bluetoothScreen.visibility = View.VISIBLE

                bluetoothScreen.animate()
                    .alpha(1f)  // Fade to fully visible
                    .setDuration(1000)  // Set the duration of the fade
                    .start()

                var bluetoothConnected = false

                // bluetooth screen goes away
                CoroutineScope(Dispatchers.Main).launch {
                    delay(1000L) // Delay in milliseconds
                    while(!bluetoothConnected) {
                        delay(50L)
                    }

                    bluetoothScreen.animate()
                        .alpha(0f)  // Fade to fully invisible
                        .setDuration(1000)  // Set the duration of the fade
                        .withEndAction {
                            bluetoothScreen.visibility = View.GONE  // After fading, set visibility to gone
                        }
                        .start()

                }

                // Try to connect to device "CLIMB_Device"
                CoroutineScope(Dispatchers.Main).launch {
                    if (bluetoothSocket == null || outputStream == null || inputStream == null) {
                        val pairedDevices: Set<BluetoothDevice>? = bluetoothAdapter?.bondedDevices
                        val esp32Device = pairedDevices?.find { it.name == "CLIMB_Device" }
                        val uuid =
                            UUID.fromString("00001101-0000-1000-8000-00805F9B34FB") // Standard UUID for serial
                        bluetoothSocket = esp32Device?.createRfcommSocketToServiceRecord(uuid)
                        bluetoothSocket?.connect()
                        outputStream = bluetoothSocket?.outputStream
                        inputStream = bluetoothSocket?.inputStream
                    }
                    bluetoothConnected = true
                }

                //calibration starts
                CoroutineScope(Dispatchers.Main).launch {
                    delay(6600L) // Delay in milliseconds
                    val calibrationScreen: ConstraintLayout = binding.calibrationScreen
                    calibrationScreen.alpha = 0f  // Make sure the view starts invisible
                    calibrationScreen.visibility = View.VISIBLE

                    calibrationScreen.animate()
                        .alpha(1f)  // Fade to fully visible
                        .setDuration(1000)  // Set the duration of the fade
                        .start()
                }

                //calibration starts
                CoroutineScope(Dispatchers.Main).launch {
                    delay(6700L) // Delay in milliseconds
                    startCalibrationTimer()
                }

                CoroutineScope(Dispatchers.Main).launch {
                    delay(16800L) // Delay in milliseconds
                    stopCalibrationTimer()
                }



                //calibration ends
                CoroutineScope(Dispatchers.Main).launch {
                    delay(16900L) // Delay in milliseconds

                    binding.transitionBg.visibility = View.GONE

                    val calibrationScreen: ConstraintLayout = binding.calibrationScreen
                    calibrationScreen.animate()
                        .alpha(0f)  // Fade to fully invisible
                        .setDuration(1000)  // Set the duration of the fade
                        .withEndAction {
                            calibrationScreen.visibility = View.GONE  // After fading, set visibility to gone
                        }
                        .start()

                    resetTimer()
                    startStopButton.text = "Stop"

                    // Send signal to ESP to begin data send process.
                    sendToESP("START")
                    lastUnixTimeSinceStart = System.currentTimeMillis()


                }

                CoroutineScope(Dispatchers.Main).launch {
                    delay(17900L) // Delay in milliseconds
                    startTimer()

                    startStopButton.isEnabled = true
                    startStopButton.isClickable = true

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
        } catch (e: Exception) {
            return // maybe deal with error eventually. but for now, don't do anything
        }
    }

    private fun dataReceiver() {
        Thread {
            try {
                val reader = BufferedReader(InputStreamReader(inputStream))
                var delta: Long = 0
                while (true) {
                    isDatabaseSync = true
                    val line = reader.readLine() ?: break

                    if (line == "EOF") {
                        break
                    }
                    val values = line.split(":").map { it.toInt() }
                    val pointer = values[0]
                    val middle = values[1]
                    val ring = values[2]
                    val pinky = values[3]
                    val entry0 = WorkoutData(lastUnixTimeSinceStart+delta,pointer,dummyWorkoutID,0, null)
                    val entry1 = WorkoutData(lastUnixTimeSinceStart+delta,middle,dummyWorkoutID,1, null)
                    val entry2 = WorkoutData(lastUnixTimeSinceStart+delta,ring,dummyWorkoutID,2, null)
                    val entry3 = WorkoutData(lastUnixTimeSinceStart+delta,pinky,dummyWorkoutID,3, null)

                    lifecycleScope.launch() {
                        withContext(Dispatchers.IO) {
                            db.dao.upsertTuple(entry0)
                            db.dao.upsertTuple(entry1)
                            db.dao.upsertTuple(entry2)
                            db.dao.upsertTuple(entry3)
                        }
                    }
                    delta += 100
                }

                runOnUiThread {
                    dummyWorkoutID++
                    startStopButton.text = "Start"
                    isDatabaseSync = false
                }
            } catch(e: Exception) {
                // do nothing? idk what to do for now
                isDatabaseSync = false
            }
        }.start()
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
}