package com.example.climbc1

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.example.climbc1.databinding.ActivityMainBinding
import java.util.logging.Handler

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val teal1 = ContextCompat.getColor(this, R.color.teal1)
        val black2 = ContextCompat.getColor(this, R.color.black2)
        val black1 = ContextCompat.getColor(this, R.color.black)
        val white = ContextCompat.getColor(this, R.color.white)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        timerDisplay = findViewById(R.id.timerDisplayText)
        startStopButton = findViewById(R.id.sessionStartButton)
        timerBg = findViewById(R.id.rectangle)
        timerDisplayText = findViewById(R.id.timerDisplayText)
        sessionNumberText = findViewById(R.id.sessionNumberText)
        dateDisplayText = findViewById(R.id.dateDisplayText)

        startStopButton.setBackgroundColor(teal1)
        startStopButton.setTextColor(black1)

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
                startStopButton.text = "Start"

            } else { //start timer
                resetTimer()
                startTimer()
                timerBg.setBackgroundColor(teal1)
                startStopButton.setBackgroundColor(black1)
                startStopButton.setTextColor(teal1)
                timerDisplayText.setTextColor(black1)
                sessionNumberText.setTextColor(black1)
                dateDisplayText.setTextColor(black1)
                startStopButton.text = "Stop"
            }
        }

        val navView: BottomNavigationView = binding.navView

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
    }

    private fun startTimer() {
        clockRunning = true
        handler.post(updateTimeRunnable)  // Start updating the time
    }

    private fun stopTimer() {
        clockRunning = false
        handler.removeCallbacks(updateTimeRunnable)  // Stop updating the time
    }

    private fun resetTimer() {
        timeElapsed = 0
        updateStopwatchDisplay()
        if (!clockRunning) {
            startStopButton.text = "Start"
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
}