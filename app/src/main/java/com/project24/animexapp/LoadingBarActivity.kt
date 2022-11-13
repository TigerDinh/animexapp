package com.project24.animexapp

import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import java.lang.Thread.sleep

class LoadingBarActivity : AppCompatActivity() {

    private val isDisabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_progess_bar)

        setupProgressBar()
    }

    private fun setupProgressBar() {
        val pbTimeRemaining = findViewById<ProgressBar>(R.id.pbTimeRemaining);

        // 1 second loading time
        val desiredLoadingTimeInMilliseconds = 1000.toLong()

        // Set Timer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            val timer = object : CountDownTimer(desiredLoadingTimeInMilliseconds, 250) {
                override fun onTick(millisUntilFinished: Long) {
                    val progress = (((desiredLoadingTimeInMilliseconds - millisUntilFinished).toDouble()/desiredLoadingTimeInMilliseconds)*100.00).toInt()
                    pbTimeRemaining.setProgress(progress, true)
                }

                override fun onFinish() {
                    finish()
                }
            }
            timer.start()
        }
        else {
            sleep(desiredLoadingTimeInMilliseconds)
            finish()
        }
    }

    // Disable back button while loading to prevent overloading api request
    override fun onBackPressed() {
        if (!isDisabled) {
            super.onBackPressed()
        }
    }
}